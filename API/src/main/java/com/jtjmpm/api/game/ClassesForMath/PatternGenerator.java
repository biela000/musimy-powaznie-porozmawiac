package com.jtjmpm.api.game.ClassesForMath;

import java.util.ArrayList;
import java.util.List;

public class PatternGenerator {
    public static List<Point2D> createCirclePattern(int numberOfPoints) {
        List<Point2D> pattern = new ArrayList<>();
        for (int i = 0; i < numberOfPoints; i++) {
            double angle = 2 * Math.PI * i / numberOfPoints;
            double x = Math.cos(angle);
            double y = Math.sin(angle);
            pattern.add(new Point2D(x, y));
        }
        return pattern;
    }
}

// chwilowo tylko okrag, potem sie pomysli o reszcie
