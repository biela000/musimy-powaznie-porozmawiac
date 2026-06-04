package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public class PoisonEffect extends AbstractPeriodicEffect {

    private final double damagePerInterval;

    public PoisonEffect(double damagePerInterval, double intervalInSeconds, double durationInSeconds) {
        super(intervalInSeconds, durationInSeconds);
        this.damagePerInterval = damagePerInterval;
    }

    @Override
    protected void applyEffectLogic(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents) {
        CombatEventMessage event = engine.applyStatusDamage(state, targetId, damagePerInterval);
        outEvents.add(event);
    }

    @Override
    public String getName() {
        return "POISON";
    }
}
