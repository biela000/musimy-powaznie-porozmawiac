package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.service.CombatEngine;

public class HealSpellEffect implements SpellEffect {
    private final double baseHeal;
    private final double manaCost;
    private final int castDuration;

    public HealSpellEffect(double baseHeal, double manaCost, int castDuration) {
        this.baseHeal = baseHeal;
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
            double finalHeal = baseHeal * accuracy;
            outEvents.add(combatEngine.applyHeal(state, casterId, finalHeal));
        });
    }
}