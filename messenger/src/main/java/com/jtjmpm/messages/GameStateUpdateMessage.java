package com.jtjmpm.messages;

import com.jtjmpm.messages.GameStateDTO;
import com.jtjmpm.MessageType;

public class GameStateUpdateMessage extends WsMessage{
    public GameStateDTO gameState;
    public GameStateUpdateMessage(GameStateDTO gameState) {
        super(MessageType.GAME_STATE_UPDATE);
        this.gameState = gameState;
    }
}
