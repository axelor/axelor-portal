package com.axelor.apps.portal.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.IOException;

public class SaleOrderPortalController {

  public void attachReport(ActionRequest request, ActionResponse response)
      throws AxelorException, IOException {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    Beans.get(SaleOrderPortalService.class).attachReport(EntityHelper.getEntity(saleOrder));
    response.setReload(true);
  }
}
