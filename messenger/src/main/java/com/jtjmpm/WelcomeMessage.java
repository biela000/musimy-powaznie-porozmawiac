package com.jtjmpm;

public class WelcomeMessage extends WsMessage {
    public String myPlayerId;

    public WelcomeMessage(String myPlayerId) {
        super("WELCOME");
        this.myPlayerId = myPlayerId;
    }
}
