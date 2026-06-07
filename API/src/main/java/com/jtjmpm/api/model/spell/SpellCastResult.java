package com.jtjmpm.api.model.spell;

import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;
import java.util.function.Consumer;

public class SpellCastResult {
    public int delayMs;
    public Consumer<List<CombatEventMessage>> impactActions;

    public SpellCastResult(int delayMs, Consumer<List<CombatEventMessage>> impactActions) {
        this.delayMs = delayMs;
        this.impactActions = impactActions;
    }
}
