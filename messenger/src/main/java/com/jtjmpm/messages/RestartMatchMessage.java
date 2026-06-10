package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class RestartMatchMessage extends WsMessage {
    public RestartMatchMessage() {
        super(MessageType.RESTART_MATCH);
    }
}
