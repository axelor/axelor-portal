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

import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportUtils {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static MetaFile importFile(String fileName, Object pathStr) {

    if (!StringUtils.isEmpty(fileName)) {
      final Path path = (Path) pathStr;

      try {
        final File image = path.resolve(fileName).toFile();
        if (image != null && image.isFile()) {
          return Beans.get(MetaFiles.class).upload(image);
        } else {
          LOG.debug(
              "No file found: {}", image == null ? path.toAbsolutePath() : image.getAbsolutePath());
        }
      } catch (Exception e) {
        LOG.error("Error when importing file : {}", e);
      }
    }

    return null;
  }
}
