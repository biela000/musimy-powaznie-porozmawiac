package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class GetSpellsListMessage extends WsMessage {
    public GetSpellsListMessage() {
        super(MessageType.GET_SPELLS_LIST);
    }
}
