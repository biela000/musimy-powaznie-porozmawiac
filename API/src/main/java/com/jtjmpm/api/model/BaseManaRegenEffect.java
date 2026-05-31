package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public class BaseManaRegenEffect implements StatusEffect {
    private final double valuePerTick;

    public BaseManaRegenEffect(double valuePerTick) {
        this.valuePerTick = valuePerTick;
    }

    @Override
    public boolean tick(GameState gameState, String targetId, CombatEngine combatEngine) {
        combatEngine.applyManaRegen(gameState, targetId, valuePerTick);

        return false;
    }
}
