package com.jtjmpm;

import java.awt.geom.Point2D;
import java.util.List;

public class PlayerMoveResult {
    public List<Point2D.Double> points;
    public double accuracy;

    public PlayerMoveResult(List<Point2D.Double> points, double accuracy) {
        this.points = points;
        this.accuracy = accuracy;
    }
}
