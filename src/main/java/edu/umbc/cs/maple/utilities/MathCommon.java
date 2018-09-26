package edu.umbc.cs.maple.utilities;

import java.util.Random;

public class MathCommon {

    public static double distance(double x1, double y1, double x2, double y2) {

        return Math.hypot(x1 - x2, y1 - y2);

    }

    public static double nextDoubleInRange(Random rng, double min, double max) {
        if (min > max) { throw new RuntimeException("Error: min greater than max"); }
        return min + (max - min) * rng.nextDouble();
    }

}
