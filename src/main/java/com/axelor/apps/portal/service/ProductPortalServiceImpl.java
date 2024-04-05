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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ProductPortalServiceImpl implements ProductPortalService {

  protected ProductRepository productRepo;
  protected PartnerRepository partnerRepo;
  protected CompanyRepository companyRepo;

  protected ProductCompanyService productCompanyService;
  protected TaxService taxService;
  protected AccountManagementService accountManagementService;
  protected AppBaseService appBaseService;
  protected AppSaleService appSaleService;
  protected CurrencyService currencyService;

  @Inject
  public ProductPortalServiceImpl(
      ProductRepository productRepo,
      PartnerRepository partnerRepo,
      CompanyRepository companyRepo,
      ProductCompanyService productCompanyService,
      TaxService taxService,
      AccountManagementService accountManagementService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      CurrencyService currencyService) {
    this.productRepo = productRepo;
    this.partnerRepo = partnerRepo;
    this.companyRepo = companyRepo;
    this.productCompanyService = productCompanyService;
    this.taxService = taxService;
    this.accountManagementService = accountManagementService;
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.currencyService = currencyService;
  }

  @Override
  public Map<String, Object> getProductPrices(
      Long productId, Long companyId, Long partnerId, BigDecimal qty) throws AxelorException {

    Product product = null;
    if (productId != null) {
      product = productRepo.find(productId);
    }
    if (product == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.PRODUCT_MISSING));
    }

    Company company = null;
    if (companyId != null) {
      company = companyRepo.find(companyId);
    }
    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.COMPANY_MISSING));
    }

    Partner partner = null;
    if (partnerId != null) {
      partner = partnerRepo.find(partnerId);
    }
    if (partner == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.CUSTOMER_MISSING));
    }

    LocalDate todayDate = appBaseService.getTodayDate(company);
    Boolean productInAti = (Boolean) productCompanyService.get(product, "inAti", company);
    BigDecimal productSalePrice =
        ((BigDecimal) productCompanyService.get(product, "salePrice", company)).multiply(qty);
    TaxLine taxLine =
        accountManagementService.getTaxLine(
            todayDate, product, company, partner.getFiscalPosition(), false);

    BigDecimal basePrice =
        currencyService
            .getAmountCurrencyConvertedAtDate(
                (Currency) productCompanyService.get(product, "saleCurrency", company),
                partner.getCurrency(),
                productSalePrice,
                todayDate)
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    BigDecimal computedPrice =
        taxService.convertUnitPrice(
            productInAti, taxLine, basePrice, appBaseService.getNbDecimalDigitForUnitPrice());

    Map<String, Object> data = new HashMap<>();
    if (productInAti) {
      data.put("price", computedPrice);
      data.put("inTaxPrice", basePrice);
    } else {
      data.put("price", basePrice);
      data.put("inTaxPrice", computedPrice);
    }

    return data;
  }
}
