package com.axelor.apps.portal.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderDeliveryAddressService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;

public class SaleOrderStockPortalServiceImpl extends SaleOrderStockServiceImpl {

  @Inject
  public SaleOrderStockPortalServiceImpl(
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockConfigService stockConfigService,
      UnitConversionService unitConversionService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      StockMoveLineServiceSupplychain stockMoveLineSupplychainService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRepository,
      AppSupplychainService appSupplychainService,
      SupplyChainConfigService supplyChainConfigService,
      ProductCompanyService productCompanyService,
      PartnerStockSettingsService partnerStockSettingsService,
      TaxService taxService,
      SaleOrderDeliveryAddressService saleOrderDeliveryAddressService) {
    super(
        stockMoveService,
        stockMoveLineService,
        stockConfigService,
        unitConversionService,
        saleOrderLineServiceSupplyChain,
        stockMoveLineSupplychainService,
        stockMoveLineRepository,
        appBaseService,
        saleOrderRepository,
        appSupplychainService,
        supplyChainConfigService,
        productCompanyService,
        partnerStockSettingsService,
        taxService,
        saleOrderDeliveryAddressService);
  }

  @Override
  public StockMove createStockMove(
      SaleOrder saleOrder,
      Company company,
      List<SaleOrderLine> saleOrderLineList,
      String deliveryAddressStr,
      LocalDate estimatedDeliveryDate)
      throws AxelorException {
    StockMove stockMove =
        super.createStockMove(
            saleOrder, company, saleOrderLineList, deliveryAddressStr, estimatedDeliveryDate);
    stockMove.setPortalWorkspace(saleOrder.getPortalWorkspace());
    return stockMove;
  }
}
