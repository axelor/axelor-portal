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
package com.axelor.apps.portal.web.service;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.service.PortalEventRegistrationService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.inject.Beans;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/portal/event")
public class EventWebService {

  @POST
  @Path("/price")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetchrEventPrice(Map<String, Object> values) {

    try {
      return new PortalRestResponse()
          .setData(Beans.get(PortalEventRegistrationService.class).fetchEventPricing(values))
          .success();
    } catch (Exception e) {
      TraceBackService.trace(e);
      PortalRestResponse response = new PortalRestResponse();
      response.setException(e);
      return response.fail();
    }
  }
}
