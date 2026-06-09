package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

/**
 * Drives the pre-round countdown on all clients.
 * count = 3, 2, 1 for the ticking numbers.
 * count = 0 means "Battle!" — the round is now live.
 */
public class CountdownMessage extends WsMessage {
    public int count;

    public CountdownMessage(int count) {
        super(MessageType.COUNTDOWN);
        this.count = count;
    }
}
