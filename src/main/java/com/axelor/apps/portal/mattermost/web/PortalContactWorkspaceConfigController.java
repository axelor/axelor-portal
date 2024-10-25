package com.axelor.apps.portal.mattermost.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.db.PortalContactWorkspaceConfig;
import com.axelor.apps.portal.mattermost.service.MattermostPortalService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PortalContactWorkspaceConfigController {

  public void updateChatAccessForContacts(ActionRequest request, ActionResponse response) {
    try {
      PortalContactWorkspaceConfig portalContactWorkspaceConfig =
          request.getContext().asType(PortalContactWorkspaceConfig.class);
      Beans.get(MattermostPortalService.class)
          .updateChatAccessForContacts(portalContactWorkspaceConfig);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
