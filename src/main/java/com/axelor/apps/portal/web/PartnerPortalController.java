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
package com.axelor.apps.portal.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.db.PortalWorkspace;
import com.axelor.apps.portal.db.repo.PortalWorkspaceRepository;
import com.axelor.apps.portal.service.PartnerMailService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Map;

public class PartnerPortalController {

  public void sendExampleEmailToSelectedPartners(ActionRequest request, ActionResponse response) {
    try {
      @SuppressWarnings("unchecked")
      List<Integer> ids = (List<Integer>) request.getContext().get("_ids");

      if (ids == null || ids.isEmpty()) {
        response.setAlert(I18n.get("Please select at least one partner"));
        return;
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> workspaceMap =
          (Map<String, Object>) request.getContext().get("portalWorkspace");

      if (workspaceMap == null || workspaceMap.get("id") == null) {
        response.setAlert(I18n.get("Please select a workspace"));
        return;
      }

      PortalWorkspace workspace =
          Beans.get(PortalWorkspaceRepository.class)
              .find(Long.valueOf(workspaceMap.get("id").toString()));

      List<Partner> partners =
          Beans.get(PartnerRepository.class)
              .all()
              .filter("self.id IN :ids")
              .bind("ids", ids)
              .fetch();

      if (partners.isEmpty()) {
        response.setAlert(I18n.get("No partners found"));
        return;
      }

      String errors = Beans.get(PartnerMailService.class).sendExampleEmail(partners, workspace);

      if (errors.isEmpty()) {
        response.setInfo(I18n.get("Emails sent successfully"));
      } else {
        response.setAlert(I18n.get("Some emails failed") + ":\n" + errors);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
