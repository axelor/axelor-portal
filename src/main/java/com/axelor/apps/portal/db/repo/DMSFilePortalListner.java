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

import com.axelor.apps.portal.db.PortalWorkspace;
import com.axelor.apps.portal.service.DMSFilePortalService;
import com.axelor.apps.portal.service.NotificationService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.dms.db.DMSFile;
import com.axelor.inject.Beans;
import jakarta.persistence.PostPersist;

public class DMSFilePortalListner {

  @PostPersist
  protected void onPostPersist(DMSFile dmsFile) {

    if (dmsFile.getVersion() == 0) {
      if (dmsFile.getAuthor() == null) {
        User user = AuthUtils.getUser();
        if (user != null && user.getActiveCompany() != null) {
          dmsFile.setAuthor(user.getActiveCompany().getPartner());
        }
      }

      if (dmsFile.getParent() != null) {
        Beans.get(DMSFilePortalService.class).assignParentDetails(dmsFile, dmsFile.getParent());

        if (ObjectUtils.notEmpty(dmsFile.getParent().getWorkspaceSet())) {
          for (PortalWorkspace portalWorkspace : dmsFile.getParent().getWorkspaceSet()) {
            Beans.get(NotificationService.class)
                .notifyUser(
                    "resources",
                    dmsFile.getParent().getId(),
                    dmsFile.getClass().getName(),
                    portalWorkspace);
          }
        }
      }
    }
  }
}
