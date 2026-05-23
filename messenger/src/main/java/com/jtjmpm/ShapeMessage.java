package com.jtjmpm;

import java.util.List;

public class ShapeMessage extends WsMessage {
    public List<Point2D> points;

    public ShapeMessage(List<Point2D> points) {
        super("SHAPE");
        this.points = points;
    }
}
