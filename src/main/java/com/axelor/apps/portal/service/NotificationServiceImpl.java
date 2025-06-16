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

import com.axelor.app.AppSettings;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.db.PortalWorkspace;
import com.axelor.common.ObjectUtils;
import com.axelor.db.tenants.TenantConfig;
import com.axelor.db.tenants.TenantConfigProvider;
import com.axelor.db.tenants.TenantResolver;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.studio.db.AppGooveePortal;
import com.axelor.studio.db.repo.AppGooveePortalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.http.HTTPClient;
import wslite.http.HTTPMethod;
import wslite.http.HTTPRequest;
import wslite.http.HTTPResponse;

public class NotificationServiceImpl implements NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected MessageRepository messageRepo;
  protected AppGooveePortalRepository appGooveePortalRepo;

  @Inject
  public NotificationServiceImpl(
      AppBaseService appBaseService,
      MessageRepository messageRepo,
      AppGooveePortalRepository appGooveePortalRepo) {
    this.appBaseService = appBaseService;
    this.messageRepo = messageRepo;
    this.appGooveePortalRepo = appGooveePortalRepo;
  }

  @Override
  public void notifyUser(String code, Long id, String className, PortalWorkspace portalWorkspace) {

    if (ObjectUtils.isEmpty(code) || ObjectUtils.isEmpty(portalWorkspace) || id == null) {
      return;
    }

    AppGooveePortal appGooveePortal = appGooveePortalRepo.all().fetchOne();
    if (appGooveePortal == null) {
      return;
    }

    String notificationUrl = appGooveePortal.getNotificationWebhookUrl();
    if (ObjectUtils.isEmpty(notificationUrl)) {
      return;
    }

    try {
      HTTPResponse response = requestWebhook(code, id, className, portalWorkspace, notificationUrl);

      if (response.getStatusCode() == 200) {
        LOG.info("Successful webhook request");
      } else {
        TraceBackService.trace(
            new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                String.format("%s : %s", response.getStatusCode(), response.getStatusMessage())));
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  protected String getAuthorization(String tenantId) {
    String username = null;
    String password = null;
    if (ObjectUtils.notEmpty(tenantId)) {
      try {
        TenantConfig tenantConfig = Beans.get(TenantConfigProvider.class).find(tenantId);
        // TODO : Access Tenant credentials
        username = "";
        password = "";
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }

    if (ObjectUtils.isEmpty(username)) {
      username = AppSettings.get().get("portal.ws.user");
      password = AppSettings.get().get("portal.ws.password");
    }
    String encodedAuth =
        Base64.getEncoder()
            .encodeToString(
                String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8));

    return String.format("Basic %s", encodedAuth);
  }

  protected String getRequestData(
      String code, Long id, String className, PortalWorkspace portalWorkspace, String tenantId) {

    ObjectNode record = new ObjectMapper().createObjectNode();
    record.put("id", id);

    ObjectNode data = new ObjectMapper().createObjectNode();
    data.put("tenantId", tenantId);
    data.put("timestamp", appBaseService.getTodayDateTime(null).toInstant().toEpochMilli());
    data.put("code", code);
    data.set("record", record);
    data.set("mail", getMail(id, className));
    data.put("workspaceUrl", portalWorkspace.getUrl());
    return data.toString();
  }

  protected ObjectNode getMail(Long id, String className) {

    ObjectNode mail = null;
    Message message =
        messageRepo
            .all()
            .filter(
                "self.id IN (SELECT message.id FROM MultiRelated mr WHERE mr.relatedToSelectId = :relatedId AND mr.relatedToSelect = :relatedModel)")
            .bind("relatedId", id)
            .bind("relatedModel", className)
            .fetchOne();
    if (message != null) {
      mail = new ObjectMapper().createObjectNode();
      mail.put("subject", message.getSubject());
      mail.put("content", message.getContent());
    }

    return mail;
  }

  protected HTTPResponse requestWebhook(
      String code,
      Long id,
      String className,
      PortalWorkspace portalWorkspace,
      String notificationUrl)
      throws MalformedURLException {

    URL url = new URL(notificationUrl);
    String tenantId = TenantResolver.currentTenantIdentifier();
    if (ObjectUtils.isEmpty(tenantId)) {
      tenantId = AppSettings.get().get("portal.ws.tenantId");
    }

    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("Accept", "application/json");
    headers.put("Authorization", getAuthorization(tenantId));

    HTTPClient httpclient = new HTTPClient();
    HTTPRequest request = new HTTPRequest();
    request.setMethod(HTTPMethod.POST);
    request.setUrl(url);
    request.setHeaders(headers);
    request.setData(
        getRequestData(code, id, className, portalWorkspace, tenantId)
            .getBytes(StandardCharsets.UTF_8));

    return httpclient.execute(request);
  }
}
