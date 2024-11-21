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
package com.axelor.csv.script;

import com.axelor.apps.portal.db.ForumGroup;
import com.axelor.apps.portal.db.repo.ForumGroupRepository;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.Map;

public class ImportForum {

  @Inject private ForumGroupRepository groupRepo;

  public Object importForumGroup(Object bean, Map<String, Object> values) {
    assert bean instanceof ForumGroup;
    MetaFile metaFile =
        ImportUtils.importFile((String) values.get("image_path"), (Path) values.get("__path__"));
    if (metaFile == null) {
      return bean;
    }

    ForumGroup group = (ForumGroup) bean;
    group.setImage(metaFile);
    return groupRepo.save(group);
  }
}
