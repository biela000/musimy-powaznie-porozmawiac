package com.jtjmpm.api.game.ClassesForMath;

import java.util.ArrayList;
import java.util.List;

public class ShapeNormalizer {
    public static List<Point2D> normalize(List<Point2D> rawPoints){
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MIN_VALUE;
        double maxY = Double.MAX_VALUE;

        for (Point2D p : rawPoints) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;

        double scale = Math.max(maxX - minX, maxY - minY) / 2.0;

        if (scale == 0) scale = 1.0;

        List<Point2D> normalizedPoints = new ArrayList<>();
        for (Point2D p : rawPoints) {
            double newX = (p.getX() - centerX) / scale;
            double newY = (p.getY() - centerY) / scale;
            normalizedPoints.add(new Point2D(newX, newY));
        }

        return normalizedPoints;
    }
}
