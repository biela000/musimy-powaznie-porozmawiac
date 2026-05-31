package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public class FlatDamageEffect implements SpellEffect {
    private final double baseDamage;
    private final double manaCost;

    public FlatDamageEffect(double baseDamage, double manaCost){
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
    }
    @Override
    public void apply(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine){
        Player target = state.getPlayer(targetId);
        combatEngine.applyManaUsage(state, casterId, manaCost);
        if (target != null && accuracy >= 0.5) {
            double actualDamage = Math.round(baseDamage * accuracy);
            combatEngine.applyDamage(state, casterId, targetId, actualDamage);
        }
    }
}
