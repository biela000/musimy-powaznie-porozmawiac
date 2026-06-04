package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class GameOverMessage extends WsMessage {
    public String winnerId;
    public GameOverReason reason;

    public GameOverMessage(String winnerId, GameOverReason reason) {
        super(MessageType.GAME_OVER);
        this.winnerId = winnerId;
        this.reason = reason;
    }
}