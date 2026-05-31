package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public class PoisonEffect implements StatusEffect {
    private final double damagePerTick;
    private int duration;

    public PoisonEffect(double damagePerTick, int duration) {
        this.damagePerTick = damagePerTick;
        this.duration = duration;
    }

    @Override
    public boolean tick(GameState gameState, String targetId, CombatEngine combatEngine) {
        combatEngine.applyStatusDamage(gameState, targetId, damagePerTick);

        duration--;

        return duration <= 0;
    }
}
