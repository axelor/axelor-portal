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

import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.production.service.StockMoveServiceProductionImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
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
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderReceiptStateService;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.db.EntityHelper;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import org.apache.commons.collections.CollectionUtils;

public class StockMovePortalServiceImpl extends StockMoveServiceProductionImpl {

  protected StockLocationUtilsService stockLocationUtilsService;
  protected PrintingTemplateService printingTemplateService;
  protected PrintingTemplatePrintService printingTemplatePrintService;

  protected DMSFileRepository dmsFileRepo;
  protected MetaFiles metaFiles;

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
      ProductCompanyService productCompanyService,
      StockLocationService stockLocationService,
      AppSupplychainService appSupplyChainService,
      AppAccountService appAccountService,
      PurchaseOrderRepository purchaseOrderRepo,
      SaleOrderRepository saleOrderRepo,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService,
      PartnerSupplychainService partnerSupplychainService,
      FixedAssetRepository fixedAssetRepository,
      PfpService pfpService,
      SaleOrderConfirmService saleOrderConfirmService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      PurchaseOrderReceiptStateService purchaseOrderReceiptStateService,
      SaleOrderLineRepository saleOrderLineRepository,
      StockLocationUtilsService stockLocationUtilsService,
      PrintingTemplateService printingTemplateService,
      PrintingTemplatePrintService printingTemplatePrintService,
      DMSFileRepository dmsFileRepo,
      MetaFiles metaFiles) {
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
        productCompanyService,
        stockLocationService,
        appSupplyChainService,
        appAccountService,
        purchaseOrderRepo,
        saleOrderRepo,
        unitConversionService,
        reservedQtyService,
        partnerSupplychainService,
        fixedAssetRepository,
        pfpService,
        saleOrderConfirmService,
        stockMoveLineServiceSupplychain,
        purchaseOrderReceiptStateService,
        saleOrderLineRepository);
    this.stockLocationUtilsService = stockLocationUtilsService;
    this.printingTemplateService = printingTemplateService;
    this.printingTemplatePrintService = printingTemplatePrintService;
    this.dmsFileRepo = dmsFileRepo;
    this.metaFiles = metaFiles;
  }

  @Override
  public void plan(StockMove stockMove) throws AxelorException {

    super.plan(stockMove);
    printStockMove(stockMove, true);
  }

  @Override
  public String realize(StockMove stockMove) throws AxelorException {

    String seq = super.realize(stockMove);
    updateProductLeftQty(stockMove);

    DMSFile report =
        dmsFileRepo
            .all()
            .filter(
                "self.relatedId = :stockMoveId AND relatedModel = :model AND COALESCE(isDirectory, false) IS FALSE")
            .bind("stockMoveId", stockMove.getId())
            .bind("model", StockMove.class.getName())
            .order("-createdOn")
            .fetchOne();
    if (report == null) {
      printStockMove(stockMove, true);
    } else {
      File reportFile = printStockMove(stockMove, true);
      updateReport(stockMove, report, reportFile);
    }

    return seq;
  }

  protected File printStockMove(StockMove stockMove, Boolean toAttach) throws AxelorException {

    PrintingGenFactoryContext factoryContext =
        new PrintingGenFactoryContext(EntityHelper.getEntity(stockMove));
    PrintingTemplate printingTemplate =
        printingTemplateService
            .getActivePrintingTemplates(StockMove.class.getName())
            .iterator()
            .next();
    String fileName =
        String.format(
            "%s-%s-%s",
            stockMove.getStockMoveSeq(),
            I18n.get("Customer delivery"),
            Beans.get(AppBaseService.class)
                .getTodayDate(stockMove.getCompany())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd")));

    return printingTemplatePrintService.getPrintFile(
        printingTemplate, factoryContext, fileName, toAttach);
  }

  @Transactional
  public void updateReport(StockMove stockMove, DMSFile report, File reportFile) {
    try {
      report.setMetaFile(metaFiles.upload(reportFile));
      dmsFileRepo.save(report);
    } catch (IOException e) {
    }
  }

  @Transactional
  public void updateProductLeftQty(StockMove stockMove) {

    if (CollectionUtils.isNotEmpty(stockMove.getStockMoveLineList())) {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        try {
          Product product = stockMoveLine.getProduct();
          product.setLeftQty(
              stockLocationUtilsService.getRealQtyOfProductInStockLocations(
                  product.getId(), null, stockMove.getCompany().getId()));
          productRepository.save(product);
        } catch (Exception e) {
        }
      }
    }
  }
}
