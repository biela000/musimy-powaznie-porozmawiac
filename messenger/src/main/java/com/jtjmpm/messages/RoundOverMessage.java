package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class RoundOverMessage extends WsMessage {
    public String roundWinnerId;

    public RoundOverMessage(String roundWinnerId) {
        super(MessageType.ROUND_OVER);
        this.roundWinnerId = roundWinnerId;
    }
}
