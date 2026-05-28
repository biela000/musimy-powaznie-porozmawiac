package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class LobbyMessage extends WsMessage {
    public String lobbyName;

    public LobbyMessage(MessageType type, String lobbyName) {
        super(type);
        this.lobbyName = lobbyName;
    }
}
