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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class SaleOrderPortalServiceImpl implements SaleOrderPortalService {

  @Inject ProductPortalService productPortalService;
  @Inject SaleOrderCreateService saleOrdeCreateService;
  @Inject SaleOrderLineService saleOrderLineService;
  @Inject SaleOrderComputeService saleOrderComputeService;
  @Inject UserService userService;

  @Inject PartnerRepository partnerRepo;
  @Inject ProductRepository productRepo;
  @Inject SaleOrderRepository saleOrderRepo;

  @Override
  @Transactional
  public Pair<SaleOrder, Boolean> createQuotation(Map<String, Object> values)
      throws AxelorException {

    Company company = userService.getUserActiveCompany();
    SaleOrder order = createSaleOrder(company, values);
    Boolean isItemsChanged = createOrderLines(company, values, order);
    saleOrderComputeService.computeSaleOrder(order);
    saleOrderRepo.save(order);
    return ImmutablePair.of(order, isItemsChanged);
  }

  private SaleOrder createSaleOrder(Company company, Map<String, Object> values)
      throws AxelorException {

    Partner clientPartner = null, contactPartner = null;
    if (values.containsKey("partnerId") && ObjectUtils.notEmpty(values.get("partnerId"))) {
      clientPartner = partnerRepo.find(Long.parseLong(values.get("partnerId").toString()));
    }

    if (values.containsKey("contactId") && ObjectUtils.notEmpty(values.get("contactId"))) {
      contactPartner = partnerRepo.find(Long.parseLong(values.get("contactId").toString()));
    }

    if (clientPartner == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.CUSTOMER_MISSING));
    } else if (ObjectUtils.isEmpty(clientPartner.getPartnerAddressList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.ADDRESS_MISSING));
    }

    SaleOrder saleOrder =
        saleOrdeCreateService.createSaleOrder(
            userService.getUser(),
            company,
            contactPartner,
            company != null ? company.getCurrency() : null,
            null,
            null,
            null,
            Beans.get(PartnerPriceListService.class)
                .getDefaultPriceList(clientPartner, PriceListRepository.TYPE_SALE),
            clientPartner,
            null,
            null,
            null,
            null);

    if (saleOrder != null) {
      saleOrder.setInAti((Boolean) values.get("inAti"));
    }

    return saleOrder;
  }

  private Boolean createOrderLines(Company company, Map<String, Object> values, SaleOrder order)
      throws AxelorException {

    StockConfigService stockConfigService = Beans.get(StockConfigService.class);
    StockLocation stockLocation =
        stockConfigService.getPickupDefaultStockLocation(
            stockConfigService.getStockConfig(company));
    Boolean isItemsChanged = false;

    if (values.containsKey("items") && ObjectUtils.notEmpty(values.get("items"))) {
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> items = (List<Map<String, Object>>) values.get("items");
      for (Map<String, Object> cartItem : items) {
        Product product = getProduct(cartItem);
        BigDecimal qty = getQty(cartItem);
        BigDecimal price = getPrice(cartItem);
        BigDecimal availableQty = BigDecimal.ZERO;
        if (ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())) {
          availableQty = productPortalService.getAvailableQty(product, company, stockLocation);
          if (qty.compareTo(availableQty) > 0) {
            cartItem.put("quantity", availableQty);
            qty = availableQty;
            isItemsChanged = true;
          }
        }

        SaleOrderLine line = createSaleOrderLine(order, product, qty, price);
        if (cartItem.containsKey("note") && StringUtils.notBlank(cartItem.get("note").toString())) {
          line.setDescription(cartItem.get("note").toString());
        }
        order.addSaleOrderLineListItem(line);
      }
    }

    return isItemsChanged;
  }

  private Product getProduct(Map<String, Object> cartItem) {
    Product product = null;
    if (cartItem.containsKey("productId") && cartItem.get("productId") != null) {
      product = productRepo.find(Long.parseLong(cartItem.get("productId").toString()));
    }
    if (product == null) {
      throw new NotFoundException();
    }
    return product;
  }

  private BigDecimal getQty(Map<String, Object> cartItem) throws AxelorException {

    BigDecimal qty = BigDecimal.ZERO;
    if (cartItem.containsKey("quantity") && cartItem.get("quantity") != null) {
      qty = new BigDecimal(cartItem.get("quantity").toString());
    }
    if (BigDecimal.ZERO.compareTo(qty) >= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(PortalExceptionMessage.QTY_ERROR));
    }
    return qty;
  }

  private BigDecimal getPrice(Map<String, Object> cartItem) {
    BigDecimal price = BigDecimal.ZERO;

    if (cartItem.containsKey("price") && cartItem.get("price") != null) {
      price = new BigDecimal(cartItem.get("price").toString());
    }

    return price;
  }

  private SaleOrderLine createSaleOrderLine(
      SaleOrder order, Product product, BigDecimal qty, BigDecimal price) throws AxelorException {
    SaleOrderLine line = new SaleOrderLine();
    line.setProduct(product);
    line.setQty(qty);
    saleOrderLineService.computeProductInformation(line, order);
    if (BigDecimal.ZERO.compareTo(price) < 0) {
      line.setPrice(price);
    }
    line.setInTaxPrice(line.getInTaxPrice().setScale(10));
    saleOrderLineService.computeValues(order, line);
    return line;
  }
}
