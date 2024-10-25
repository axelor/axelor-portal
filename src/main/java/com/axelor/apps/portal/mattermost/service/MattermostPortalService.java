package com.axelor.apps.portal.mattermost.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.portal.db.PortalContactWorkspaceConfig;

public interface MattermostPortalService {

  void createUsers(Partner customer);

  void updateChatAccessForContacts(PortalContactWorkspaceConfig portalContactWorkspaceConfig)
      throws AxelorException;
}
