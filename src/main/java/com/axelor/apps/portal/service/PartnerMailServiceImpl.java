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
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.exception.PortalExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.MessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import javax.mail.MessagingException;

public class PartnerMailServiceImpl implements PartnerMailService {

  protected final MessageService messageService;
  protected final MessageRepository messageRepository;
  protected final EmailAccountRepository emailAccountRepository;

  @Inject
  public PartnerMailServiceImpl(
      MessageService messageService,
      MessageRepository messageRepository,
      EmailAccountRepository emailAccountRepository) {
    this.messageService = messageService;
    this.messageRepository = messageRepository;
    this.emailAccountRepository = emailAccountRepository;
  }

  @Override
  public String sendExampleEmail(List<Partner> partners) throws AxelorException {
    EmailAccount emailAccount =
        emailAccountRepository.all().filter("self.isDefault = true").fetchOne();

    if (emailAccount == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PortalExceptionMessage.NO_DEFAULT_EMAIL_ACCOUNT));
    }

    StringBuilder errors = new StringBuilder();

    for (Partner partner : partners) {
      if (partner.getEmailAddress() == null
          || partner.getEmailAddress().getAddress() == null
          || partner.getEmailAddress().getAddress().isEmpty()) {
        continue;
      }

      try {
        sendEmailToPartner(partner, emailAccount);
      } catch (Exception e) {
        errors.append(partner.getName()).append("\n");
        TraceBackService.trace(e);
      }
    }

    return errors.toString();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void sendEmailToPartner(Partner partner, EmailAccount emailAccount)
      throws MessagingException {
    Message message = new Message();
    message.setMediaTypeSelect(MessageRepository.MEDIA_TYPE_EMAIL);
    message.setReplyToEmailAddressSet(new HashSet<>());
    message.setCcEmailAddressSet(new HashSet<>());
    message.setBccEmailAddressSet(new HashSet<>());
    message.addToEmailAddressSetItem(partner.getEmailAddress());
    message.setSenderUser(AuthUtils.getUser());
    message.setSubject("Example email from Portal");
    message.setContent(
        "Hello " + partner.getName() + ", this is an example email from the Portal module.");
    message.setMailAccount(emailAccount);

    message = messageRepository.save(message);
    messageService.sendByEmail(message);
  }
}
