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

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.portal.db.PartnerPortalWorkspace;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.NotFoundException;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderPortalServiceImpl implements SaleOrderPortalService {

  protected SaleOrderCreateService saleOrdeCreateService;
  protected SaleOrderLineService saleOrderLineService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected AppSaleService appSaleService;
  protected AppBaseService appBaseService;
  protected FiscalPositionService fiscalPositionService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected CurrencyScaleService currencyScaleService;
  protected TaxService taxService;

  protected PartnerRepository partnerRepo;
  protected ProductRepository productRepo;
  protected SaleOrderRepository saleOrderRepo;

  @Inject
  public SaleOrderPortalServiceImpl(
      SaleOrderCreateService saleOrdeCreateService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderComputeService saleOrderComputeService,
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      FiscalPositionService fiscalPositionService,
      SaleOrderMarginService saleOrderMarginService,
      CurrencyScaleService currencyScaleService,
      TaxService taxService,
      PartnerRepository partnerRepo,
      ProductRepository productRepo,
      SaleOrderRepository saleOrderRepo) {
    this.saleOrdeCreateService = saleOrdeCreateService;
    this.saleOrderLineService = saleOrderLineService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.appSaleService = appSaleService;
    this.appBaseService = appBaseService;
    this.fiscalPositionService = fiscalPositionService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.currencyScaleService = currencyScaleService;
    this.taxService = taxService;
    this.partnerRepo = partnerRepo;
    this.productRepo = productRepo;
    this.saleOrderRepo = saleOrderRepo;
  }

  @Override
  @Transactional
  public SaleOrder createQuotation(Map<String, Object> values) throws AxelorException {

    SaleOrder order = createSaleOrder(values);
    createOrderLines(values, order);
    saleOrderComputeService.computeSaleOrder(order);
    saleOrderRepo.save(order);
    return order;
  }

  protected Company getCompany(Partner clientPartner) {

    Company company = AuthUtils.getUser().getActiveCompany();
    if (clientPartner == null
        || clientPartner.getPartnerWorkspaceSet() == null
        || CollectionUtils.isEmpty(clientPartner.getPartnerWorkspaceSet())) {
      return company;
    }

    PartnerPortalWorkspace partnerPortalWorkspace =
        clientPartner.getPartnerWorkspaceSet().stream()
            .filter(pws -> pws.getIsDefaultWorkspaceAfterLogin())
            .findFirst()
            .orElse(
                (PartnerPortalWorkspace)
                    CollectionUtils.get(clientPartner.getPartnerWorkspaceSet(), 0));
    if (partnerPortalWorkspace == null) {
      return company;
    }

    return partnerPortalWorkspace.getPortalAppConfig().getCompany();
  }

  protected SaleOrder createSaleOrder(Map<String, Object> values) throws AxelorException {

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

    Company company = getCompany(clientPartner);
    SaleOrder saleOrder =
        saleOrdeCreateService.createSaleOrder(
            null,
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
            clientPartner.getFiscalPosition(),
            null);

    if (saleOrder != null) {
      saleOrder.setInAti((Boolean) values.get("inAti"));
    }

    return saleOrder;
  }

  protected void createOrderLines(Map<String, Object> values, SaleOrder order)
      throws AxelorException {

    if (!values.containsKey("items") || ObjectUtils.isEmpty(values.get("items"))) {
      return;
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> items = (List<Map<String, Object>>) values.get("items");
    for (Map<String, Object> cartItem : items) {
      SaleOrderLine line = new SaleOrderLine();
      line.setProduct(getProduct(cartItem));
      line.setQty(getQty(cartItem));

      String note = getNote(cartItem);
      if (StringUtils.notBlank(note)) {
        line.setDescription(note);
      } else if (appSaleService.getAppSale().getIsEnabledProductDescriptionCopy()) {
        line.setDescription(line.getProduct().getDescription());
      }
      completeSaleOrderLine(order, line, cartItem);
      order.addSaleOrderLineListItem(line);
    }
  }

  protected Product getProduct(Map<String, Object> cartItem) {

    Product product = null;
    if (cartItem.containsKey("productId") && cartItem.get("productId") != null) {
      product = productRepo.find(Long.parseLong(cartItem.get("productId").toString()));
    }
    if (product == null) {
      throw new NotFoundException();
    }
    return product;
  }

  protected BigDecimal getQty(Map<String, Object> cartItem) throws AxelorException {

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

  protected String getNote(Map<String, Object> cartItem) {

    if (cartItem.containsKey("note") && StringUtils.notBlank(cartItem.get("note").toString())) {
      return cartItem.get("note").toString();
    }

    return null;
  }

  protected void fillPrice(Map<String, Object> cartItem, SaleOrderLine line, SaleOrder order)
      throws AxelorException {

    if (appBaseService.getAppBase().getEnablePricingScale()) {
      saleOrderLineService.computePricingScale(line, order);
    }
    fillTaxInformation(line, order);
    if (cartItem.containsKey("price") && cartItem.get("price") != null) {
      BigDecimal itemPrice = new BigDecimal(cartItem.get("price").toString());
      BigDecimal amount =
          taxService.convertUnitPrice(
              order.getInAti(),
              line.getTaxLineSet(),
              itemPrice,
              appBaseService.getNbDecimalDigitForUnitPrice());
      if (order.getInAti()) {
        line.setInTaxPrice(itemPrice);
        line.setPrice(amount);
      } else {
        line.setInTaxPrice(amount);
        line.setPrice(itemPrice);
      }
    }
    line.setCompanyCostPrice(saleOrderLineService.getCompanyCostPrice(order, line));
  }

  protected void fillTaxInformation(SaleOrderLine line, SaleOrder order) throws AxelorException {

    if (order.getClientPartner() == null) {
      line.setTaxLineSet(Sets.newHashSet());
      line.setTaxEquiv(null);
      return;
    }

    Set<Tax> taxSet = null;
    if (line.getProduct() != null
        && line.getProduct().getProductFamily() != null
        && ObjectUtils.notEmpty(line.getProduct().getProductFamily().getAccountManagementList())) {
      AccountManagement accountManagement =
          line.getProduct().getProductFamily().getAccountManagementList().stream()
              .filter(am -> am.getCompany().equals(order.getCompany()))
              .findFirst()
              .orElse(null);
      if (accountManagement != null && ObjectUtils.notEmpty(accountManagement.getSaleTaxSet())) {
        taxSet = accountManagement.getSaleTaxSet();
        line.setTaxLineSet(taxService.getTaxLineSet(taxSet, order.getCreationDate()));
      }
    }

    TaxEquiv taxEquiv = fiscalPositionService.getTaxEquiv(order.getFiscalPosition(), taxSet);
    line.setTaxEquiv(taxEquiv);
  }

  protected SaleOrderLine completeSaleOrderLine(
      SaleOrder order, SaleOrderLine line, Map<String, Object> cartItem) throws AxelorException {

    line.setProductName(line.getProduct().getName());
    line.setUnit(saleOrderLineService.getSaleUnit(line));
    line.setTypeSelect(SaleOrderLineRepository.TYPE_NORMAL);
    fillPrice(cartItem, line, order);

    saleOrderLineService.fillComplementaryProductList(line);

    BigDecimal taxRate = BigDecimal.ZERO;
    if (ObjectUtils.notEmpty(line.getTaxLineSet())) {
      taxRate = taxService.getTotalTaxRate(line.getTaxLineSet());
    }

    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    if (!order.getInAti()) {
      exTaxTotal =
          saleOrderLineService.computeAmount(
              line.getQty(), line.getPrice(), currencyScaleService.getScale(order));
      inTaxTotal =
          currencyScaleService.getScaledValue(order, exTaxTotal.add(exTaxTotal.multiply(taxRate)));
      companyExTaxTotal = saleOrderLineService.getAmountInCompanyCurrency(exTaxTotal, order);
      companyInTaxTotal =
          currencyScaleService.getCompanyScaledValue(
              order, companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate)));
    } else {
      inTaxTotal =
          saleOrderLineService.computeAmount(
              line.getQty(), line.getInTaxPrice(), currencyScaleService.getScale(order));
      exTaxTotal =
          inTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getScale(order),
              RoundingMode.HALF_UP);
      companyInTaxTotal = saleOrderLineService.getAmountInCompanyCurrency(inTaxTotal, order);
      companyExTaxTotal =
          companyInTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getCompanyScale(order),
              RoundingMode.HALF_UP);
    }

    BigDecimal subTotalCostPrice = BigDecimal.ZERO;
    if (line.getProduct() != null && line.getPrice().compareTo(BigDecimal.ZERO) != 0) {
      subTotalCostPrice =
          currencyScaleService.getCompanyScaledValue(
              order, line.getPrice().multiply(line.getQty()));
    }

    line.setInTaxTotal(inTaxTotal);
    line.setExTaxTotal(exTaxTotal);
    line.setPriceDiscounted(line.getPrice());
    line.setCompanyInTaxTotal(companyInTaxTotal);
    line.setCompanyExTaxTotal(companyExTaxTotal);
    line.setSubTotalCostPrice(subTotalCostPrice);
    saleOrderMarginService.getSaleOrderLineComputedMarginInfo(order, line);

    return line;
  }

  @Override
  @Transactional
  public SaleOrder createOrder(Map<String, Object> values) throws AxelorException {

    SaleOrder order = createSaleOrder(values);
    createOrderLines(values, order);
    saleOrderComputeService.computeSaleOrder(order);
    saleOrderRepo.save(order);

    SaleOrderWorkflowService soWorkflowService = Beans.get(SaleOrderWorkflowService.class);
    soWorkflowService.finalizeQuotation(order);
    soWorkflowService.confirmSaleOrder(order);

    return order;
  }
}
