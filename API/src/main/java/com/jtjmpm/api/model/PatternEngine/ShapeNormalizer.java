package com.jtjmpm.api.model.PatternEngine;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class ShapeNormalizer {
    public static List<Point2D.Double> preProcess(List<Point2D.Double> rawPoints, int targetPointCount, int trimCount) {
        if (rawPoints == null || rawPoints.size() <= trimCount + 1) {
            return new ArrayList<>();
        }

        List<Point2D.Double> trimmed = trimEnds(rawPoints, trimCount);

        List<Point2D.Double> resampled = resample(trimmed, targetPointCount);

        return normalize(resampled);
    }

    private static List<Point2D.Double> normalize(List<Point2D.Double> points) {
        if (points == null || points.isEmpty()) {
            return points;
        }

        Point2D.Double startPoint = points.getFirst();
        double anchorX = startPoint.getX();
        double anchorY = startPoint.getY();

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point2D.Double p : points) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double scale = Math.max(maxX - minX, maxY - minY);
        if (scale == 0) scale = 1.0;

        List<Point2D.Double> normalizedPoints = new ArrayList<>();
        for (Point2D.Double p : points) {
            double newX = (p.getX() - anchorX) / scale;
            double newY = (p.getY() - anchorY) / scale;
            normalizedPoints.add(new Point2D.Double(newX, newY));
        }

        return normalizedPoints;
    }

    private static List<Point2D.Double> trimEnds(List<Point2D.Double> points, int trimCount) {
        int endIndex = Math.max(1, points.size() - trimCount);
        return new ArrayList<>(points.subList(0, endIndex));
    }

    private static List<Point2D.Double> resample(List<Point2D.Double> points, int n) {
        double interval = pathLength(points) / (n - 1);
        double D = 0.0;

        List<Point2D.Double> newPoints = new ArrayList<>();
        newPoints.add(points.getFirst());

        List<Point2D.Double> workingList = new ArrayList<>(points);

        for (int i = 1; i < workingList.size(); i++) {
            Point2D.Double p1 = workingList.get(i - 1);
            Point2D.Double p2 = workingList.get(i);
            double d = p1.distance(p2);

            if (D + d >= interval) {
                double qx = p1.getX() + ((interval - D) / d) * (p2.getX() - p1.getX());
                double qy = p1.getY() + ((interval - D) / d) * (p2.getY() - p1.getY());
                Point2D.Double q = new Point2D.Double(qx, qy);

                newPoints.add(q);

                workingList.add(i, q);
                D = 0.0;
            } else {
                D += d;
            }
        }

        if (newPoints.size() == n - 1) {
            newPoints.add(points.get(points.size() - 1));
        }

        return newPoints;
    }

    public static double pathLength(List<Point2D.Double> points) {
        double d = 0.0;
        for (int i = 1; i < points.size(); i++) {
            d += points.get(i - 1).distance(points.get(i));
        }
        return d;
    }
}
