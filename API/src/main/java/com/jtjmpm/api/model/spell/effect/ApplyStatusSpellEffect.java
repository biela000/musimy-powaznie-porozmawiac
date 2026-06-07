package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.api.model.status.StatusEffect;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.CombatEventType;

import java.util.function.Supplier;

public class ApplyStatusSpellEffect implements SpellEffect {
    private final int castDuration;
    private final Supplier<StatusEffect> effectFactory;

    public ApplyStatusSpellEffect(int castDuration, Supplier<StatusEffect> effectFactory) {
        this.castDuration = castDuration;
        this.effectFactory = effectFactory;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        return new SpellCastResult(castDuration, (outEvents) -> {
            StatusEffect freshEffect = effectFactory.get();
            state.getPlayer(targetId).getActiveEffects().add(freshEffect);
            outEvents.add(new CombatEventMessage(targetId, CombatEventType.STATUS_APPLIED, 0));
        });
    }
}