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

import com.axelor.common.ObjectUtils;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class DMSFilePortalServiceImpl implements DMSFilePortalService {

  @Inject DMSFileRepository dmsFileRepo;

  @Override
  public void assignParentDetails(DMSFile dmsFile, DMSFile parentFile) {

    if (ObjectUtils.isEmpty(dmsFile) || ObjectUtils.isEmpty(parentFile)) {
      return;
    }

    dmsFile.setAuthor(parentFile.getAuthor());
    dmsFile.setIsPrivate(parentFile.getIsPrivate());
    Optional.ofNullable(parentFile.getWorkspaceSet())
        .ifPresent(set -> set.forEach(dmsFile::addWorkspaceSetItem));
    Optional.ofNullable(parentFile.getPartnerSet())
        .ifPresent(set -> set.forEach(dmsFile::addPartnerSetItem));
    Optional.ofNullable(parentFile.getPartnerCategorySet())
        .ifPresent(set -> set.forEach(dmsFile::addPartnerCategorySetItem));
    dmsFile.setLogoSelect(parentFile.getLogoSelect());
  }

  @Override
  @Transactional
  public void updateChildren(DMSFile dmsFile) {

    dmsFile = dmsFileRepo.find(dmsFile.getId());
    if (ObjectUtils.isEmpty(dmsFile) || ObjectUtils.isEmpty(dmsFile.getChildren())) {
      return;
    }

    dmsFile.getChildren().forEach(child -> assignParentDetails(child, child.getParent()));
    dmsFileRepo.save(dmsFile);
  }
}
