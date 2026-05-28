package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class StartGameMessage extends WsMessage {
    public StartGameMessage() {
        super(MessageType.GAME_START);
    }
}
