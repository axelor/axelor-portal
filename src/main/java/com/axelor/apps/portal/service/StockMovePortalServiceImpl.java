package com.axelor.apps.portal.service;

import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.service.StockMoveServiceProductionImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections.CollectionUtils;

public class StockMovePortalServiceImpl extends StockMoveServiceProductionImpl {

  protected StockLocationService stockLocationService;

  @Inject
  public StockMovePortalServiceImpl(
      StockMoveLineService stockMoveLineService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService,
      ProductRepository productRepository,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService,
      AppStockService appStockService,
      AppSupplychainService appSupplyChainService,
      AppAccountService appAccountService,
      PurchaseOrderRepository purchaseOrderRepo,
      SaleOrderRepository saleOrderRepo,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService,
      PartnerSupplychainService partnerSupplychainService,
      FixedAssetRepository fixedAssetRepository,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      PfpService pfpService,
      ProductCompanyService productCompanyService,
      StockLocationService stockLocationService) {
    super(
        stockMoveLineService,
        stockMoveToolService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService,
        productRepository,
        partnerStockSettingsService,
        stockConfigService,
        appStockService,
        appSupplyChainService,
        appAccountService,
        purchaseOrderRepo,
        saleOrderRepo,
        unitConversionService,
        reservedQtyService,
        partnerSupplychainService,
        fixedAssetRepository,
        stockMoveLineServiceSupplychain,
        pfpService,
        productCompanyService);
    this.stockLocationService = stockLocationService;
  }

  @Override
  public String realize(StockMove stockMove) throws AxelorException {

    String seq = super.realize(stockMove);
    updateProductLeftQty(stockMove);
    return seq;
  }

  @Transactional
  public void updateProductLeftQty(StockMove stockMove) {

    if (CollectionUtils.isNotEmpty(stockMove.getStockMoveLineList())) {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        try {
          Product product = stockMoveLine.getProduct();
          product.setLeftQty(
              stockLocationService.getRealQtyOfProductInStockLocations(
                  product.getId(), null, stockMove.getCompany().getId()));
          productRepository.save(product);
        } catch (Exception e) {
        }
      }
    }
  }
}
