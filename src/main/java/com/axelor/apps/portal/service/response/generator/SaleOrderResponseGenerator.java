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
package com.axelor.apps.portal.service.response.generator;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class SaleOrderResponseGenerator extends ResponseGenerator {

  @Inject SaleOrderRepository saleOrderRepo;

  @Override
  public void init() {
    modelFields.addAll(
        Arrays.asList(
            "id",
            "saleOrderSeq",
            "clientPartner",
            "confirmationDateTime",
            "creationDate",
            "currency",
            "deliveryAddress",
            "deliveryAddressStr",
            "exTaxTotal",
            "inTaxTotal",
            "mainInvoicingAddress",
            "mainInvoicingAddressStr",
            "orderDate",
            "saleOrderLineList",
            "saleOrderLineTaxList",
            "statusSelect",
            "taxTotal"));
    extraFieldMap.put("_discountTotal", this::getDiscountTotal);
    classType = SaleOrder.class;
  }

  private BigDecimal getDiscountTotal(Object object) {
    SaleOrder order = (SaleOrder) object;
    if (order.getId() != null) {
      order = saleOrderRepo.find(order.getId());
    }
    BigDecimal sum = BigDecimal.ZERO;
    for (SaleOrderLine saleOrderLine : order.getSaleOrderLineList()) {
      BigDecimal totalWTDiscount = saleOrderLine.getPrice().multiply(saleOrderLine.getQty());
      BigDecimal totalInDiscount = saleOrderLine.getExTaxTotal();
      sum = sum.add(totalWTDiscount.subtract(totalInDiscount));
    }
    return sum.setScale(2, RoundingMode.HALF_EVEN);
  }
}
