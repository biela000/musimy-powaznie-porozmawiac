package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class LobbyJoinedMessage extends WsMessage {
    public String lobbyName;

    public LobbyJoinedMessage(String lobbyName) {
        super(MessageType.LOBBY_JOINED);
        this.lobbyName = lobbyName;
    }
}
