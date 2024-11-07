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
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.StringUtils;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class MailMessagePortalServiceImpl implements MailMessagePortalService {

  protected MailMessageRepository mailMessageRepo;

  @Inject
  public MailMessagePortalServiceImpl(MailMessageRepository mailMessageRepo) {

    this.mailMessageRepo = mailMessageRepo;
  }

  @Override
  public MailMessage computeMailMessage(ProjectTask projectTask, MailMessage message) {

    message.setAuthor(projectTask.getUpdatedBy());
    message.setRelatedId(projectTask.getId());
    message.setRelatedModel(ProjectTask.class.getName());

    if (message.getId() != null) {
      message.setIsPublicNote(message.getIsPublicNote());
      message.setNote(message.getNote());
    } else {
      message.setIsPublicNote(projectTask.getIsPublicNote());
      message.setNote(projectTask.getNote());
    }

    List<MailMessageFile> mailMessageFileList = projectTask.getMailMessageFileList();

    if (CollectionUtils.isNotEmpty(mailMessageFileList)) {
      mailMessageFileList.forEach(message::addMailMessageFileListItem);
    }

    return message;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createMailMessageWithOnlyAttachment(ProjectTask projectTask) {

    mailMessageRepo.save(computeMailMessage(projectTask, new MailMessage()));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void deleteMailMessage(MailMessage mailMessage) throws JSONException {

    if (StringUtils.isEmpty(mailMessage.getNote())) {
      return;
    }

    List<MailMessageFile> mailMessageFileList = mailMessage.getMailMessageFileList();

    if (CollectionUtils.isEmpty(mailMessageFileList)
        && (!StringUtils.isBlank(mailMessage.getBody())
            || !StringUtils.isBlank(mailMessage.getNote()))) {

      if (StringUtils.isBlank(mailMessage.getBody())
          && !StringUtils.isBlank(mailMessage.getNote())) {
        mailMessageRepo.remove(mailMessage);
        return;
      }

      JSONObject jsonObject = new JSONObject(mailMessage.getBody());
      JSONArray jsonArray = jsonObject.optJSONArray("tracks");

      if (jsonArray != null && jsonArray.length() == 1) {
        JSONObject track = jsonArray.getJSONObject(0);

        if ("comment.note".equals(track.getString("title"))) {
          mailMessageRepo.remove(mailMessage);
          return;
        }
      }
    }

    mailMessage.setNote("");

    mailMessageRepo.save(mailMessage);
  }
}
