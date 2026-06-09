package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.model.spell.effect.SpellEffect;
import com.jtjmpm.api.service.CombatEngine;

public class ManaStealSpellEffect implements SpellEffect {
    private final double baseSteal;
    private final double manaCost;
    private final int castDuration;

    public ManaStealSpellEffect(double baseSteal, double manaCost, int castDuration) {
        this.baseSteal = baseSteal;
        this.manaCost = manaCost;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        Player caster = state.getPlayer(casterId);
        Player target = state.getPlayer(targetId);

        if (caster.getMana() < manaCost) {
            return new SpellCastResult(0, null);
        }

        combatEngine.applyManaUsage(state, casterId, manaCost);

        return new SpellCastResult(castDuration, outEvents -> {
            double stealAttempt = baseSteal * accuracy;
            double actualSteal = Math.min(target.getMana(), stealAttempt);

            combatEngine.applyManaUsage(state, targetId, actualSteal);
            combatEngine.applyManaRegen(state, casterId, actualSteal);
        });
    }
}