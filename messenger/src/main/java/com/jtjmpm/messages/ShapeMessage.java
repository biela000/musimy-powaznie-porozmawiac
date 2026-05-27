package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

import java.util.List;

public class ShapeMessage extends WsMessage {
    public List<Point2D> points;
    public double accuracy;

    public ShapeMessage(List<Point2D> points, double accuracy) {
        super(MessageType.SHAPE_DRAWN);
        this.points = points;
        this.accuracy = accuracy;
    }
}
