package com.jtjmpm.messages;

import com.jtjmpm.MessageType;
import com.jtjmpm.WsErrorName;

public class WsErrorMessage extends WsMessage {
    public WsErrorName name;
    public String description;

    public WsErrorMessage(WsErrorName name, String description) {
        super(MessageType.ERROR);
        this.name = name;
        this.description = description;
    }
}
