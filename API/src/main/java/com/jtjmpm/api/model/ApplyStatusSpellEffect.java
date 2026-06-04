package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CastStatus;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.CombatEventType;

import java.util.function.Supplier;

public class ApplyStatusSpellEffect implements SpellEffect {
    private final double manaCost;
    private final int castDuration;

    // Factory
    private final Supplier<StatusEffect> effectFactory;

    public ApplyStatusSpellEffect(double manaCost, int castDuration, Supplier<StatusEffect> effectFactory) {
        this.manaCost = manaCost;
        this.castDuration = castDuration;
        this.effectFactory = effectFactory;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        Player caster = state.getPlayer(casterId);
        Player target = state.getPlayer(targetId);

        if (caster.getMana() < manaCost) {
            return new SpellCastResult(CastStatus.FAILED_MANA);
        }

        combatEngine.applyManaUsage(state, casterId, manaCost);

        if (target == null || accuracy < 0.5) {
            return new SpellCastResult(CastStatus.FAILED_ACCURACY);
        }

        return new SpellCastResult(CastStatus.SUCCESS, castDuration, (outEvents) -> {
            StatusEffect freshEffect = effectFactory.get();

            target.getActiveEffects().add(freshEffect);

            outEvents.add(new CombatEventMessage(targetId, CombatEventType.STATUS_APPLIED, 0));
        });
    }
}