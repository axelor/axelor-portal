package com.axelor.csv.script;

import com.axelor.apps.portal.db.PortalEvent;
import com.axelor.apps.portal.db.repo.PortalEventRepository;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.Map;

public class ImportEvent {

  @Inject private PortalEventRepository eventRepo;

  public Object importEvent(Object bean, Map<String, Object> values) {
    assert bean instanceof PortalEvent;

    MetaFile metaFile =
        ImportUtils.importFile(
            (String) values.get("eventImage_path"), (Path) values.get("__path__"));
    if (metaFile == null) {
      return bean;
    }

    PortalEvent event = (PortalEvent) bean;
    event.setEventImage(metaFile);
    return eventRepo.save(event);
  }
}
