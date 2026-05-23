package com.jtjmpm.api.game.game_logic;

import com.jtjmpm.ControllerRotation;
import com.jtjmpm.Point2D;

import java.util.List;

public class GestureToScore {
    public static double getScore(List<Point2D> pattern, List<ControllerRotation> rawData, double k, double windowPercent) {
        RotationVectorParser parser = new RotationVectorParser();

        List<Point2D> normalPoints = parser.processBatch(rawData);
        List<Point2D> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 6);
        return GestureEvaluator.evaluateScoreBiDirectional(pattern, normalizedPoints, k, windowPercent);
    }

    public static double getScore(List<Point2D> pattern, List<ControllerRotation> rawData) {
        RotationVectorParser parser = new RotationVectorParser();

        List<Point2D> normalPoints = parser.processBatch(rawData);
        List<Point2D> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 6);
        return GestureEvaluator.evaluateScoreBiDirectional(pattern, normalizedPoints, 2.5, 0.20);
    }
}
