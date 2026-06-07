package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.ArrayList;
import java.util.List;

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
