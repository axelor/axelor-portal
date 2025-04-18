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
package com.axelor.apps.portal.db.repo;

import com.axelor.apps.portal.db.PortalCmsSite;
import com.axelor.common.ObjectUtils;
import java.util.UUID;

public class PortalCmsSitePortalRepository extends PortalCmsSiteRepository {

  @Override
  public PortalCmsSite save(PortalCmsSite portalCmsSite) {

    portalCmsSite = super.save(portalCmsSite);
    String slug = portalCmsSite.getName().replaceAll("\\s+", "-");
    if (ObjectUtils.isEmpty(portalCmsSite.getSlug())) {
      slug = UUID.randomUUID().toString();
    }
    portalCmsSite.setSlug(slug);

    return portalCmsSite;
  }
}
