package com.jtjmpm;

public class LobbyMessage extends WsMessage {
    public String lobbyName;

    public LobbyMessage(String type, String lobbyName) {
        super(type);
        this.lobbyName = lobbyName;
    }
}
