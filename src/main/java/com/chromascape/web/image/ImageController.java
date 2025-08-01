package com.chromascape.web.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to serve image files from the server.
 *
 * <p>Provides endpoints to retrieve the original and modified images as PNG byte arrays. If the
 * requested image is not found in the output directory, a default fallback image from resources is
 * returned.
 */
@RestController
@RequestMapping("/api")
public class ImageController {

  /**
   * Returns the original image as a PNG byte array.
   *
   * <p>Attempts to read the file "output/original.png" from disk. If the file does not exist, falls
   * back to "resources/images/defaultImage/original.png" on the classpath.
   *
   * @return byte array representing the original PNG image.
   * @throws IOException if the file cannot be read.
   */
  @GetMapping(value = "/originalImage", produces = MediaType.IMAGE_PNG_VALUE)
  public @ResponseBody byte[] originalImage() throws IOException {
    File outputFile = new File("output/original.png");
    try (InputStream in =
        outputFile.exists()
            ? new FileInputStream(outputFile)
            : getClass().getResourceAsStream("/images/defaultImage/original.png")) {
      assert in != null;
      return IOUtils.toByteArray(in);
    }
  }

  /**
   * Returns the modified image as a PNG byte array.
   *
   * <p>Attempts to read "output/modified.png" from disk. If it does not exist, attempts to read
   * "output/original.png". If neither exist, returns the fallback image from
   * "resources/images/defaultImage/original.png".
   *
   * @return byte array representing the modified, original, or fallback PNG image.
   * @throws IOException if the file(s) cannot be read.
   */
  @GetMapping(value = "/modifiedImage", produces = MediaType.IMAGE_PNG_VALUE)
  public @ResponseBody byte[] modifiedImage() throws IOException {
    File modifiedFile = new File("output/modified.png");
    File originalFile = new File("output/original.png");
    InputStream in;

    if (modifiedFile.exists()) {
      in = new FileInputStream(modifiedFile);
    } else if (originalFile.exists()) {
      in = new FileInputStream(originalFile);
    } else {
      in = getClass().getResourceAsStream("/images/defaultImage/original.png");
    }

    try (in) {
      assert in != null;
      return IOUtils.toByteArray(in);
    }
  }
}
