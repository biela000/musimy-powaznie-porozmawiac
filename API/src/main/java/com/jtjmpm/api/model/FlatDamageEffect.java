package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public class FlatDamageEffect implements SpellEffect {
    private final double baseDamage;

    public FlatDamageEffect(double baseDamage){
        this.baseDamage = baseDamage;
    }
    @Override
    public void apply(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine){
        Player target = state.getPlayer(targetId);
        if (target != null && accuracy >= 0.5) {
            double actualDamage = Math.round(baseDamage * accuracy);
            combatEngine.applyDamage(state, casterId, targetId, actualDamage);
        }
    }
}
