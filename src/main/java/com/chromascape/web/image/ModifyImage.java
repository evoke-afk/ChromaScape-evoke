package com.chromascape.web.image;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

import com.chromascape.utils.core.screen.topology.ColourContours;
import com.chromascape.web.slider.CurrentSliderState;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Class responsible for applying modifications to an image based on slider inputs.
 *
 * <p>This class loads an original image from the filesystem, applies colour extraction based on the
 * current slider state, applies the resulting mask to the original image, and saves the modified
 * image back to the filesystem.
 */
public class ModifyImage {

  private static final String ORIGINAL_IMAGE_PATH = "output/original.png";
  private static final String MODIFIED_IMAGE_PATH = "output/modified.png";

  /**
   * Applies modifications to the original image based on the given slider state.
   *
   * <p>The method performs the following steps:
   *
   * <ul>
   *   <li>Loads the original image from disk.
   *   <li>Extracts colour contours using the slider's colour object.
   *   <li>Applies the extracted mask to the original image using {@link
   *       MaskImage#applyMaskToImage(Mat, Mat)}.
   *   <li>Saves the resulting modified image as a PNG file.
   * </ul>
   *
   * @param sliderState the current state of the sliders controlling colour extraction.
   * @throws IOException if the original image file is not found, cannot be read, or if saving the
   *     modified image fails.
   */
  public void applySliderChanges(CurrentSliderState sliderState) throws IOException {
    // Load original image from file system
    File originalFile = new File(ORIGINAL_IMAGE_PATH);
    if (!originalFile.exists()) {
      throw new IOException("Original image file not found at: " + ORIGINAL_IMAGE_PATH);
    }

    BufferedImage originalImage = ImageIO.read(originalFile);
    if (originalImage == null) {
      throw new IOException("Failed to read original image from: " + ORIGINAL_IMAGE_PATH);
    }

    // Apply colour extraction
    try (Mat modifiedMat =
            ColourContours.extractColours(originalImage, sliderState.getColourObj());
        Mat original = imread(ORIGINAL_IMAGE_PATH)) {
      Mat result = MaskImage.applyMaskToImage(original, modifiedMat);
      BufferedImage modifiedImage = Java2DFrameUtils.toBufferedImage(result);

      // Ensure output directory exists
      File outputFile = new File(MODIFIED_IMAGE_PATH);
      File parentDir = outputFile.getParentFile();
      if (!parentDir.exists() && !parentDir.mkdirs()) {
        throw new IOException("Failed to create output directory: " + parentDir.getAbsolutePath());
      }

      ImageIO.write(modifiedImage, "png", outputFile);
    }
  }
}
