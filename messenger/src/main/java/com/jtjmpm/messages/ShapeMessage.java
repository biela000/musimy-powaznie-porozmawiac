package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

import java.awt.geom.Point2D;
import java.util.List;

public class ShapeMessage extends WsMessage {
    public List<Point2D.Double> points;
    public double accuracy;
    public String shapeName;

    public ShapeMessage(List<Point2D.Double> points, String shapeName) {
        super(MessageType.SHAPE_DRAWN);
        this.points = points;
        this.shapeName = shapeName;
        this.accuracy = 0;
    }
}
