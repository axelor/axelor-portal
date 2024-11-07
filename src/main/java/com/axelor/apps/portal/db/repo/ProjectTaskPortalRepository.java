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

import com.axelor.apps.base.db.MailMessageFile;
import com.axelor.apps.base.db.repo.MailMessageFileRepository;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskProgressUpdateService;
import com.axelor.apps.businesssupport.db.repo.ProjectTaskBusinessSupportRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;

public class ProjectTaskPortalRepository extends ProjectTaskBusinessSupportRepository {

  @Inject
  public ProjectTaskPortalRepository(
      ProjectTaskProgressUpdateService projectTaskProgressUpdateService) {
    super(projectTaskProgressUpdateService);
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    ProjectTask projectTask = this.find((Long) json.get("id"));

    if (!context.containsKey("_model")) {
      List<MailMessageFile> mailMessageFilePreviewList =
          Beans.get(MailMessageFileRepository.class)
              .all()
              .filter(
                  "self.relatedMailMessage.relatedModel = ?1 and self.relatedMailMessage.relatedId = ?2",
                  ProjectTask.class.getName(),
                  projectTask.getId())
              .fetch();

      json.put("$mailMessageFilePreviewList", mailMessageFilePreviewList);
    }

    return super.populate(json, context);
  }
}
