package com.axelor.csv.script;

import com.axelor.apps.base.db.ProductPicture;
import com.axelor.apps.base.db.repo.ProductPictureRepository;
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

public class ImportProductPicture {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ProductPictureRepository productPictureRepo;
  @Inject MetaFiles metaFiles;

  public Object importProductPicture(Object bean, Map<String, Object> values) {
    assert bean instanceof ProductPicture;

    ProductPicture productPicture = (ProductPicture) bean;
    String fileName = (String) values.get("picture_path");

    if (!StringUtils.isEmpty(fileName)) {
      final Path path = (Path) values.get("__path__");

      try {
        final File image = path.resolve(fileName).toFile();
        if (image != null && image.isFile()) {
          final MetaFile metaFile = metaFiles.upload(image);
          productPicture.setPicture(metaFile);
        } else {
          LOG.debug(
              "No image file found: {}",
              image == null ? path.toAbsolutePath() : image.getAbsolutePath());
        }
      } catch (Exception e) {
        LOG.error("Error when importing product picture : {}", e);
      }
    }

    return productPictureRepo.save(productPicture);
  }
}
