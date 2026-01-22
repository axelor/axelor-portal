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

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.MailMessageFile;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.service.MailMessagePortalService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.db.MetaFile;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class MailMessagePortalRepository extends MailMessageRepository {

  @Override
  public MailMessage save(MailMessage entity) {

    setRelatedRecord(entity, entity.getParentMailMessage());

    if (entity.getRelatedModel() != null
        && ProjectTask.class.getName().equals(entity.getRelatedModel())) {

      try {
        ProjectTask projectTask =
            Beans.get(ProjectTaskRepository.class).find(entity.getRelatedId());
        entity = Beans.get(MailMessagePortalService.class).computeMailMessage(projectTask, entity);
        clearProjectTaskTempFields(projectTask);
      } catch (Exception e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
        throw new PersistenceException(e.getMessage(), e);
      }
    }

    return super.save(entity);
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    MailMessage mailMessage = find((Long) json.get("id"));

    List<MailMessageFile> mailMessageFileList = mailMessage.getMailMessageFileList();

    StringBuilder sb = new StringBuilder();

    if (CollectionUtils.isNotEmpty(mailMessageFileList)) {
      String baseURL = AppSettings.get().getBaseURL();
      String urlFormat = "%s/ws/rest/com.axelor.meta.db.MetaFile/%d/content/download?v=%d";

      for (MailMessageFile mailMessageFile : mailMessageFileList) {
        MetaFile attachmentFile = mailMessageFile.getAttachmentFile();

        if (attachmentFile != null) {
          sb.append("<li> ")
              .append("<a href='")
              .append(
                  String.format(
                      urlFormat, baseURL, attachmentFile.getId(), attachmentFile.getVersion()))
              .append("'>")
              .append(attachmentFile.getFileName())
              .append("</a></li>");
        }
      }
    }

    json.put("$mailMessageFiles", sb.toString());

    return super.populate(json, context);
  }

  @Override
  public void remove(MailMessage entity) {

    List<MailMessage> mailMessageList =
        all()
            .filter(
                "self.id != ?1 and self.relatedModel = ?2 and self.relatedId =?3",
                entity.getId(),
                entity.getRelatedModel(),
                entity.getRelatedId())
            .fetch();

    if (CollectionUtils.isNotEmpty(mailMessageList)) {
      mailMessageList.stream()
          .filter(mailMessage -> entity.equals(mailMessage.getParentMailMessage()))
          .peek(mailMessage -> mailMessage.setParentMailMessage(null))
          .forEach(this::save);
    }

    super.remove(entity);
  }

  protected void clearProjectTaskTempFields(ProjectTask projectTask) {

    projectTask.setNote("");
    projectTask.clearMailMessageFileList();
  }

  protected void setRelatedRecord(MailMessage mailMessage, MailMessage parent) {

    if (parent == null) {
      return;
    }

    if (parent.getParentMailMessage() == null) {
      mailMessage.setRelatedId(parent.getRelatedId());
      mailMessage.setRelatedModel(parent.getRelatedModel());
      return;
    } else {
      setRelatedRecord(mailMessage, parent);
    }
  }
}
