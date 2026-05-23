package com.jtjmpm;

public class GameStateUpdateMessage extends WsMessage{
    GameState gameState;
    public GameStateUpdateMessage(GameState gameState) {
        super("GAME_STATE_UPDATE");
        this.gameState = gameState;
    }
}
