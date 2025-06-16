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

import com.axelor.apps.base.db.MailMessageFile;
import com.axelor.apps.base.db.repo.MailMessageFileRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MailMessageFileServiceImpl implements MailMessageFileService {

  protected MetaFileRepository metaFileRepo;
  protected MailMessageFileRepository mailMessageFileRepo;
  protected MailMessageRepository mailMessageRepo;
  protected ProjectTaskRepository projectTaskRepo;

  @Inject
  public MailMessageFileServiceImpl(
      MetaFileRepository metaFileRepo,
      MailMessageFileRepository mailMessageFileRepo,
      MailMessageRepository mailMessageRepo,
      ProjectTaskRepository projectTaskRepo) {

    this.metaFileRepo = metaFileRepo;
    this.mailMessageFileRepo = mailMessageFileRepo;
    this.mailMessageRepo = mailMessageRepo;
    this.projectTaskRepo = projectTaskRepo;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void deleteMailMessageFile(MailMessageFile mailMessageFile) {

    MetaFile attachmentFile = mailMessageFile.getAttachmentFile();

    if (attachmentFile != null) {
      ProjectTask projectTask =
          projectTaskRepo.find(mailMessageFile.getRelatedMailMessage().getRelatedId());
      String body = "<ul><li>" + "(<del>" + attachmentFile.getFileName() + "</del>)" + "</li></ul>";

      MailMessage message = new MailMessage();
      message.setBody(body);
      message.setRelatedId(projectTask.getId());
      message.setRelatedModel(ProjectTask.class.getName());
      message.setAuthor(projectTask.getUpdatedBy());

      mailMessageRepo.save(message);
      metaFileRepo.remove(attachmentFile);
      mailMessageFileRepo.remove(mailMessageFile);
    }
  }
}
