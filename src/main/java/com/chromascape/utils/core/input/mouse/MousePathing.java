package com.chromascape.utils.core.input.mouse;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;

/**
 * Utility class to generate human-like mouse paths using cubic Bézier curves. Supports dynamic
 * easing, curved trajectories, and screen-bound clamping for realistic movement simulation.
 *
 * <p>This class is typically used for simulating mouse motion in a way that mimics natural human
 * behavior, making automation less detectable.
 */
public class MousePathing {

  private final Random random = new Random();

  private final DoubleBinaryOperator easeOut = (t, exponent) -> 1 - Math.pow(1 - t, exponent);

  private final int screenWidth;

  private final int screenHeight;

  private final int startX;

  private final int startY;

  /**
   * Constructs a MousePathing utility tied to a given screen or window bounds. The bounds define
   * the coordinate space in which generated points will be clamped.
   *
   * @param bounds the rectangle defining the screen or application window.
   */
  public MousePathing(Rectangle bounds) {
    startX = bounds.x;
    startY = bounds.y;
    this.screenWidth = bounds.width;
    this.screenHeight = bounds.height;
  }

  /**
   * If instantiated as a point it will always stay within the window bounds.
   *
   * @param p The point to clamp.
   * @return The clamped position.
   */
  private Point clampToScreen(final Point p) {
    int x = Math.max(startX, Math.min(p.x, startX + screenWidth - 1));
    int y = Math.max(startY, Math.min(p.y, startY + screenHeight - 1));
    return new Point(x, y);
  }

  /**
   * Generates a list of pixel points forming a cubic Bézier path from a start to end point. The
   * path uses randomized curvature and speed adjustments to simulate human input.
   *
   * @param p0 the starting point (e.g. current mouse position).
   * @param p3 the destination point.
   * @param speed the movement speed profile ("slow", "medium", "fast", "fastest").
   * @return a list of points along the calculated Bézier path.
   */
  public java.util.List<Point> generateCubicBezierPath(
      final Point p0, final Point p3, final String speed) {

    int distance = calculateDistance(p0, p3);
    int steps = calculateSteps(distance, speed);

    // The direction that the mouse will arc (random)
    int direction = random.nextBoolean() ? 1 : -1; // Randomly +1 or -1

    // Calculating the offset inputs based on distance
    int[] originBound = calculateOffset(calculateDistance(p0, p3));

    // Offsetting the points so they're not on the line
    // The first offset (for the first curve) is increased to make the mouse path more varied
    // overall
    double p1offset =
        random.nextInt(originBound[0], originBound[1] + 50) * (random.nextBoolean() ? 1 : -1);
    double p2offset =
        random.nextInt(originBound[0], originBound[1]) * (random.nextBoolean() ? 1 : -1);

    // Apply the perpendicular offset to create final control points, and clamp to screen bounds
    Point p1 = calculatePointAlongPath(p0, p3, 0.2, 0.3, p1offset, direction);
    Point p2 = calculatePointAlongPath(p0, p3, 0.6, 0.7, p2offset, direction);

    // Initialise the list of points
    List<Point> path = new ArrayList<>();

    for (int i = 0; i < steps; i++) {
      double traw = i / (double) (steps - 1);

      // Apply easing to t to simulate more natural speed variation (starts fast, slows down)
      double t = easeOut.applyAsDouble(traw, calculateEasing(distance));
      ;

      // Calculate the cubic Bézier point at parameter t
      double u = 1 - t;
      double bcx =
          Math.pow(u, 3) * p0.x
              + 3 * Math.pow(u, 2) * t * p1.x
              + 3 * u * Math.pow(t, 2) * p2.x
              + Math.pow(t, 3) * p3.x;
      double bcy =
          Math.pow(u, 3) * p0.y
              + 3 * Math.pow(u, 2) * t * p1.y
              + 3 * u * Math.pow(t, 2) * p2.y
              + Math.pow(t, 3) * p3.y;

      // Round to integer pixel coordinates and add to the path
      Point next = new Point((int) Math.round(bcx), (int) Math.round(bcy));
      if (path.isEmpty() || !path.get(path.size() - 1).equals(next)) {
        path.add(next);
      }
    }

    return path;
  }

  /**
   * Creates a point between two different points with an offset perpendicular to the direction
   * vector. locOrigin and locBound refer to how far along the path the point should appear e.g, 0.5
   * is in the centre- Of the two points.
   *
   * @param p0 the start point.
   * @param p3 the end point.
   * @param locOrigin lower bound of the relative path position (0 to 1).
   * @param locBound upper bound of the relative path position (0 to 1).
   * @param pointOffset magnitude of perpendicular distortion.
   * @param direction -1 or 1; determines which direction the curve arcs.
   * @return the calculated Bézier control point.
   */
  public Point calculatePointAlongPath(
      final Point p0,
      final Point p3,
      final double locOrigin,
      final double locBound,
      final double pointOffset,
      final int direction) {

    // Calculate the vector from the start point (p0) to the end point (p3)
    double dx = p3.x - p0.x;
    double dy = p3.y - p0.y;

    // Compute the length (magnitude) of the vector
    double len = Math.sqrt(dx * dx + dy * dy);

    // Compute a unit vector perpendicular to the direction vector (dx, dy)
    // This will be used to offset control points away from the straight line
    double ux = -dy / len;
    double uy = dx / len;

    // Calculate the final perpendicular vector to be applied to control points
    double nx = direction * ux;
    double ny = direction * uy;

    // Picks a random normalized position along the line (t value between 0 and 1)
    // This determines where along the path the control points are placed
    double t1 = random.nextDouble(locOrigin, locBound);

    // Calculating where p1 will be on a straight line
    double p1x = p0.x + t1 * dx;
    double p1y = p0.y + t1 * dy;

    // Apply the perpendicular offset to create final control points, and clamp to screen bounds
    return clampToScreen(new Point((int) (p1x + pointOffset * nx), (int) (p1y + pointOffset * ny)));
  }

  /**
   * Determines curvature offset ranges based on distance to destination. Change this if you want to
   * personalise your mouse.
   *
   * @param distance the pixel distance between start and end points.
   * @return a two-element array: [minimum offset, maximum offset].
   */
  private static int[] calculateOffset(final int distance) {
    if (distance >= 600) {
      return new int[] {100, 140};
    } else if (distance >= 300) {
      return new int[] {50, 60};
    } else if (distance >= 200) {
      return new int[] {10, 30};
    } else if (distance >= 100) {
      return new int[] {5, 10};
    } else if (distance >= 20) {
      return new int[] {3, 5};
    } else {
      return new int[] {2, 4};
    }
  }

  /**
   * Calculates the number of points to generate based on speed and distance. More points = slower
   * and smoother movement. Change this if you want to personalise your mouse.
   *
   * @param distance the pixel distance between points.
   * @param speed one of "slow", "medium", "fast", "fastest".
   * @return the number of interpolation steps.
   */
  private static int calculateSteps(final int distance, final String speed) {
    // tuning factor
    double scale =
        switch (speed) {
          case "slow" -> 2.0;
          case "medium" -> 3.2;
          case "fast" -> 4.0;
          case "fastest" -> 4.5;
          default -> throw new IllegalStateException("Unexpected value: " + speed);
        };

    // divide distance by scale to get step count
    int steps = Math.toIntExact(Math.round(distance / scale));

    // at least 1 step, to avoid zero
    return Math.max(1, steps);
  }

  /**
   * Computes the straight-line pixel distance between two points.
   *
   * @param p0 the start point.
   * @param p3 the end point.
   * @return the integer pixel distance.
   */
  private static int calculateDistance(final Point p0, final Point p3) {
    double vx = p3.x - p0.x;
    double vy = p3.y - p0.y;

    double distance = Math.sqrt(vx * vx + vy * vy);
    return Math.toIntExact(Math.round(distance));
  }

  /**
   * Determines the easing curve exponent based on path distance. A higher exponent causes the mouse
   * to decelerate more sharply near the end.
   *
   * @param distance the distance to travel.
   * @return an exponent to use in the ease-out function.
   */
  private static double calculateEasing(final int distance) {
    if (distance >= 1200) {
      return 16;
    } else if (distance >= 1000) {
      return 14;
    } else if (distance >= 800) {
      return 12;
    } else if (distance >= 600) {
      return 10;
    } else if (distance >= 400) {
      return 8;
    } else if (distance >= 200) {
      return 6;
    } else if (distance >= 100) {
      return 4;
    } else {
      return 2;
    }
  }
}
