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

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;

public class DMSFilePortalRepository extends DMSFileRepository {

  @Override
  public DMSFile save(DMSFile entity) {

    entity = super.save(entity);

    if (entity.getVersion() == 0 && entity.getAuthor() == null) {
      User user = AuthUtils.getUser();
      if (user != null && user.getActiveCompany() != null) {
        entity.setAuthor(user.getActiveCompany().getPartner());
      }
    }

    return entity;
  }
}
