package com.jtjmpm.api.model;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.model.status.StatusEffect;
import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.api.model.spell.effect.SpellEffect;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.CombatEventType;
import java.util.function.Function;
import java.util.function.Supplier;

public class ApplyStatusSpellEffect implements SpellEffect {
    private final double manaCost;
    private final int castDuration;
    private final boolean targetCaster;
    private final Function<Double, StatusEffect> effectFactory;

    public ApplyStatusSpellEffect(double manaCost, int castDuration, Supplier<StatusEffect> oldFactory) {
        this.manaCost = manaCost;
        this.castDuration = castDuration;
        this.targetCaster = false;
        this.effectFactory = accuracy -> oldFactory.get();
    }

    public ApplyStatusSpellEffect(double manaCost, int castDuration, boolean targetCaster, Function<Double, StatusEffect> effectFactory) {
        this.manaCost = manaCost;
        this.castDuration = castDuration;
        this.targetCaster = targetCaster;
        this.effectFactory = effectFactory;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        Player caster = state.getPlayer(casterId);

        if (caster.getMana() < manaCost) {
            return new SpellCastResult(0, null);
        }

        combatEngine.applyManaUsage(state, casterId, manaCost);

        if (accuracy < 0.5 || (!targetCaster && state.getPlayer(targetId) == null)) {
            return new SpellCastResult(castDuration, null);
        }

        return new SpellCastResult(castDuration, (outEvents) -> {
            String finalTargetId = targetCaster ? casterId : targetId;
            Player finalTarget = state.getPlayer(finalTargetId);
            StatusEffect freshEffect = effectFactory.apply(accuracy);
            finalTarget.getActiveEffects().add(freshEffect);
            outEvents.add(new CombatEventMessage(finalTargetId, CombatEventType.STATUS_APPLIED, 0));
        });
    }
}