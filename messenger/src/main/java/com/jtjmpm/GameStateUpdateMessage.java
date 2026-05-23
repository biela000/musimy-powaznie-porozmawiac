package com.jtjmpm;

public class GameStateUpdateMessage extends WsMessage{
    public GameState gameState;
    public GameStateUpdateMessage(GameState gameState) {
        super("GAME_STATE_UPDATE");
        this.gameState = gameState;
    }
}
