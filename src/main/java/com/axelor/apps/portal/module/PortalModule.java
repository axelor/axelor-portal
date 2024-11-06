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
import com.axelor.apps.mattermost.mattermost.service.MattermostServiceImpl;
import com.axelor.apps.portal.db.repo.DMSFilePortalRepository;
import com.axelor.apps.portal.db.repo.MailMessagePortalRepository;
import com.axelor.apps.portal.mattermost.service.MattermostPortalService;
import com.axelor.apps.portal.mattermost.service.MattermostPortalServiceImpl;
import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.portal.service.SaleOrderPortalServiceImpl;
import com.axelor.apps.portal.service.StockMovePortalServiceImpl;
import com.axelor.apps.production.service.StockMoveServiceProductionImpl;
import com.axelor.apps.project.db.repo.MailMessageProjectRepository;
import com.axelor.dms.db.repo.DMSFileRepository;

public class PortalModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(SaleOrderPortalService.class).to(SaleOrderPortalServiceImpl.class);
    bind(StockMoveServiceProductionImpl.class).to(StockMovePortalServiceImpl.class);

    bind(DMSFileRepository.class).to(DMSFilePortalRepository.class);
    bind(MailMessageProjectRepository.class).to(MailMessagePortalRepository.class);
    bind(MattermostPortalService.class).to(MattermostPortalServiceImpl.class);
    bind(MattermostServiceImpl.class).to(MattermostPortalServiceImpl.class);
  }
}
