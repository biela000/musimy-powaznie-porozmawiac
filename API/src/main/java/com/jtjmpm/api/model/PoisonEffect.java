package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.CombatEventType;

import java.util.List;

public class PoisonEffect implements StatusEffect {
    private final double damagePerTick;
    private int duration;

    public PoisonEffect(double damagePerTick, int duration) {
        this.damagePerTick = damagePerTick;
        this.duration = duration;
    }

    @Override
    public void onTick(GameState gameState, String targetId, CombatEngine combatEngine, List<CombatEventMessage> outEvents) {
        CombatEventMessage event = combatEngine.applyStatusDamage(gameState, targetId, damagePerTick);
        outEvents.add(event);
    }
    @Override
    public void decreaseDuration() {
        duration--;
    }

    @Override
    public boolean isExpired() {
        return duration <= 0;
    }

    @Override
    public String getName() {
        return "POISON";
    }

    @Override
    public int getRemainingDuration() {
        return duration;
    }
}
