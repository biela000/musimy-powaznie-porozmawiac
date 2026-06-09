package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.service.CombatEngine;

public class GambleSpellEffect implements SpellEffect {
    private final double baseDamage;
    private final int castDuration;

    public GambleSpellEffect(double baseDamage, int castDuration) {
        this.baseDamage = baseDamage;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        return new SpellCastResult(castDuration, outEvents -> {
            double finalDamage = baseDamage * accuracy;
            String finalTarget = Math.random() < 0.40 ? casterId : targetId;
            combatEngine.applyDamage(state, casterId, finalTarget, finalDamage, outEvents);
        });
    }
}