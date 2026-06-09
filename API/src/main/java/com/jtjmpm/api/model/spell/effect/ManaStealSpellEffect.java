package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.model.spell.effect.SpellEffect;
import com.jtjmpm.api.service.CombatEngine;

public class ManaStealSpellEffect implements SpellEffect {
    private final double baseSteal;
    private final int castDuration;

    public ManaStealSpellEffect(double baseSteal, int castDuration) {
        this.baseSteal = baseSteal;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        return new SpellCastResult(castDuration, targetId, outEvents -> {
            Player target = state.getPlayer(targetId);
            if (target != null) {
                double stealAttempt = baseSteal * accuracy;
                double actualSteal = Math.min(target.getMana(), stealAttempt);
                combatEngine.applyManaUsage(state, targetId, actualSteal);
                combatEngine.applyManaRegen(state, casterId, actualSteal);
            }
        });
    }
}