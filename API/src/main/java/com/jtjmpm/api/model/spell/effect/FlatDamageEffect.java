package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.spell.SpellCastResult;

public class FlatDamageEffect implements SpellEffect {
    private final double baseDamage;
    private final int castDuration;

    public FlatDamageEffect(double baseDamage, int castDuration){
        this.baseDamage = baseDamage;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine){
        return new SpellCastResult(castDuration, (outEvents) -> {
            double actualDamage = Math.round(baseDamage * accuracy);
            outEvents.add(combatEngine.applyDamage(state, casterId, targetId, actualDamage));
        });
    }
}
