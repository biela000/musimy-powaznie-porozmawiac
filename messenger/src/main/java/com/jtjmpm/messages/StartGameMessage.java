package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

import java.awt.geom.Point2D;
import java.util.List;

public class StartGameMessage extends WsMessage {
    public List<Point2D.Double> initialPattern;
    public String initialPatternName;

    public StartGameMessage() {
        super(MessageType.GAME_START);
    }

    public StartGameMessage(List<Point2D.Double> initialPattern, String initialPatternName) {
        super(MessageType.GAME_START);
        this.initialPattern = initialPattern;
        this.initialPatternName = initialPatternName;
    }
}
