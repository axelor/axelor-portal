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

import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.apps.portal.service.response.generator.SaleOrderResponseGenerator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

public class ResponseGeneratorFactory {

  private static final Map<String, Class<? extends ResponseGenerator>> CUSTOMISED_MODELS =
      new HashMap<>();

  static {
    Map<String, Class<? extends ResponseGenerator>> map =
        new ImmutableMap.Builder<String, Class<? extends ResponseGenerator>>()
            .put(SaleOrder.class.getName(), SaleOrderResponseGenerator.class)
            .build();
    CUSTOMISED_MODELS.putAll(map);
  }

  private ResponseGeneratorFactory() {
    throw new IllegalStateException("ResponseGeneratorFactory class");
  }

  public static ResponseGenerator of(String modelConcerned) {

    if (!isValid(modelConcerned)) {
      return null;
    }

    return Beans.get(CUSTOMISED_MODELS.get(modelConcerned));
  }

  public static boolean isValid(String modelConcerned) {
    return StringUtils.notBlank(modelConcerned) && CUSTOMISED_MODELS.containsKey(modelConcerned);
  }
}
