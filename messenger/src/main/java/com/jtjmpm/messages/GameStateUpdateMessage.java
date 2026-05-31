package com.jtjmpm.messages;

import com.jtjmpm.messages.GameStateDTO;
import com.jtjmpm.MessageType;

import java.util.List;

public class GameStateUpdateMessage extends WsMessage{
    public GameStateDTO gameState;
    public List<CombatEventMessage> events;

    public GameStateUpdateMessage(GameStateDTO gameState, List<CombatEventMessage> events) {
        super(MessageType.GAME_STATE_UPDATE);
        this.gameState = gameState;
        this.events = events;
    }
}
