package com.jtjmpm.api.model.spell;

import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;
import java.util.function.Consumer;

public class SpellCastResult {
    public int delayMs;
    public String actualTargetId;
    public Consumer<List<CombatEventMessage>> impactActions;

    public SpellCastResult(int delayMs, String actualTargetId, Consumer<List<CombatEventMessage>> impactActions) {
        this.delayMs = delayMs;
        this.actualTargetId = actualTargetId;
        this.impactActions = impactActions;
    }
}
