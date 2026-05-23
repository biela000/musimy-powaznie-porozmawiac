package com.jtjmpm;

public class ReadyMessage extends WsMessage {
    public ReadyMessage() {
        super("TOGGLE_READY");
    }
}
