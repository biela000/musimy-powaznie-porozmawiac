package com.jtjmpm;

public class LobbyErrorMessage extends WsErrorMessage{
    public LobbyErrorMessage(String description) {
        super("LOBBY_ERROR", description);
    }
}
