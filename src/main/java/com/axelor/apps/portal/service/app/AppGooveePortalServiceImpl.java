package com.axelor.apps.portal.service.app;

import com.axelor.db.Query;
import com.axelor.studio.db.AppGooveePortal;

public class AppGooveePortalServiceImpl implements AppGooveePortalService {

  @Override
  public AppGooveePortal getAppGooveePortal() {
    return Query.of(AppGooveePortal.class).fetchOne();
  }
}
