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
import com.axelor.apps.portal.db.repo.DMSFilePortalRepository;
import com.axelor.apps.portal.db.repo.MailMessagePortalRepository;
import com.axelor.apps.portal.db.repo.ProjectTaskPortalRepository;
import com.axelor.apps.portal.service.MailMessageFileService;
import com.axelor.apps.portal.service.MailMessageFileServiceImpl;
import com.axelor.apps.portal.service.MailMessagePortalService;
import com.axelor.apps.portal.service.MailMessagePortalServiceImpl;
import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.portal.service.SaleOrderPortalServiceImpl;
import com.axelor.apps.portal.service.StockMovePortalServiceImpl;
import com.axelor.apps.production.service.StockMoveServiceProductionImpl;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.mail.db.repo.MailMessageRepository;

public class PortalModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(SaleOrderPortalService.class).to(SaleOrderPortalServiceImpl.class);
    bind(StockMoveServiceProductionImpl.class).to(StockMovePortalServiceImpl.class);
    bind(DMSFileRepository.class).to(DMSFilePortalRepository.class);
    bind(MailMessageRepository.class).to(MailMessagePortalRepository.class);
    bind(ProjectTaskBusinessSupportRepository.class).to(ProjectTaskPortalRepository.class);
    bind(MailMessagePortalService.class).to(MailMessagePortalServiceImpl.class);
    bind(MailMessageFileService.class).to(MailMessageFileServiceImpl.class);
  }
}
