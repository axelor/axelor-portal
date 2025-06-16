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

import com.axelor.apps.portal.db.PortalNews;
import com.axelor.apps.portal.db.PortalNewsCategory;
import com.axelor.apps.portal.db.repo.PortalNewsCategoryRepository;
import com.axelor.apps.portal.db.repo.PortalNewsRepository;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.Map;

public class ImportNews {

  @Inject private PortalNewsRepository newsRepo;
  @Inject private PortalNewsCategoryRepository newsCategoryRepo;

  public Object importNews(Object bean, Map<String, Object> values) {
    assert bean instanceof PortalNews;
    MetaFile metaFile =
        ImportUtils.importFile((String) values.get("image_path"), (Path) values.get("__path__"));
    if (metaFile == null) {
      return bean;
    }

    PortalNews news = (PortalNews) bean;
    news.setImage(metaFile);
    return newsRepo.save(news);
  }

  public Object importNewsCategory(Object bean, Map<String, Object> values) {
    assert bean instanceof PortalNewsCategory;
    MetaFile metaFile =
        ImportUtils.importFile((String) values.get("image_path"), (Path) values.get("__path__"));
    if (metaFile == null) {
      return bean;
    }

    PortalNewsCategory newsCategory = (PortalNewsCategory) bean;
    newsCategory.setImage(metaFile);
    return newsCategoryRepo.save(newsCategory);
  }
}
