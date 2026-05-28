package com.jtjmpm.api.model.PatternEngine;

import com.jtjmpm.ControllerRotation;

import java.awt.geom.Point2D;
import java.util.List;

public class GestureToScore {
    public static double getScore(List<Point2D.Double> pattern, List<ControllerRotation> rawData, double k, double windowPercent) {
        RotationVectorParser parser = new RotationVectorParser();

        List<Point2D.Double> normalPoints = parser.processBatch(rawData);
        List<Point2D.Double> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 6);
        return GestureEvaluator.evaluateScoreBiDirectional(pattern, normalizedPoints, k, windowPercent);
    }

    public static double getScore(List<Point2D.Double> pattern, List<ControllerRotation> rawData) {
        RotationVectorParser parser = new RotationVectorParser();

        List<Point2D.Double> normalPoints = parser.processBatch(rawData);
        List<Point2D.Double> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 4);
        return GestureEvaluator.evaluateScoreBiDirectional(pattern, normalizedPoints, 2.75, 0.15);
    }
}
