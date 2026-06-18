package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

import java.awt.geom.Point2D;
import java.util.List;

public class RoundStartMessage extends WsMessage {
    public List<Point2D.Double> initialPattern;
    public String initialPatternName;

    public RoundStartMessage(List<Point2D.Double> initialPattern, String initialPatternName) {
        super(MessageType.ROUND_START);
        this.initialPattern = initialPattern;
        this.initialPatternName = initialPatternName;
    }
}
