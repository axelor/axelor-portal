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
package com.axelor.apps.portal.service.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
public class PortalPricingResponse {

  private Long id;
  private BigDecimal priceWT;
  private BigDecimal priceATI;
  private Long currencyId;
  private String currencyCode;
  private List<PortalPricingResponse> facilityPricingList;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public BigDecimal getPriceWT() {
    return priceWT;
  }

  public void setPriceWT(BigDecimal priceWT) {
    this.priceWT = priceWT;
  }

  public BigDecimal getPriceATI() {
    return priceATI;
  }

  public void setPriceATI(BigDecimal priceATI) {
    this.priceATI = priceATI;
  }

  public Long getCurrencyId() {
    return currencyId;
  }

  public void setCurrencyId(Long currencyId) {
    this.currencyId = currencyId;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public List<PortalPricingResponse> getFacilityPricingList() {
    return facilityPricingList;
  }

  public void setFacilityPricingList(List<PortalPricingResponse> facilityPricingList) {
    this.facilityPricingList = facilityPricingList;
  }

  public void addFacilityPricingListItem(PortalPricingResponse item) {
    if (getFacilityPricingList() == null) {
      setFacilityPricingList(new ArrayList<>());
    }
    getFacilityPricingList().add(item);
  }
}
