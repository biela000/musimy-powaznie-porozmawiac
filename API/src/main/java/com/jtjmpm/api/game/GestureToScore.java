package com.jtjmpm.api.game;

import com.jtjmpm.api.game.game_logic.GestureEvaluator;
import com.jtjmpm.api.game.game_logic.Point2D;
import com.jtjmpm.api.game.game_logic.RotationVectorParser;
import com.jtjmpm.api.game.game_logic.ShapeNormalizer;

import java.util.List;

public class GestureToScore {
    public static double getScore(List<Point2D> pattern, List<float[]> rawData, double k, double windowPercent) {
        RotationVectorParser parser = new RotationVectorParser();

        List<Point2D> normalPoints = parser.processBatch(rawData);
        List<Point2D> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 3);
        return GestureEvaluator.evaluateScoreBiDirectional(pattern, normalizedPoints, k, windowPercent);
    }

    public static double getScore(List<Point2D> pattern, List<float[]> rawData) {
        RotationVectorParser parser = new RotationVectorParser();

        List<Point2D> normalPoints = parser.processBatch(rawData);
        List<Point2D> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 3);
        return GestureEvaluator.evaluateScoreBiDirectional(pattern, normalizedPoints, 3.5, 0.15);
    }
}
