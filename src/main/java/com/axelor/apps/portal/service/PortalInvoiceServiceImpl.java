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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;

public class PortalInvoiceServiceImpl implements PortalInvoiceService {

  protected InvoiceRepository invoiceRepo;
  protected PaymentModeRepository paymentModeRepo;
  protected InvoicePaymentRepository invoicePaymentRepo;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoicePaymentValidateService invoicePaymentValidateService;

  @Inject
  public PortalInvoiceServiceImpl(
      InvoiceRepository invoiceRepo,
      PaymentModeRepository paymentModeRepo,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoicePaymentValidateService invoicePaymentValidateService) {
    this.invoiceRepo = invoiceRepo;
    this.paymentModeRepo = paymentModeRepo;
    this.invoicePaymentRepo = invoicePaymentRepo;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoicePaymentValidateService = invoicePaymentValidateService;
  }

  @Override
  public InvoicePayment payInvoice(Map<String, Object> values) throws AxelorException {

    Invoice invoice = null;
    if (values.containsKey("invoiceId") && ObjectUtils.notEmpty(values.get("invoiceId"))) {
      invoice = invoiceRepo.find(Long.parseLong(values.get("invoiceId").toString()));
    }

    if (invoice.getStatusSelect() != InvoiceRepository.STATUS_VENTILATED
        || invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.INVALID_INVOICE));
    }

    BigDecimal amount = null;
    if (values.containsKey("paidAmount") && ObjectUtils.notEmpty(values.get("paidAmount"))) {
      amount = new BigDecimal(values.get("paidAmount").toString());
    }

    return createInvoiePayment(invoice, amount);
  }

  @Override
  @Transactional
  public InvoicePayment createInvoiePayment(Invoice invoice, BigDecimal amount)
      throws AxelorException {

    if (invoice == null
        || invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) <= 0
        || (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0)
        || invoice.getAmountRemaining().compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }

    if (amount == null || amount.compareTo(invoice.getAmountRemaining()) > 0) {
      amount = invoice.getAmountRemaining();
    }

    InvoicePayment invoicePayment = new InvoicePayment();
    invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_PAYMENT);
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_DRAFT);

    invoicePayment.setCompanyBankDetails(invoice.getCompanyBankDetails());
    invoicePayment.setCurrency(invoice.getCurrency());
    invoicePayment.setAmount(amount);
    invoicePayment.setPaymentDate(invoice.getInvoiceDate());
    invoicePayment.setPaymentMode(paymentModeRepo.findByCode("IN_WEB"));
    invoicePayment.setTotalAmountWithFinancialDiscount(amount);
    invoicePayment.setMove(invoice.getMove());
    invoiceTermPaymentService.createInvoicePaymentTerms(
        invoicePayment, invoice.getInvoiceTermList());

    invoice.addInvoicePaymentListItem(invoicePayment);
    invoicePaymentRepo.save(invoicePayment);

    try {
      invoicePaymentValidateService.validate(invoicePayment);
    } catch (AxelorException | JAXBException | IOException | DatatypeConfigurationException e) {
      TraceBackService.trace(e);
    }

    return invoicePayment;
  }
}
