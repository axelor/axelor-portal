/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.utils;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class ImageUtils {

  protected static final float QUALITY = 0.5f;
  protected static final float SIZE_RATIO = 0.7f;

  @CallMethod
  public MetaFile createThumbnail(MetaFile metaFile) {

    try {
      BufferedImage originalImage = getOriginalFile(metaFile);
      if (originalImage == null) {
        return null;
      }

      File thumbnailImage = File.createTempFile("thumbnailImage", ".jpg");
      BufferedImage resizedImage = resizeImage(originalImage);

      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
      ImageWriter writer = writers.next();

      FileImageOutputStream outputStream = new FileImageOutputStream(thumbnailImage);
      writer.setOutput(outputStream);

      ImageWriteParam param = writer.getDefaultWriteParam();
      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      param.setCompressionQuality(QUALITY);

      writer.write(null, new IIOImage(resizedImage, null, null), param);

      outputStream.close();
      writer.dispose();

      return Beans.get(MetaFiles.class).upload(thumbnailImage);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return null;
  }

  protected BufferedImage getOriginalFile(MetaFile metaFile) throws IOException {

    if (metaFile == null) {
      return null;
    }

    Path path = MetaFiles.getPath(metaFile);
    if (!isImage(path)) {
      return null;
    }

    return ImageIO.read(path.toFile());
  }

  protected BufferedImage resizeImage(BufferedImage originalImage) {

    int newHeight = (int) (originalImage.getHeight() * (SIZE_RATIO));
    int newWidth = (int) (originalImage.getWidth() * (SIZE_RATIO));
    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = resizedImage.createGraphics();
    g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
    g2d.dispose();

    return resizedImage;
  }

  protected boolean isImage(Path path) {

    try {
      String mimeType = Files.probeContentType(path);
      return mimeType != null && mimeType.startsWith("image/");
    } catch (IOException e) {
    }

    return false;
  }
}
