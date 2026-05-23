package com.jtjmpm;

public class MoveResultMessage extends WsMessage {
    public float accuracy;

    public MoveResultMessage(float accuracy) {
        super("MOVE_RESULT");
        this.accuracy = accuracy;
    }
}
