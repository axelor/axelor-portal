package com.axelor.apps.portal.mattermost.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.portal.mattermost.service.MattermostPortalService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PartnerController {

  public void createUser(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
      if (ObjectUtils.isEmpty(partner.getMattermostUserId())
          && (partner.getIsCustomer() || partner.getIsContact())) {
        Beans.get(MattermostPortalService.class).createUsers(partner);
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
