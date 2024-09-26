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

import com.axelor.apps.base.db.Comment;
import com.axelor.apps.project.db.repo.CommentProjectRepository;

public class CommentPortalRepository extends CommentProjectRepository {

  @Override
  public Comment save(Comment entity) {

    entity = super.save(entity);
    setRelatedRecord(entity, entity.getParentComment());
    return entity;
  }

  protected void setRelatedRecord(Comment comment, Comment parent) {

    if (parent == null) {
      return;
    }

    if (parent.getParentComment() == null) {
      if (parent.getForumPost() != null) {
        comment.setForumPost(parent.getForumPost());
      } else if (parent.getPortalEvent() != null) {
        comment.setPortalEvent(parent.getPortalEvent());
      } else if (parent.getPortalNews() != null) {
        comment.setPortalNews(parent.getPortalNews());
      }
      return;
    } else {
      setRelatedRecord(comment, parent);
    }
  }
}
