package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class WsMessage {
    public final MessageType type;

    public WsMessage(MessageType type) {
        this.type = type;
    }
}
