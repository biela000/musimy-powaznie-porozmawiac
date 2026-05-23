package com.jtjmpm;

import java.util.List;

public class GameStateUpdateMessage extends WsMessage{
    public GameState gameState;
    public GameStateUpdateMessage(GameState gameState) {
        super("GAME_STATE_UPDATE");
        this.gameState = gameState;
    }
}
