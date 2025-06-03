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
    productCompany.setSlug(String.join("-", product.getSlug().concat(slug)));
    return productCompany;
  }
}
