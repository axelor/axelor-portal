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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.apps.portal.service.ProductPortalService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/portal/products")
public class ProductWebService {

  @SuppressWarnings("unchecked")
  @GET
  @Path("/productPrices")
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetchProductPrices(Map<String, Object> values) throws AxelorException {

    Long partnerId = Long.parseLong(values.get("partnerId").toString());
    Long companyId = Long.parseLong(values.get("companyId").toString());
    String taxSelect = values.get("taxSelect").toString();

    List<Object> products = (List<Object>) values.get("productIds");
    List<Long> productIds =
        products.stream().map(p -> Long.parseLong(p.toString())).collect(Collectors.toList());

    try {
      if (ObjectUtils.isEmpty(productIds)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(PortalExceptionMessage.PRODUCT_LIST_EMPTY));
      }

      Beans.get(JpaSecurity.class)
          .check(AccessType.READ, Product.class, productIds.toArray(new Long[productIds.size()]));
      List<Map<String, Object>> data =
          Beans.get(ProductPortalService.class)
              .getProductPrices(productIds, companyId, partnerId, taxSelect);
      PortalRestResponse response = new PortalRestResponse();
      return response.setData(data).success();

    } catch (Exception e) {
      PortalRestResponse response = new PortalRestResponse();
      response.setException(e);
      return response.fail();
    }
  }
}
