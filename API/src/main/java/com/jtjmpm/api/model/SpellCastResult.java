package com.jtjmpm.api.model;

import com.jtjmpm.messages.CastStatus;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;
import java.util.function.Consumer;

public class SpellCastResult {
    public CastStatus status;
    public int delayMs; // maybe redundant but its ok
    public Consumer<List<CombatEventMessage>> impactActions;

    public SpellCastResult(CastStatus status) {
        this.status = status;
        this.delayMs = 0;
        this.impactActions = null;
    }

    public SpellCastResult(CastStatus status, int delayMs, Consumer<List<CombatEventMessage>> impactAction) {
        this.status = status;
        this.delayMs = delayMs;
        this.impactActions = impactAction;
    }
}
