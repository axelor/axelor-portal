/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.portal.mattermost.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.app.service.AppMattermostService;
import com.axelor.apps.mattermost.exception.MattermostExceptionMessage;
import com.axelor.apps.mattermost.mattermost.MattermostRestLinker;
import com.axelor.apps.mattermost.mattermost.MattermostRestUser;
import com.axelor.apps.mattermost.mattermost.service.MattermostServiceImpl;
import com.axelor.apps.portal.db.PortalApp;
import com.axelor.apps.portal.db.PortalContactAppPermission;
import com.axelor.apps.portal.db.PortalContactWorkspaceConfig;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.message.db.EmailAddress;
import com.axelor.studio.db.AppMattermost;
import com.axelor.studio.db.repo.AppMattermostRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class MattermostPortalServiceImpl extends MattermostServiceImpl
    implements MattermostPortalService {

  @Inject
  public MattermostPortalServiceImpl(
      AppMattermostService appMattermostService, AppMattermostRepository appMattermostRepository) {
    super(appMattermostService, appMattermostRepository);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void syncProject(Project project) {
    try {
      initialize();
      getTeamId();
      if (project.getChatVisibilitySelect() == ProjectRepository.CHAT_VISIBILITY_NO_CHAT) {
        removeChannel(project);
        project.setMattermostChannelId(null);
        return;
      }
      if (ObjectUtils.isEmpty(project.getMattermostChannelId())) {
        project.setMattermostChannelId(createChannel(project));
      } else {
        updateProjectName(project);
      }
      Collection<User> userCollection = project.getMembersUserSet();
      createUsers(userCollection);
      if (project.getChatVisibilitySelect() == ProjectRepository.CHAT_VISIBILITY_CUSTOMER_CHAT
          && project.getClientPartner() != null) {
        createUsers(project.getClientPartner());
      }
      linkUsersToTeamAndChannel(project);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void savePartner(Partner partner) {

    JPA.save(partner);
  }

  @Override
  public void createUsers(Partner partner) {
    try {
      initialize();
      checkMailAddress(partner);
      String email = partner.getEmailAddress().getAddress();
      if (ObjectUtils.isEmpty(partner.getMattermostUserId())) {
        String userId =
            new MattermostRestUser(url, token)
                .createUser(
                    partner.getId(), email, partner.getFirstName(), partner.getName(), email);
        if (ObjectUtils.notEmpty(userId)) {
          partner.setMattermostUserId(userId);
        }
        savePartner(partner);
      }
      if (partner.getIsContact()) {
        Set<PortalContactWorkspaceConfig> portalContactWorkspaceConfigSet =
            partner.getContactWorkspaceConfigSet();
        for (PortalContactWorkspaceConfig portalContactWorkspaceConfig :
            portalContactWorkspaceConfigSet) {
          updateChatAccessForContacts(portalContactWorkspaceConfig);
        }
      }
      Set<Partner> contactSet = partner.getContactPartnerSet();
      if (CollectionUtils.isEmpty(contactSet)) {
        return;
      }
      for (Partner contact : contactSet) {
        createUsers(contact);
      }
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  protected Partner findPartnerByMattermostId(String mattermostUserId) {
    return Query.of(Partner.class)
        .filter("self.mattermostUserId = :mattermostUserId")
        .bind("mattermostUserId", mattermostUserId)
        .fetchOne();
  }

  protected void checkMailAddress(Partner partner) throws AxelorException {
    EmailAddress emailAddress = partner.getEmailAddress();
    if (emailAddress == null || ObjectUtils.isEmpty(emailAddress.getAddress())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          MattermostExceptionMessage.MATTERMOST_MISSING_MAIL_ON_PARTNER,
          partner.getFullName());
    }
  }

  protected boolean checkContactPermission(Partner partner) throws AxelorException {
    Set<PortalContactWorkspaceConfig> workspaceConfigSet = partner.getContactWorkspaceConfigSet();
    AppMattermost appMattermost = appMattermostService.getAppMattermost();
    PortalApp portalApp = appMattermost.getPortalAppToAccessChat();
    if (CollectionUtils.isEmpty(workspaceConfigSet) || ObjectUtils.isEmpty(portalApp)) {
      return false;
    }
    for (PortalContactWorkspaceConfig portalContactWorkspaceConfig : workspaceConfigSet) {
      List<PortalContactAppPermission> appPermissionList =
          portalContactWorkspaceConfig.getContactAppPermissionList();
      if (CollectionUtils.isEmpty(appPermissionList)) {
        continue;
      }
      for (PortalContactAppPermission appPermission : appPermissionList) {
        if (appPermission.getApp() != null && appPermission.getId().equals(portalApp.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void linkUsersToTeamAndChannel(Project project) throws AxelorException {
    if (project.getChatVisibilitySelect() == ProjectRepository.CHAT_VISIBILITY_NO_CHAT) {
      return;
    }

    checkProject(project);
    String channelId = project.getMattermostChannelId();

    Collection<User> collectionUser = project.getMembersUserSet();
    MattermostRestLinker mattermostRestLinker = new MattermostRestLinker(url, token);
    List<String> userIDs = new ArrayList<String>();
    for (User user : collectionUser) {
      try {
        if (!user.getCanAccessChat()) {
          removeUserFromChannel(project, user.getMattermostUserId());
          continue;
        }
        userIDs.add(user.getMattermostUserId());
        mattermostRestLinker.linkUsersToTeamAndChannel(
            user.getMattermostUserId(), teamId, channelId);
      } catch (Exception e) {
        TraceBackService.trace(e, "mattermost");
      }
    }
    if (project.getChatVisibilitySelect() == ProjectRepository.CHAT_VISIBILITY_INTERNAL_CHAT) {
      removeNotFoundUsers(userIDs, project);
      return;
    }
    Partner customer = project.getClientPartner();
    if (customer == null) {
      return;
    }
    try {
      checkMailAddress(customer);
      userIDs.add(customer.getMattermostUserId());
      mattermostRestLinker.linkUsersToTeamAndChannel(
          customer.getMattermostUserId(), teamId, channelId);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
    Set<Partner> contactSet = customer.getContactPartnerSet();
    if (CollectionUtils.isEmpty(contactSet)) {
      return;
    }
    for (Partner partner : contactSet) {
      try {
        checkMailAddress(partner);
        if (checkContactPermission(partner)) {
          userIDs.add(partner.getMattermostUserId());
          mattermostRestLinker.linkUsersToTeamAndChannel(
              partner.getMattermostUserId(), teamId, channelId);
        }
      } catch (Exception e) {
        TraceBackService.trace(e, "mattermost");
      }
    }
    try {
      addContactWithAllAccess();
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }

    removeNotFoundUsers(userIDs, project);
  }

  protected void addContactWithAllAccess() throws AxelorException {
    AppMattermost appMattermost = appMattermostService.getAppMattermost();
    PortalApp portalApp = appMattermost.getPortalAppToAccessChat();
    List<PortalContactAppPermission> portalContactAppPermissionList =
        JPA.all(PortalContactAppPermission.class)
            .filter("self.app = :app AND self.roleSelect = 'total'")
            .bind("app", portalApp)
            .fetch();
    List<PortalContactWorkspaceConfig> portalContactWorkspaceConfigList =
        new ArrayList<PortalContactWorkspaceConfig>();
    for (PortalContactAppPermission portalContactAppPermission : portalContactAppPermissionList) {
      PortalContactWorkspaceConfig portalContactWorkspaceConfig =
          portalContactAppPermission.getContactWorkspaceConfig();
      if (!portalContactWorkspaceConfigList.contains(portalContactWorkspaceConfig)) {
        portalContactWorkspaceConfigList.add(portalContactWorkspaceConfig);
        updateChatAccessForContacts(portalContactWorkspaceConfig);
      }
    }
  }

  @Override
  public void updateChatAccessForContacts(PortalContactWorkspaceConfig portalContactWorkspaceConfig)
      throws AxelorException {
    String roleSelect = getRoleSelect(portalContactWorkspaceConfig);
    List<Partner> contactList =
        JPA.all(Partner.class)
            .filter(
                "self.isContact = true AND :portalContactWorkspaceConfig MEMBER OF self.contactWorkspaceConfigSet")
            .bind("portalContactWorkspaceConfig", portalContactWorkspaceConfig)
            .fetch();
    for (Partner partner : contactList) {
      switch (roleSelect) {
        case "restricted":
          updateAccessToChat(partner, false);
          break;
        case "total":
          updateAccessToChat(partner, true);
          break;
        default:
          removeAccessToChat(partner);
      }
    }
  }

  protected void removeAccessToChat(Partner partner) throws AxelorException {
    initialize();
    List<Project> allCustomerChatProjects =
        JPA.all(Project.class).filter("self.chatVisibilitySelect > 2").fetch();

    if (CollectionUtils.isEmpty(allCustomerChatProjects)) {
      return;
    }
    for (Project project : allCustomerChatProjects) {
      try {
        removeUserFromChannel(project, partner.getMattermostUserId());
      } catch (Exception e) {
        TraceBackService.trace(e, "mattermost");
      }
    }
  }

  protected void updateAccessToChat(Partner partner, boolean accessToAllProject)
      throws AxelorException {
    initialize();

    List<Project> projectWithContact =
        JPA.all(Project.class)
            .filter(
                ":partner MEMBER OF self.clientPartner.contactPartnerSet AND self.chatVisibilitySelect > 2")
            .bind("partner", partner)
            .fetch();
    List<Project> allCustomerChatProjects =
        JPA.all(Project.class).filter("self.chatVisibilitySelect > 2").fetch();

    if (CollectionUtils.isEmpty(projectWithContact)
        && CollectionUtils.isEmpty(allCustomerChatProjects)) {
      return;
    }
    if (accessToAllProject) {
      for (Project project : allCustomerChatProjects) {
        try {
          addUserToChannel(project, partner.getMattermostUserId());
        } catch (Exception e) {
          TraceBackService.trace(e, "mattermost");
        }
      }
    } else {
      for (Project project : projectWithContact) {
        try {
          addUserToChannel(project, partner.getMattermostUserId());
        } catch (Exception e) {
          TraceBackService.trace(e, "mattermost");
        }
      }
      for (Project project : allCustomerChatProjects) {
        if (!projectWithContact.contains(project)) {
          try {
            removeUserFromChannel(project, partner.getMattermostUserId());
          } catch (Exception e) {
            TraceBackService.trace(e, "mattermost");
          }
        }
      }
    }
  }

  protected String getRoleSelect(PortalContactWorkspaceConfig portalContactWorkspaceConfig) {
    AppMattermost appMattermost = appMattermostService.getAppMattermost();
    PortalApp portalApp = appMattermost.getPortalAppToAccessChat();
    if (ObjectUtils.isEmpty(portalApp)) {
      return "";
    }
    List<PortalContactAppPermission> appPermissionList =
        portalContactWorkspaceConfig.getContactAppPermissionList();
    for (PortalContactAppPermission appPermission : appPermissionList) {
      if (appPermission.getApp() != null
          && appPermission.getApp().getId().equals(portalApp.getId())) {
        return appPermission.getRoleSelect();
      }
    }
    return "";
  }

  protected boolean checkApp(PortalContactWorkspaceConfig portalContactWorkspaceConfig) {
    AppMattermost appMattermost = appMattermostService.getAppMattermost();
    PortalApp portalApp = appMattermost.getPortalAppToAccessChat();
    if (ObjectUtils.isEmpty(portalApp)) {
      return false;
    }
    List<PortalContactAppPermission> appPermissionList =
        portalContactWorkspaceConfig.getContactAppPermissionList();
    if (CollectionUtils.isEmpty(appPermissionList)) {
      return false;
    }
    for (PortalContactAppPermission appPermission : appPermissionList) {
      if (appPermission.getApp() != null
          && appPermission.getApp().getId().equals(portalApp.getId())) {
        return true;
      }
    }
    return false;
  }
}
