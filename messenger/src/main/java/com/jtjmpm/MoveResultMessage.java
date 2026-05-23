package com.jtjmpm;

public class MoveResultMessage extends WsMessage {
    public double accuracy;

    public MoveResultMessage(double accuracy) {
        super("MOVE_RESULT");
        this.accuracy = accuracy;
    }
}
