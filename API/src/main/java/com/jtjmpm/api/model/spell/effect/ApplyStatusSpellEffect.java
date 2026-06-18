package com.jtjmpm.api.model.spell.effect;

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
    private final int castDuration;
    private final boolean targetCaster;
    private final Function<Double, StatusEffect> effectFactory;

    public ApplyStatusSpellEffect(int castDuration, boolean targetCaster, Function<Double, StatusEffect> effectFactory) {
        this.castDuration = castDuration;
        this.targetCaster = targetCaster;
        this.effectFactory = effectFactory;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        String finalTargetId = targetCaster ? casterId : targetId;
        return new SpellCastResult(castDuration, finalTargetId, (outEvents) -> {
            Player finalTarget = state.getPlayer(finalTargetId);
            if (finalTarget != null) {
                StatusEffect freshEffect = effectFactory.apply(accuracy);
                finalTarget.addEffect(freshEffect);
                outEvents.add(new CombatEventMessage(finalTargetId, CombatEventType.STATUS_APPLIED, 0));
            }
        });
    }
}
