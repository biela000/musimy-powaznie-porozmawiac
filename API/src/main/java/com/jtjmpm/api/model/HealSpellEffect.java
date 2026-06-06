package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CastStatus;

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
            return new SpellCastResult(CastStatus.FAILED_MANA);
        }

        combatEngine.applyManaUsage(state, casterId, manaCost);

        return new SpellCastResult(CastStatus.SUCCESS, castDuration, outEvents -> {
            double finalHeal = baseHeal * accuracy;
            outEvents.add(combatEngine.applyHeal(state, casterId, finalHeal));
        });
    }
}