package com.jtjmpm.api.game.game_logic;

import java.util.ArrayList;
import java.util.List;
import com.jtjmpm.Point2D;

public class PatternGenerator {

    public static List<Point2D> createCircle(int pointsCount) {
        List<Point2D> rawPattern = new ArrayList<>();

        if (pointsCount <= 1) return rawPattern;

        for (int i = 0; i < pointsCount; i++) {
            double angle = (2.0 * Math.PI * i) / (pointsCount - 1);
            double x = Math.cos(angle);
            double y = Math.sin(angle);

            rawPattern.add(new Point2D(x, y));
        }

        return ShapeNormalizer.preProcess(rawPattern, pointsCount, 0);
    }

    public static List<Point2D> createFigureEight(int pointsCount) {
        List<Point2D> rawPattern = new ArrayList<>();
        if (pointsCount <= 1) return rawPattern;

        for (int i = 0; i < pointsCount; i++) {
            double angle = (2.0 * Math.PI * i) / (pointsCount - 1);
            double x = Math.sin(2 * angle);
            double y = Math.cos(angle);
            rawPattern.add(new Point2D(x, y));
        }

        return ShapeNormalizer.preProcess(rawPattern, pointsCount, 0);
    }

    public static List<Point2D> createTriangle(int pointsCount) {
        List<Point2D> rawTriangle = new ArrayList<>();

        rawTriangle.add(new Point2D(0, 1));
        rawTriangle.add(new Point2D(-0.866, -0.5));
        rawTriangle.add(new Point2D(0.866, -0.5));
        rawTriangle.add(new Point2D(0, 1));

        return ShapeNormalizer.preProcess(rawTriangle, pointsCount, 0);
    }
}