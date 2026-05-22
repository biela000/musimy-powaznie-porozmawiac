package com.jtjmpm;

public class ReadyMessage extends WsMessage {
    public boolean ready;
    public ReadyMessage(boolean ready) {
        super("PLAYER_READY");
        this.ready = ready;
    }
}
