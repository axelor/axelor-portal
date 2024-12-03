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
package com.axelor.apps.portal.web.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.print.InvoicePrintService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.inject.Beans;
import java.io.File;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/portal/invoice")
public class InvoiceWebService {

  @GET
  @Path("/print/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response printInvoice(@PathParam("id") Long id) throws AxelorException {

    Beans.get(JpaSecurity.class).check(AccessType.READ, Invoice.class, id);
    Invoice invoice = Beans.get(InvoiceRepository.class).find(id);
    if (invoice == null) {
      return null;
    }

    PrintingTemplate invoicePrintTemplate =
        Beans.get(AccountConfigService.class).getInvoicePrintTemplate(invoice.getCompany());
    File report =
        Beans.get(InvoicePrintService.class)
            .getPrintedInvoice(invoice, true, 4, invoicePrintTemplate, null);
    return Response.ok()
        .entity(report)
        .header("Content-Disposition", "attachment;filename=" + report.getName() + ".pdf")
        .build();
  }
}
