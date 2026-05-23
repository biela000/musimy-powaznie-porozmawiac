package com.jtjmpm;

public class LobbyJoinedMessage extends WsMessage {
    public String lobbyName;
    public GameState gameState;

    public LobbyJoinedMessage(String lobbyName, GameState gameState) {
        super("LOBBY_JOINED");
        this.lobbyName = lobbyName;
        this.gameState = gameState;
    }
}
