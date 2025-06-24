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

import com.axelor.apps.portal.db.Attachment;
import com.axelor.apps.portal.db.PortalNews;
import com.axelor.apps.portal.db.PortalNewsCategory;
import com.axelor.apps.portal.service.NotificationService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import java.util.HashSet;
import java.util.List;

public class PortalNewsPortalRepository extends PortalNewsRepository {

  @Override
  public PortalNews save(PortalNews entity) {

    entity = super.save(entity);

    if (ObjectUtils.notEmpty(entity.getCategorySet())) {
      if (ObjectUtils.isEmpty(entity.getRelatedNewsSet())) {
        List<PortalNews> relatedNews =
            all()
                .filter(
                    "self.id IN (SELECT news.id FROM PortalNews news LEFT JOIN news.categorySet category WHERE category IN :categories) AND self.id != :id")
                .bind("categories", entity.getCategorySet())
                .bind("id", entity.getId())
                .order("-createdOn")
                .fetch(5);
        entity.setRelatedNewsSet(new HashSet<PortalNews>(relatedNews));
      }

      if (entity.getVersion() == 0) {
        for (PortalNewsCategory portalNewsCategory : entity.getCategorySet()) {
          if (portalNewsCategory.getWorkspace() != null) {
            Beans.get(NotificationService.class)
                .notifyUser(
                    "news",
                    portalNewsCategory.getId(),
                    entity.getClass().getName(),
                    portalNewsCategory.getWorkspace());
          }
        }
      }
    }

    return entity;
  }

  @Override
  public PortalNews copy(PortalNews entity, boolean deep) {

    PortalNews copiedNews = super.copy(entity, deep);
    AttachmentRepository attachmentRepo = Beans.get(AttachmentRepository.class);

    copiedNews.clearAttachmentList();
    for (Attachment attachment : entity.getAttachmentList()) {
      Attachment copiedAttachment = attachmentRepo.copy(attachment, deep);
      copiedNews.addAttachmentListItem(copiedAttachment);
    }

    return copiedNews;
  }

  @Override
  public void remove(PortalNews entity) {

    List<PortalNews> relatedNews =
        all().filter(":news MEMBER OF self.relatedNewsSet").bind("news", entity).fetch();
    for (PortalNews relatedNew : relatedNews) {
      System.out.println(relatedNew.getRelatedNewsSet().size());
      relatedNew.removeRelatedNewsSetItem(entity);
      save(relatedNew);
      System.out.println(relatedNew.getRelatedNewsSet().size());
    }

    if (ObjectUtils.notEmpty(entity.getRelatedNewsSet())) {
      entity.clearRelatedNewsSet();
      entity.clearCategorySet();
      save(entity);
    }

    super.remove(entity);
  }
}
