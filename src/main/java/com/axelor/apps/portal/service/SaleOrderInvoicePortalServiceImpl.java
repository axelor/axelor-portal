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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.invoice.InvoiceToolBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceTaxService;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.apps.supplychain.service.order.OrderInvoiceService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SaleOrderInvoicePortalServiceImpl extends SaleOrderBudgetServiceImpl {

  @Inject
  public SaleOrderInvoicePortalServiceImpl(
      AppBaseService appBaseService,
      AppStockService appStockService,
      AppSupplychainService appSupplychainService,
      SaleOrderRepository saleOrderRepo,
      InvoiceRepository invoiceRepo,
      InvoiceServiceSupplychainImpl invoiceService,
      StockMoveRepository stockMoveRepository,
      SaleOrderWorkflowService saleOrderWorkflowService,
      InvoiceTermService invoiceTermService,
      CommonInvoiceService commonInvoiceService,
      InvoiceLineOrderService invoiceLineOrderService,
      SaleInvoicingStateService saleInvoicingStateService,
      CurrencyScaleService currencyScaleService,
      OrderInvoiceService orderInvoiceService,
      InvoiceTaxService invoiceTaxService,
      AppBusinessProjectService appBusinessProjectService,
      AppBudgetService appBudgetService,
      BudgetDistributionService budgetDistributionService,
      SaleOrderLineBudgetService saleOrderLineBudgetService,
      BudgetService budgetService,
      BudgetToolsService budgetToolsService,
      InvoiceToolBudgetService invoiceToolBudgetService) {
    super(
        appBaseService,
        appStockService,
        appSupplychainService,
        saleOrderRepo,
        invoiceRepo,
        invoiceService,
        stockMoveRepository,
        saleOrderWorkflowService,
        invoiceTermService,
        commonInvoiceService,
        invoiceLineOrderService,
        saleInvoicingStateService,
        currencyScaleService,
        orderInvoiceService,
        invoiceTaxService,
        appBusinessProjectService,
        appBudgetService,
        budgetDistributionService,
        saleOrderLineBudgetService,
        budgetService,
        budgetToolsService,
        invoiceToolBudgetService);
  }

  @Override
  public Invoice createInvoice(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLineList,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    Invoice invoice = super.createInvoice(saleOrder, saleOrderLineList, qtyToInvoiceMap);
    if (invoice != null) {
      invoice.setPortalWorkspace(saleOrder.getPortalWorkspace());
    }

    return invoice;
  }

  @Override
  public Invoice createInvoiceAndLines(
      SaleOrder saleOrder,
      List<SaleOrderLineTax> taxLineList,
      Product invoicingProduct,
      BigDecimal percentToInvoice,
      int operationSubTypeSelect,
      Account partnerAccount)
      throws AxelorException {

    Invoice invoice =
        super.createInvoiceAndLines(
            saleOrder,
            taxLineList,
            invoicingProduct,
            percentToInvoice,
            operationSubTypeSelect,
            partnerAccount);
    if (invoice != null) {
      invoice.setPortalWorkspace(saleOrder.getPortalWorkspace());
    }

    return invoice;
  }
}
