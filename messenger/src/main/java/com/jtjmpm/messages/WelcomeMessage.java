package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class WelcomeMessage extends WsMessage {
    public String myPlayerId;

    public WelcomeMessage(String myPlayerId) {
        super(MessageType.WELCOME);
        this.myPlayerId = myPlayerId;
    }
}
