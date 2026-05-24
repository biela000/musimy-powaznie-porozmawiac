package com.jtjmpm;

public class LobbyJoinedMessage extends WsMessage {
    public String lobbyName;

    public LobbyJoinedMessage(String lobbyName) {
        super("LOBBY_JOINED");
        this.lobbyName = lobbyName;
    }
}
