package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.Element;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.model.spell.effect.SpellEffect;
import com.jtjmpm.api.model.status.DarkMagicEffect;
import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.CombatEventType;

public class DarkMagicSpellEffect implements SpellEffect {
    private final int castDuration;
    private final double damagePerTick;

    public DarkMagicSpellEffect(double damagePerTick, int castDuration) {
        this.damagePerTick = damagePerTick;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        return new SpellCastResult(castDuration, targetId, outEvents -> {
            Player target = state.getPlayer(targetId);
            if (target != null) {
                target.addEffect(new DarkMagicEffect(damagePerTick, 1.0));
                outEvents.add(new CombatEventMessage(targetId, CombatEventType.STATUS_APPLIED, 0));
            }
        });
    }
}
