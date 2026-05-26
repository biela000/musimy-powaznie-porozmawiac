package com.jtjmpm.messages;

import com.jtjmpm.GameState;
import com.jtjmpm.MessageType;

public class GameStateUpdateMessage extends WsMessage{
    public GameState gameState;
    public GameStateUpdateMessage(GameState gameState) {
        super(MessageType.GAME_STATE_UPDATE);
        this.gameState = gameState;
    }
}
