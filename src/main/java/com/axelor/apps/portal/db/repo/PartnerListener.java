package com.axelor.apps.portal.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.inject.Beans;
import java.io.IOException;
import javax.persistence.PreRemove;
import wslite.json.JSONException;

public class PartnerListener {

  @PreRemove
  protected void onPreRemove(Partner partner) throws AxelorException, IOException, JSONException {
    String mattermostUserId = partner.getMattermostUserId();
    if (mattermostUserId != null) {
      Beans.get(MattermostService.class).deleteUser(mattermostUserId);
    }
  }
}
