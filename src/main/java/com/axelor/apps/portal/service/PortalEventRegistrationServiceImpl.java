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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.portal.db.PartnerPortalWorkspace;
import com.axelor.apps.portal.db.PortalAppConfig;
import com.axelor.apps.portal.db.PortalEvent;
import com.axelor.apps.portal.db.PortalEventFacility;
import com.axelor.apps.portal.db.PortalParticipant;
import com.axelor.apps.portal.db.PortalWorkspace;
import com.axelor.apps.portal.db.Registration;
import com.axelor.apps.portal.db.repo.PartnerPortalWorkspaceRepository;
import com.axelor.apps.portal.db.repo.PortalEventRepository;
import com.axelor.apps.portal.db.repo.RegistrationRepository;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.apps.portal.service.app.AppGooveePortalService;
import com.axelor.apps.portal.service.response.PortalPricingResponse;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalEventRegistrationServiceImpl implements PortalEventRegistrationService {

  protected RegistrationRepository registrationRepo;
  protected PartnerPortalWorkspaceRepository partnerPortalWorkspaceRepo;
  protected InvoiceRepository invoiceRepo;
  protected PortalEventRepository portalEventRepo;
  protected PartnerRepository partnerRepo;
  protected CurrencyRepository currencyRepo;
  protected TaxService taxService;
  protected PartnerService partnerService;
  protected InvoiceService invoiceService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceLineService invoiceLineService;
  protected AddressService addressService;
  protected AppBaseService appBaseService;
  protected ProductCompanyService productCompanyService;
  protected CurrencyService currencyService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected AppGooveePortalService appGooveePortalService;
  protected TemplateMessageService templateMessageService;
  protected PortalInvoiceService portalInvoiceService;

  @Inject
  public PortalEventRegistrationServiceImpl(
      RegistrationRepository registrationRepo,
      PartnerPortalWorkspaceRepository partnerPortalWorkspaceRepo,
      InvoiceRepository invoiceRepo,
      PortalEventRepository portalEventRepo,
      PartnerRepository partnerRepo,
      CurrencyRepository currencyRepo,
      TaxService taxService,
      PartnerService partnerService,
      InvoiceService invoiceService,
      InvoiceTermService invoiceTermService,
      InvoiceLineService invoiceLineService,
      AddressService addressService,
      AppBaseService appBaseService,
      ProductCompanyService productCompanyService,
      CurrencyService currencyService,
      AccountManagementAccountService accountManagementAccountService,
      AppGooveePortalService appGooveePortalService,
      TemplateMessageService templateMessageService,
      PortalInvoiceService portalInvoiceService) {
    this.registrationRepo = registrationRepo;
    this.partnerPortalWorkspaceRepo = partnerPortalWorkspaceRepo;
    this.invoiceRepo = invoiceRepo;
    this.portalEventRepo = portalEventRepo;
    this.partnerRepo = partnerRepo;
    this.currencyRepo = currencyRepo;
    this.taxService = taxService;
    this.partnerService = partnerService;
    this.invoiceService = invoiceService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceLineService = invoiceLineService;
    this.addressService = addressService;
    this.appBaseService = appBaseService;
    this.productCompanyService = productCompanyService;
    this.currencyService = currencyService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.appGooveePortalService = appGooveePortalService;
    this.templateMessageService = templateMessageService;
    this.portalInvoiceService = portalInvoiceService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Invoice createEventInvoice(Map<String, Object> values) throws AxelorException {

    Registration registration = null;
    if (values.containsKey("registrationId")
        && ObjectUtils.notEmpty(values.get("registrationId"))) {
      registration = registrationRepo.find(Long.parseLong(values.get("registrationId").toString()));
    }
    PartnerPortalWorkspace partnerWorkspace = null;
    if (values.containsKey("partnerWorkspaceId")
        && ObjectUtils.notEmpty(values.get("partnerWorkspaceId"))) {
      partnerWorkspace =
          partnerPortalWorkspaceRepo.find(
              Long.parseLong(values.get("partnerWorkspaceId").toString()));
    }
    Currency currency = null;
    if (values.containsKey("currencyCode") && ObjectUtils.notEmpty(values.get("currencyCode"))) {
      currency = currencyRepo.findByCode(values.get("currencyCode").toString());
    }

    if (registration == null || registration.getEvent() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.EVENT_MISSING));
    }
    if (partnerWorkspace == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.WORKSPACE_MISSING));
    }
    if (ObjectUtils.isEmpty(registration.getParticipantList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.PARTICIPANT_MISSING));
    }

    PortalParticipant participant =
        registration.getParticipantList().stream()
            .min((p1, p2) -> Integer.compare(p1.getSequence(), p2.getSequence()))
            .get();
    if (participant == null || participant.getContact() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.CUSTOMER_MISSING));
    }

    PortalAppConfig portalAppConfig = partnerWorkspace.getPortalAppConfig();
    if (portalAppConfig == null || portalAppConfig.getCompany() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.COMPANY_MISSING));
    }

    if (registration.getInvoice() != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.INVOICE_EXISTS));
    }

    Invoice invoice =
        createInvoice(
            participant, registration, portalAppConfig, currency, partnerWorkspace.getWorkspace());
    registration.setInvoice(invoice);
    registrationRepo.save(registration);

    sendRegistrationNotification(registration);

    return invoice;
  }

  protected Invoice createInvoice(
      PortalParticipant participant,
      Registration registration,
      PortalAppConfig portalAppConfig,
      Currency currency,
      PortalWorkspace workspace)
      throws AxelorException {

    Company company = portalAppConfig.getCompany();
    Partner partner = participant.getContact();
    String addressStr =
        ObjectUtils.notEmpty(participant.getCompany())
            ? participant.getCompany()
            : Stream.of(participant.getName(), participant.getSurname())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" "));

    Invoice invoice = new Invoice();
    setInvoiceDetails(invoice, company, partner, portalAppConfig, currency, addressStr);
    createInvoiceLines(
        invoice, partner, company, portalAppConfig.getDefaultEventProduct(), registration);
    invoiceService.compute(invoice);
    invoiceTermService.computeInvoiceTerms(invoice);

    invoice.setPortalWorkspace(workspace);
    invoiceRepo.save(invoice);

    invoiceService.validate(invoice);
    invoiceService.ventilate(invoice);
    portalInvoiceService.createInvoiePayment(invoice, null);

    return invoice;
  }

  protected Invoice setInvoiceDetails(
      Invoice invoice,
      Company company,
      Partner partner,
      PortalAppConfig portalAppConfig,
      Currency currency,
      String addressStr) {

    invoice.setOperationTypeSelect(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    invoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_DEFAULT);
    invoice.setStatusSelect(InvoiceRepository.STATUS_DRAFT);

    invoice.setCompany(company);
    invoice.setPartner(partner);

    if (currency == null) {
      currency = partner.getCurrency() != null ? partner.getCurrency() : company.getCurrency();
    }
    invoice.setCurrency(currency);

    invoice.setCompanyBankDetails(company.getDefaultBankDetails());
    invoice.setPrintingSettings(company.getPrintingSettings());

    Address address = partnerService.getInvoicingAddress(partner);
    if (address == null) {
      address = partner.getMainAddress();
      if (address == null && ObjectUtils.notEmpty(partner.getPartnerAddressList())) {
        address = partner.getPartnerAddressList().get(0).getAddress();
      }
    }
    if (address == null) {
      address = company.getAddress();
    }

    invoice.setAddressStr(addressStr);
    invoice.setAddress(address);
    invoice.setPartnerTaxNbr(partner.getTaxNbr());
    invoice.setBankDetails(getPartnerBankDetails(partner));
    invoice.setPaymentCondition(
        partner.getPaymentCondition() != null
            ? partner.getPaymentCondition()
            : portalAppConfig.getDefaultEventPaymentCondition());
    invoice.setPaymentMode(
        partner.getInPaymentMode() != null
            ? partner.getInPaymentMode()
            : portalAppConfig.getDefaultEventPaymentMode());
    invoice.setFiscalPosition(partner.getFiscalPosition());

    return invoice;
  }

  protected BankDetails getPartnerBankDetails(Partner partner) {

    if (ObjectUtils.notEmpty(partner.getBankDetailsList())) {
      Optional<BankDetails> optBankDetails =
          partner.getBankDetailsList().stream()
              .filter(bankDetails -> bankDetails.getActive())
              .findFirst();
      if (optBankDetails.isPresent()) {
        return optBankDetails.get();
      }
    }

    return null;
  }

  protected Invoice createInvoiceLines(
      Invoice invoice,
      Partner partner,
      Company company,
      Product defaultProduct,
      Registration registration)
      throws AxelorException {

    Integer sequence = 0;
    PortalEvent portalEvent = registration.getEvent();
    createInvoiceLine(
        invoice,
        partner,
        company,
        portalEvent.getEventProduct(),
        sequence,
        new BigDecimal(registration.getParticipantList().size()),
        portalEvent.getDefaultPrice());

    for (PortalEventFacility item : portalEvent.getFacilityList()) {
      BigDecimal qty =
          new BigDecimal(
              registration.getParticipantList().stream()
                  .filter(p -> p.getSubscriptionSet().contains(item))
                  .count());
      createInvoiceLine(
          invoice, partner, company, item.getProduct(), ++sequence, qty, item.getPrice());
    }
    try {
      invoiceLineService.updateLinesAfterFiscalPositionChange(invoice);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return invoice;
  }

  protected InvoiceLine createInvoiceLine(
      Invoice invoice,
      Partner partner,
      Company company,
      Product product,
      Integer sequence,
      BigDecimal qty,
      BigDecimal priceWT)
      throws AxelorException {

    priceWT = priceWT.multiply(qty);
    InvoiceLine invoiceLine = new InvoiceLine();

    invoiceLine.setTypeSelect(InvoiceLineRepository.TYPE_NORMAL);
    invoiceLine.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    invoiceLine.setSequence(sequence);
    invoiceLine.setQty(qty);

    invoiceLine.setCompanyCurrency(company.getCurrency());
    invoiceLine.setCompanyExTaxTotal(priceWT);
    invoiceLine.setExTaxTotal(priceWT);
    invoiceLine.setPrice(priceWT);
    invoiceLine.setPriceDiscounted(priceWT);
    invoiceLine.setInTaxPrice(priceWT);
    invoiceLine.setInTaxTotal(priceWT);
    setProductDetails(invoice, invoiceLine, product);
    invoice.addInvoiceLineListItem(invoiceLine);

    return invoiceLine;
  }

  protected InvoiceLine setProductDetails(Invoice invoice, InvoiceLine invoiceLine, Product product)
      throws AxelorException {

    if (product == null) {
      return invoiceLine;
    }

    invoiceLine.setProduct(product);
    invoiceLine.setProductName(product.getName());
    invoiceLine.setProductCode(product.getCode());
    invoiceLine.setUnit(product.getUnit());

    LocalDate todayDate = appBaseService.getTodayDate(invoice.getCompany());
    Set<TaxLine> taxLineSet =
        accountManagementAccountService.getTaxLineSet(
            todayDate,
            product,
            invoice.getCompany(),
            invoice.getPartner().getFiscalPosition(),
            false);

    Currency fromCurrency =
        (Currency) productCompanyService.get(product, "saleCurrency", invoice.getCompany());
    if (fromCurrency == null) {
      fromCurrency = invoice.getCompany().getCurrency();
    }
    Currency toCurrency = invoice.getCurrency();

    BigDecimal invoicePriceWT =
        currencyService
            .getAmountCurrencyConvertedAtDate(
                fromCurrency, toCurrency, invoiceLine.getPrice(), todayDate)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    invoiceLine.setExTaxTotal(invoicePriceWT);
    invoiceLine.setPrice(invoicePriceWT);
    invoiceLine.setPriceDiscounted(invoicePriceWT);
    invoiceLine.setTaxCode(taxService.computeTaxCode(taxLineSet));

    return invoiceLine;
  }

  @Override
  public PortalPricingResponse fetchEventPricing(Map<String, Object> values)
      throws AxelorException {

    if (ObjectUtils.isEmpty(values)) {
      return null;
    }

    Long eventId = null;
    PortalEvent event = null;
    if (values.containsKey("eventId") && ObjectUtils.notEmpty(values.get("eventId"))) {
      eventId = Long.parseLong(values.get("eventId").toString());
      Beans.get(JpaSecurity.class).check(AccessType.READ, PortalEvent.class, eventId);
      event = portalEventRepo.find(eventId);
    }
    if (event == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.EVENT_MISSING));
    }

    PartnerPortalWorkspace partnerWorkspace = null;
    if (values.containsKey("partnerWorkspaceId")
        && ObjectUtils.notEmpty(values.get("partnerWorkspaceId"))) {
      partnerWorkspace =
          partnerPortalWorkspaceRepo.find(
              Long.parseLong(values.get("partnerWorkspaceId").toString()));
    }
    if (partnerWorkspace == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.WORKSPACE_MISSING));
    }

    Partner partner = null;
    if (values.containsKey("partnerId") && ObjectUtils.notEmpty(values.get("partnerId"))) {
      partner = partnerRepo.find(Long.parseLong(values.get("partnerId").toString()));
    }

    Company company = partnerWorkspace.getPortalAppConfig().getCompany();

    Currency toCurrency = company.getCurrency();
    FiscalPosition fiscalPosition = null;
    if (partner != null) {
      toCurrency = partner.getCurrency();
      fiscalPosition = partner.getFiscalPosition();
    }

    LocalDate todayDate = appBaseService.getTodayDate(company);

    PortalPricingResponse response =
        fetchProductPrices(
            eventId,
            event.getEventProduct(),
            event.getDefaultPrice(),
            company,
            toCurrency,
            fiscalPosition,
            todayDate);
    for (PortalEventFacility item : event.getFacilityList()) {
      response.addFacilityPricingListItem(
          fetchProductPrices(
              item.getId(),
              item.getProduct(),
              item.getPrice(),
              company,
              toCurrency,
              fiscalPosition,
              todayDate));
    }

    return response;
  }

  protected PortalPricingResponse fetchProductPrices(
      Long id,
      Product product,
      BigDecimal price,
      Company company,
      Currency toCurrency,
      FiscalPosition fiscalPosition,
      LocalDate todayDate)
      throws AxelorException {

    PortalPricingResponse response = new PortalPricingResponse();

    Currency fromCurrency = null;
    Set<TaxLine> taxLineSet = new HashSet<TaxLine>();
    if (product != null) {
      taxLineSet =
          accountManagementAccountService.getTaxLineSet(
              todayDate, product, company, fiscalPosition, false);

      fromCurrency = (Currency) productCompanyService.get(product, "saleCurrency", company);
      if (fromCurrency == null) {
        fromCurrency = company.getCurrency();
      }
    }

    BigDecimal priceWT =
        currencyService
            .getAmountCurrencyConvertedAtDate(fromCurrency, toCurrency, price, todayDate)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    BigDecimal priceATI =
        currencyService
            .getAmountCurrencyConvertedAtDate(
                fromCurrency,
                toCurrency,
                taxService.convertUnitPrice(
                    false, taxLineSet, price, appBaseService.getNbDecimalDigitForUnitPrice()),
                todayDate)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    response.setId(id);
    response.setPriceWT(priceWT);
    response.setPriceATI(priceATI);
    if (toCurrency != null) {
      response.setCurrencyCode(toCurrency.getCodeISO());
      response.setCurrencyId(toCurrency.getId());
    }
    return response;
  }

  protected void sendRegistrationNotification(Registration registration) {

    Template template = appGooveePortalService.getAppGooveePortal().getRegistrationTemplate();
    if (template == null) {
      return;
    }

    try {
      templateMessageService.generateAndSendMessage(registration, template);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
