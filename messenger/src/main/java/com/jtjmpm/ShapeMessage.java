package com.jtjmpm;

import java.util.List;

public class ShapeMessage extends WsMessage {
    public List<Point2D> points;
    public double accuracy;

    public ShapeMessage(List<Point2D> points, double accuracy) {
        super("SHAPE_DRAWN");
        this.points = points;
        this.accuracy = accuracy;
    }
}
