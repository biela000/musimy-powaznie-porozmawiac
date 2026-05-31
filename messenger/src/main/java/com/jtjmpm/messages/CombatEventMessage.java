package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class CombatEventMessage extends WsMessage {
    public String targetId;
    public CombatEventType combatType;
    public double value;

    public CombatEventMessage(String targetId, CombatEventType combatType, double value) {
        super(MessageType.COMBAT_EVENT);
        this.targetId = targetId;
        this.combatType = combatType;
        this.value = value;
    }
}
