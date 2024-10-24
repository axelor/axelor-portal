package com.axelor.csv.script;

import com.axelor.apps.portal.db.PortalNewsCategory;
import com.axelor.apps.portal.db.repo.PortalNewsCategoryRepository;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportNewsCategory {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private PortalNewsCategoryRepository newsCategoryRepo;
  @Inject MetaFiles metaFiles;

  public Object importNewsCategory(Object bean, Map<String, Object> values) {
    assert bean instanceof PortalNewsCategory;

    PortalNewsCategory newsCategory = (PortalNewsCategory) bean;
    String fileName = (String) values.get("image_path");

    if (!StringUtils.isEmpty(fileName)) {
      final Path path = (Path) values.get("__path__");

      try {
        final File image = path.resolve(fileName).toFile();
        if (image != null && image.isFile()) {
          final MetaFile metaFile = metaFiles.upload(image);
          newsCategory.setImage(metaFile);
        } else {
          LOG.debug(
              "No image file found: {}",
              image == null ? path.toAbsolutePath() : image.getAbsolutePath());
        }
      } catch (Exception e) {
        LOG.error("Error when importing news category image : {}", e);
      }
    }

    return newsCategoryRepo.save(newsCategory);
  }
}
