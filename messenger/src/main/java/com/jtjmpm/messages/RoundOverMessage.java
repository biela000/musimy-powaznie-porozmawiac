package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

/**
 * Broadcast when a round ends (a player's HP reaches 0).
 * roundWinnerId is null when both players die simultaneously (draw round).
 */
public class RoundOverMessage extends WsMessage {
    public String roundWinnerId;

    public RoundOverMessage(String roundWinnerId) {
        super(MessageType.ROUND_OVER);
        this.roundWinnerId = roundWinnerId;
    }
}
