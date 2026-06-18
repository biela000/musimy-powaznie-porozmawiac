package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class CountdownMessage extends WsMessage {
    public int count;

    public CountdownMessage(int count) {
        super(MessageType.COUNTDOWN);
        this.count = count;
    }
}
