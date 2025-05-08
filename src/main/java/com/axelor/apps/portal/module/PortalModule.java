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
package com.axelor.apps.portal.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businesssupport.db.repo.ProjectTaskBusinessSupportRepository;
import com.axelor.apps.helpdesk.service.MailServiceHelpDeskImpl;
import com.axelor.apps.mattermost.mattermost.service.MattermostServiceImpl;
import com.axelor.apps.portal.db.repo.ForumPostPortalRepository;
import com.axelor.apps.portal.db.repo.ForumPostRepository;
import com.axelor.apps.portal.db.repo.MailMessagePortalRepository;
import com.axelor.apps.portal.db.repo.PortalCmsPagePortalRepository;
import com.axelor.apps.portal.db.repo.PortalCmsPageRepository;
import com.axelor.apps.portal.db.repo.PortalCmsSitePortalRepository;
import com.axelor.apps.portal.db.repo.PortalCmsSiteRepository;
import com.axelor.apps.portal.db.repo.PortalEventPortalRepository;
import com.axelor.apps.portal.db.repo.PortalEventRepository;
import com.axelor.apps.portal.db.repo.PortalNewsPortalRepository;
import com.axelor.apps.portal.db.repo.PortalNewsRepository;
import com.axelor.apps.portal.db.repo.ProjectTaskPortalRepository;
import com.axelor.apps.portal.db.repo.RegistrationPortalRepository;
import com.axelor.apps.portal.db.repo.RegistrationRepository;
import com.axelor.apps.portal.mattermost.service.MattermostPortalService;
import com.axelor.apps.portal.mattermost.service.MattermostPortalServiceImpl;
import com.axelor.apps.portal.service.DMSFilePortalService;
import com.axelor.apps.portal.service.DMSFilePortalServiceImpl;
import com.axelor.apps.portal.service.MailMessageFileService;
import com.axelor.apps.portal.service.MailMessageFileServiceImpl;
import com.axelor.apps.portal.service.MailMessagePortalService;
import com.axelor.apps.portal.service.MailMessagePortalServiceImpl;
import com.axelor.apps.portal.service.MailServicePortalImpl;
import com.axelor.apps.portal.service.NotificationService;
import com.axelor.apps.portal.service.NotificationServiceImpl;
import com.axelor.apps.portal.service.PortalEventRegistrationService;
import com.axelor.apps.portal.service.PortalEventRegistrationServiceImpl;
import com.axelor.apps.portal.service.PortalInvoiceService;
import com.axelor.apps.portal.service.PortalInvoiceServiceImpl;
import com.axelor.apps.portal.service.ProjectTaskLinkPortalServiceImpl;
import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.portal.service.SaleOrderPortalServiceImpl;
import com.axelor.apps.portal.service.SaleOrderStockPortalServiceImpl;
import com.axelor.apps.portal.service.StockMovePortalServiceImpl;
import com.axelor.apps.portal.service.app.AppGooveePortalService;
import com.axelor.apps.portal.service.app.AppGooveePortalServiceImpl;
import com.axelor.apps.production.service.StockMoveServiceProductionImpl;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockServiceImpl;
import com.axelor.mail.db.repo.MailMessageRepository;

public class PortalModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(SaleOrderPortalService.class).to(SaleOrderPortalServiceImpl.class);
    bind(StockMoveServiceProductionImpl.class).to(StockMovePortalServiceImpl.class);
    bind(MailMessageRepository.class).to(MailMessagePortalRepository.class);
    bind(ProjectTaskBusinessSupportRepository.class).to(ProjectTaskPortalRepository.class);
    bind(PortalNewsRepository.class).to(PortalNewsPortalRepository.class);
    bind(PortalEventRepository.class).to(PortalEventPortalRepository.class);
    bind(RegistrationRepository.class).to(RegistrationPortalRepository.class);
    bind(ForumPostRepository.class).to(ForumPostPortalRepository.class);
    bind(PortalCmsPageRepository.class).to(PortalCmsPagePortalRepository.class);
    bind(PortalCmsSiteRepository.class).to(PortalCmsSitePortalRepository.class);
    bind(MailMessagePortalService.class).to(MailMessagePortalServiceImpl.class);
    bind(MailMessageFileService.class).to(MailMessageFileServiceImpl.class);
    bind(MailServiceHelpDeskImpl.class).to(MailServicePortalImpl.class);
    bind(ProjectTaskLinkServiceImpl.class).to(ProjectTaskLinkPortalServiceImpl.class);
    bind(MattermostPortalService.class).to(MattermostPortalServiceImpl.class);
    bind(MattermostServiceImpl.class).to(MattermostPortalServiceImpl.class);
    bind(SaleOrderStockServiceImpl.class).to(SaleOrderStockPortalServiceImpl.class);
    bind(PortalEventRegistrationService.class).to(PortalEventRegistrationServiceImpl.class);
    bind(AppGooveePortalService.class).to(AppGooveePortalServiceImpl.class);
    bind(PortalInvoiceService.class).to(PortalInvoiceServiceImpl.class);
    bind(NotificationService.class).to(NotificationServiceImpl.class);
    bind(DMSFilePortalService.class).to(DMSFilePortalServiceImpl.class);
  }
}
