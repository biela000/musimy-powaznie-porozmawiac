package com.jtjmpm.messages;

import com.jtjmpm.MessageType;
import java.util.List;

public class AvailableSpellsMessage extends WsMessage {
    List<SpellDTO> spells;
    public AvailableSpellsMessage(List<SpellDTO> spells){
        super(MessageType.AVAILABLE_SPELLS);
        this.spells = spells;
    }
}
