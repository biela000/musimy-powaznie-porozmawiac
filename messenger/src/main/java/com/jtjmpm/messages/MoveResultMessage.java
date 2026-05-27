package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

import java.awt.geom.Point2D;
import java.util.List;

public class MoveResultMessage extends WsMessage {
    public List<Point2D.Double> points;
    public double accuracy;
    public String playerID;

    public MoveResultMessage(List<Point2D.Double> points, double accuracy, String playerID) {
        super(MessageType.MOVE_RESULT);
        this.points = points;
        this.accuracy = accuracy;
        this.playerID = playerID;
    }
}
