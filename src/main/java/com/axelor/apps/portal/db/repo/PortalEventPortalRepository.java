package com.axelor.apps.portal.db.repo;

import com.axelor.apps.portal.db.PortalEvent;
import com.axelor.common.ObjectUtils;
import java.util.UUID;

public class PortalEventPortalRepository extends PortalEventRepository {

  @Override
  public PortalEvent save(PortalEvent event) {
    event = super.save(event);
    if (ObjectUtils.isEmpty(event.getSlug())) {
      event.setSlug(UUID.randomUUID().toString());
    }
    return event;
  }
}
