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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.mail.MessagingException;

public class PartnerMailServiceImpl implements PartnerMailService {

  protected final MessageService messageService;
  protected final TemplateMessageService templateMessageService;

  @Inject
  public PartnerMailServiceImpl(
      MessageService messageService, TemplateMessageService templateMessageService) {
    this.messageService = messageService;
    this.templateMessageService = templateMessageService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void sendEmail(Partner partner, Template template) throws AxelorException {

    if (partner.getEmailAddress() == null
        || partner.getEmailAddress().getAddress() == null
        || partner.getEmailAddress().getAddress().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.PARTNER_NO_EMAIL));
    }

    try {
      Message message = templateMessageService.generateMessage(partner, template);
      messageService.sendByEmail(message);
    } catch (MessagingException | ClassNotFoundException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }
}
