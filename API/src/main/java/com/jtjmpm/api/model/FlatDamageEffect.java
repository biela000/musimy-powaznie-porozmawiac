package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CastStatus;

public class FlatDamageEffect implements SpellEffect {
    private final double baseDamage;
    private final double manaCost;
    private final int castDuration;

    public FlatDamageEffect(double baseDamage, double manaCost, int castDuration){
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
        this.castDuration = castDuration;
    }
    @Override
    public SpellCastResult apply(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine){
        // if the spell fails we return it here, if it doesnt we return the method that needs to be ran when the attack hits
        Player caster = state.getPlayer(casterId);
        Player target = state.getPlayer(targetId);

        // logic that happens at the moment of casting goes here

        if (caster.getMana() < manaCost) {
            return new SpellCastResult(CastStatus.FAILED_MANA);
        }

        combatEngine.applyManaUsage(state, casterId, manaCost);

        //TODO make a min accuracy field
        if (target == null || accuracy < 0.5) {
            return new SpellCastResult(CastStatus.FAILED_ACCURACY);
        }

        // logic that happens when the attack hits goes here
        return new SpellCastResult(CastStatus.SUCCESS, castDuration, () -> {
            double actualDamage = Math.round(baseDamage * accuracy);
            combatEngine.applyDamage(state, casterId, targetId, actualDamage);
        });
    }
}
