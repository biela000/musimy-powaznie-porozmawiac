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
    private final double manaCost;
    private final int castDuration;
    private final double damagePerTick;

    public DarkMagicSpellEffect(double damagePerTick, double manaCost, int castDuration) {
        this.damagePerTick = damagePerTick;
        this.manaCost = manaCost;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        Player caster = state.getPlayer(casterId);
        Player target = state.getPlayer(targetId);

        if (caster.getMana() < manaCost) {
            return new SpellCastResult(0, null);
        }

        combatEngine.applyManaUsage(state, casterId, manaCost);

        if (target == null || accuracy < 0.5) {
            return new SpellCastResult(castDuration, null);
        }

        caster.registerSpellCast(Element.DARK);

        return new SpellCastResult(castDuration, outEvents -> {
            target.getActiveEffects().add(new DarkMagicEffect(damagePerTick, 1.0));
            outEvents.add(new CombatEventMessage(targetId, CombatEventType.STATUS_APPLIED, 0));
        });
    }
}