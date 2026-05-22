package com.jtjmpm.api.game.game_logic;

import java.util.ArrayList;
import java.util.List;

public class ShapeNormalizer {
    public static List<Point2D> preProcess(List<Point2D> rawPoints, int targetPointCount, int trimCount) {
        if (rawPoints == null || rawPoints.size() <= trimCount + 1) {
            return new ArrayList<>();
        }

        List<Point2D> trimmed = trimEnds(rawPoints, trimCount);

        List<Point2D> resampled = resample(trimmed, targetPointCount);

        return normalize(resampled);
    }

    private static List<Point2D> normalize(List<Point2D> points) {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            if (p.x < minX) minX = p.x;
            if (p.x > maxX) maxX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.y > maxY) maxY = p.y;
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;

        double scale = Math.max(maxX - minX, maxY - minY) / 2.0;

        if (scale == 0) scale = 1.0;

        List<Point2D> normalizedPoints = new ArrayList<>();
        for (Point2D p : points) {
            double newX = (p.x - centerX) / scale;
            double newY = (p.y - centerY) / scale;
            normalizedPoints.add(new Point2D(newX, newY));
        }

        return normalizedPoints;
    }

    private static List<Point2D> trimEnds(List<Point2D> points, int trimCount) {
        int endIndex = Math.max(1, points.size() - trimCount);
        return new ArrayList<>(points.subList(0, endIndex));
    }

    private static List<Point2D> resample(List<Point2D> points, int n) {
        double interval = pathLength(points) / (n - 1);
        double D = 0.0;

        List<Point2D> newPoints = new ArrayList<>();
        newPoints.add(points.get(0));

        List<Point2D> workingList = new ArrayList<>(points);

        for (int i = 1; i < workingList.size(); i++) {
            Point2D p1 = workingList.get(i - 1);
            Point2D p2 = workingList.get(i);
            double d = p1.distanceTo(p2);

            if (D + d >= interval) {
                double qx = p1.x + ((interval - D) / d) * (p2.x - p1.x);
                double qy = p1.y + ((interval - D) / d) * (p2.y - p1.y);
                Point2D q = new Point2D(qx, qy);

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

    private static double pathLength(List<Point2D> points) {
        double d = 0.0;
        for (int i = 1; i < points.size(); i++) {
            d += points.get(i - 1).distanceTo(points.get(i));
        }
        return d;
    }
}

