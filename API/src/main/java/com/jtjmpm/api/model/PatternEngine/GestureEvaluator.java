package com.jtjmpm.api.model.PatternEngine;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jtjmpm.api.model.PatternEngine.ShapeNormalizer.pathLength;

public class GestureEvaluator {
    private static double evaluateScore(List<Point2D.Double> pattern, List<Point2D.Double> gesture, double k, double windowPercent) {
        if (pattern == null || gesture == null || pattern.isEmpty() || gesture.isEmpty()) {
            return 0.0;
        }

        double patternLen = pathLength(pattern);
        double gestureLen = pathLength(gesture);

        double lengthPenalty = 1.0;
        if (patternLen > 0 && gestureLen > 0) {
            lengthPenalty = Math.min(patternLen, gestureLen) / Math.max(patternLen, gestureLen);
        }

        double dtwCost = calculateDTW(pattern, gesture, windowPercent);
        double normalizedCost = dtwCost / Math.max(pattern.size(), gesture.size());
        double dtwScore = Math.exp(-k * normalizedCost);

        return dtwScore * lengthPenalty;
    }

    public static double evaluateScoreBiDirectional(List<Point2D.Double> pattern, List<Point2D.Double> gesture, double k, double windowPercent) {
        double scoreForward = evaluateScore(pattern, gesture, k, windowPercent);

        List<Point2D.Double> reversedGesture = new ArrayList<>(gesture);
        Collections.reverse(reversedGesture);

        double scoreBackward = evaluateScore(pattern, reversedGesture, k, windowPercent);

        return Math.max(scoreForward, scoreBackward);
    }

    private static double calculateDTW(List<Point2D.Double> pattern, List<Point2D.Double> gesture, double windowPercent) {
        int n = pattern.size();
        int m = gesture.size();
        int window = Math.max((int)(Math.max(n, m) * windowPercent), Math.abs(n - m) + 1);

        double[][] dtw = new double[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dtw[i][j] = Double.POSITIVE_INFINITY;
            }
        }
        dtw[0][0] = 0.0;

        for (int i = 1; i <= n; i++) {
            int startJ = Math.max(1, i - window);
            int endJ = Math.min(m, i + window);

            for (int j = startJ; j <= endJ; j++) {
                double cost = pattern.get(i - 1).distance(gesture.get(j - 1));
                double minPrev = Math.min(dtw[i - 1][j], Math.min(dtw[i][j - 1], dtw[i - 1][j - 1]));
                dtw[i][j] = cost + minPrev;
            }
        }
        return dtw[n][m];
    }
}
