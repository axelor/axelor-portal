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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.inject.Beans;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.tuple.Pair;

@Path("/portal/orders")
public class SaleOrderWebService extends AbstractWebService {

  @POST
  @Path("/quotation")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PortalRestResponse createQuotation(Map<String, Object> values) throws AxelorException {

    try {
      Beans.get(JpaSecurity.class).check(AccessType.CREATE, SaleOrder.class);
      Pair<SaleOrder, Boolean> saleOrder =
          Beans.get(SaleOrderPortalService.class).createQuotation(values);

      Map<String, Object> data =
          ResponseGeneratorFactory.of(SaleOrder.class.getName()).generate(saleOrder.getLeft());
      data.put("itemsChanged", saleOrder.getRight());

      PortalRestResponse response = new PortalRestResponse();
      return response.setData(data).success();
    } catch (Exception e) {
      PortalRestResponse response = new PortalRestResponse();
      response.setException(e);
      return response.fail();
    }
  }
}
