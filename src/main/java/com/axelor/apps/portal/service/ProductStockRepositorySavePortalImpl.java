/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.repo.product.ProductStockRepositorySave;
import com.axelor.meta.db.MetaField;
import com.google.inject.Inject;
import java.util.Set;

public class ProductStockRepositorySavePortalImpl extends ProductStockRepositorySave {

  @Inject
  public ProductStockRepositorySavePortalImpl(AppBaseService appBaseService) {
    super(appBaseService);
  }

  @Override
  protected ProductCompany createProductCompany(
      Product product, Set<MetaField> specificProductFieldSet, Company company) {

    ProductCompany productCompany =
        super.createProductCompany(product, specificProductFieldSet, company);
    String slug =
        company.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
    productCompany.setSlug(String.join("-", product.getSlug(), slug));
    return productCompany;
  }
}
