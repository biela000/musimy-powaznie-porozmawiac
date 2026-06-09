package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.service.CombatEngine;

public class    HealSpellEffect implements SpellEffect {
    private final double baseHeal;
    private final int castDuration;

    public HealSpellEffect(double baseHeal, int castDuration) {
        this.baseHeal = baseHeal;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        return new SpellCastResult(castDuration, outEvents -> {
            double finalHeal = baseHeal * accuracy;
            outEvents.add(combatEngine.applyHeal(state, casterId, finalHeal));
        });
    }
}