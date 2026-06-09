package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.service.CombatEngine;

public class GambleSpellEffect implements SpellEffect {
    private final double baseDamage;
    private final double manaCost;
    private final int castDuration;

    public GambleSpellEffect(double baseDamage, double manaCost, int castDuration) {
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        Player caster = state.getPlayer(casterId);

        if (caster.getMana() < manaCost) {
            return new SpellCastResult(0, null);
        }

        combatEngine.applyManaUsage(state, casterId, manaCost);

        return new SpellCastResult(castDuration, outEvents -> {
            double finalDamage = baseDamage * accuracy;
            String finalTarget = Math.random() < 0.40 ? casterId : targetId;
            outEvents.add(combatEngine.applyDamage(state, casterId, finalTarget, finalDamage));
        });
    }
}