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
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalProjectFix {

  protected final ProjectRepository projectRepository;
  protected final MattermostService matterMostService;
  protected final MattermostPortalService mattermostPortalService;
  protected final UserRepository userRepository;
  protected final PartnerRepository partnerRepository;
  protected static final int FETCH_LIMIT = 10;
  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public PortalProjectFix(
      ProjectRepository projectRepository,
      MattermostService matterMostService,
      MattermostPortalService mattermostPortalService,
      UserRepository userRepository,
      PartnerRepository partnerRepository) {
    this.projectRepository = projectRepository;
    this.matterMostService = matterMostService;
    this.userRepository = userRepository;
    this.partnerRepository = partnerRepository;
    this.mattermostPortalService = mattermostPortalService;
  }

  public void initializeMattermost() throws AxelorException {

    matterMostService.createTeam();
    createUsers();
    createPartners();
    createProject();
  }

  protected void createProject() {
    Query<Project> projectQuery =
        projectRepository
            .all()
            .filter("self.archived is null OR self.archived = false")
            .order("id");
    List<Project> projectList = new ArrayList<Project>();
    int OFFSET = 0;
    long projectToUpdate = projectQuery.count();
    while (!(projectList = projectQuery.fetch(FETCH_LIMIT, OFFSET)).isEmpty()) {
      projectList.forEach(matterMostService::syncProject);
      JPA.clear();
      OFFSET += projectList.size();
      LOG.debug("Done " + OFFSET + "/" + projectToUpdate);
    }
  }

  protected void createUsers() {
    Query<User> userQuery =
        userRepository
            .all()
            .filter("(self.archived is null OR self.archived = false) AND self.email != null")
            .order("id");
    List<User> userList = new ArrayList<User>();
    int OFFSET = 0;
    long userToUpdate = userQuery.count();
    while (!(userList = userQuery.fetch(FETCH_LIMIT, OFFSET)).isEmpty()) {
      userList.forEach(matterMostService::createUser);
      JPA.clear();
      OFFSET += userList.size();
      LOG.debug("Done " + OFFSET + "/" + userToUpdate);
    }
  }

  private void createPartners() {
    Query<Partner> partnerQuery =
        partnerRepository
            .all()
            .filter(
                "(self.archived is null OR self.archived = false) AND self.isCustomer = true AND self.isActivatedOnPortal = true")
            .order("id");
    List<Partner> partnerList = new ArrayList<Partner>();
    int OFFSET = 0;
    long partnerToUpdate = partnerQuery.count();
    while (!(partnerList = partnerQuery.fetch(FETCH_LIMIT, OFFSET)).isEmpty()) {
      partnerList.forEach(mattermostPortalService::createUsers);
      JPA.clear();
      OFFSET += partnerList.size();
      LOG.debug("Done " + OFFSET + "/" + partnerToUpdate);
    }
  }
}
