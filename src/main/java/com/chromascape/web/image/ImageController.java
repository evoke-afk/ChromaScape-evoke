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
 * <p>Provides endpoints to retrieve the original and modified images from the local filesystem as
 * PNG byte arrays.
 */
@RestController
@RequestMapping("/api")
public class ImageController {

  /**
   * Returns the original image as a PNG byte array.
   *
   * <p>Reads the file "output/original.png" from disk and streams its content as the response body
   * with the MIME type "image/png".
   *
   * @return byte array representing the original PNG image.
   * @throws IOException if the file cannot be read.
   */
  @GetMapping(value = "/originalImage", produces = MediaType.IMAGE_PNG_VALUE)
  public @ResponseBody byte[] originalImage() throws IOException {
    File outputFile = new File("output/original.png");
    try (InputStream in = new FileInputStream(outputFile)) {
      return IOUtils.toByteArray(in);
    }
  }

  /**
   * Returns the modified image as a PNG byte array if it exists, otherwise returns the original
   * image.
   *
   * <p>Checks if "output/modified.png" exists. If it does, streams its content as the response
   * body. Otherwise, falls back to returning "output/original.png".
   *
   * @return byte array representing the modified PNG image if available, or the original image
   *     otherwise.
   * @throws IOException if the file(s) cannot be read.
   */
  @GetMapping(value = "/modifiedImage", produces = MediaType.IMAGE_PNG_VALUE)
  public @ResponseBody byte[] modifiedImage() throws IOException {
    File modifiedFile = new File("output/modified.png");
    File outputFile = new File("output/original.png");
    try (InputStream in =
        modifiedFile.exists()
            ? new FileInputStream(modifiedFile)
            : new FileInputStream(outputFile)) {
      return IOUtils.toByteArray(in);
    }
  }
}
