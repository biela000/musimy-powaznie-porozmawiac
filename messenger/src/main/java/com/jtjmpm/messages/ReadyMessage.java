package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

import java.util.List;

public class ReadyMessage extends WsMessage {
    public List<String> selectedSpells;
    public ReadyMessage(List<String> selectedSpells) {
        super(MessageType.TOGGLE_READY);
        this.selectedSpells = selectedSpells;
    }
}
