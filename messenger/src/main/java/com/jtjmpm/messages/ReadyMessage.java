package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class ReadyMessage extends WsMessage {
    public ReadyMessage() {
        super(MessageType.TOGGLE_READY);
    }
}
