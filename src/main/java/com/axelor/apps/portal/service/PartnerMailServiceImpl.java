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
package com.axelor.apps.portal.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.db.PartnerPortalWorkspace;
import com.axelor.apps.portal.db.PortalAppConfig;
import com.axelor.apps.portal.db.PortalWorkspace;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public class PartnerMailServiceImpl implements PartnerMailService {

  protected final MessageService messageService;
  protected final TemplateMessageService templateMessageService;

  @Inject
  public PartnerMailServiceImpl(
      MessageService messageService, TemplateMessageService templateMessageService) {
    this.messageService = messageService;
    this.templateMessageService = templateMessageService;
  }

  @Override
  public String sendExampleEmail(List<Partner> partners, PortalWorkspace workspace)
      throws AxelorException {
    PartnerPortalWorkspace partnerWorkspace = workspace.getDefaultPartnerWorkspace();
    if (partnerWorkspace == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.NO_DEFAULT_PARTNER_WORKSPACE));
    }

    PortalAppConfig portalAppConfig = partnerWorkspace.getPortalAppConfig();
    if (portalAppConfig == null || portalAppConfig.getPartnerEmailTemplate() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.NO_PARTNER_EMAIL_TEMPLATE));
    }

    Template template = portalAppConfig.getPartnerEmailTemplate();
    StringBuilder errors = new StringBuilder();

    for (Partner partner : partners) {
      if (partner.getEmailAddress() == null
          || partner.getEmailAddress().getAddress() == null
          || partner.getEmailAddress().getAddress().isEmpty()) {
        continue;
      }

      try {
        sendEmailToPartner(partner, template, workspace);
      } catch (Exception e) {
        errors.append(partner.getName()).append("\n");
        TraceBackService.trace(e);
      }
    }

    return errors.toString();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void sendEmailToPartner(Partner partner, Template template, PortalWorkspace workspace)
      throws MessagingException, ClassNotFoundException, IOException, JSONException {
    Message message = templateMessageService.generateMessage(partner, template);

    // Generate registration link from workspace URL
    String registrationLink = generateRegistrationLink(workspace);
    if (message.getContent() != null) {
      message.setContent(message.getContent().replace("{{REGISTRATION_LINK}}", registrationLink));
    }
    if (message.getSubject() != null) {
      message.setSubject(message.getSubject().replace("{{REGISTRATION_LINK}}", registrationLink));
    }

    messageService.sendByEmail(message);
  }

  /**
   * Generate a registration link from the workspace URL.
   *
   * <p>Example: If workspace URL is "http://localhost:3001/d/france", the generated link will be:
   * "http://localhost:3001/auth/register/email?callbackurl=%2Fd%2Ffrance&workspaceURI=%2Fd%2Ffrance&tenant=d&type=company"
   *
   * @param workspace the portal workspace
   * @return the registration link
   */
  protected String generateRegistrationLink(PortalWorkspace workspace) {
    String workspaceUrl = workspace.getUrl();
    if (workspaceUrl == null || workspaceUrl.isEmpty()) {
      return "";
    }

    try {
      URI uri = new URI(workspaceUrl);
      String baseUrl = uri.getScheme() + "://" + uri.getHost();
      if (uri.getPort() != -1) {
        baseUrl += ":" + uri.getPort();
      }

      String path = uri.getPath(); // e.g., "/d/france"
      String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString());

      // Extract tenant (first segment after the leading slash)
      String tenant = "";
      if (path != null && path.length() > 1) {
        String[] segments = path.substring(1).split("/");
        if (segments.length > 0) {
          tenant = segments[0]; // e.g., "d"
        }
      }

      return baseUrl
          + "/auth/register/email?callbackurl="
          + encodedPath
          + "&workspaceURI="
          + encodedPath
          + "&tenant="
          + tenant
          + "&type=company";
    } catch (Exception e) {
      TraceBackService.trace(e);
      return workspaceUrl;
    }
  }
}
