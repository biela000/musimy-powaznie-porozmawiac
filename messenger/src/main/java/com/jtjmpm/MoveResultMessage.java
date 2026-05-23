package com.jtjmpm;

import java.util.List;

public class MoveResultMessage extends WsMessage {
    public List<Point2D> points;
    public double accuracy;

    public MoveResultMessage(List<Point2D> points, double accuracy) {
        super("MOVE_RESULT");
        this.points = points;
        this.accuracy = accuracy;
    }
}
