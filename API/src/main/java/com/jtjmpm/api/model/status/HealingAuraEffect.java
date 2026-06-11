package com.jtjmpm.api.model.status;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public class HealingAuraEffect extends AbstractTemporaryPeriodicEffect {
    private final double baseHealPerInterval;
    private final double accuracy;

    public HealingAuraEffect(double baseHealPerInterval, double intervalInSeconds, double durationInSeconds, double accuracy) {
        super(intervalInSeconds, durationInSeconds);
        this.baseHealPerInterval = baseHealPerInterval;
        this.accuracy = accuracy;
    }

    @Override
    protected void applyEffectLogic(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents) {
        double healAmount = baseHealPerInterval * accuracy;
        outEvents.add(engine.applyHeal(state, targetId, healAmount));
    }

    @Override
    public String getName() {
        return "HEALING_AURA";
    }
}
