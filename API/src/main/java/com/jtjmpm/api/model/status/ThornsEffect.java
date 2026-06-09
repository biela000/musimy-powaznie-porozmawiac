package com.jtjmpm.api.model.status;

import com.jtjmpm.api.model.core.GameConstants;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.status.StatusEffect;
import com.jtjmpm.api.service.CombatEngine;

public class ThornsEffect implements StatusEffect {
    private final double reflectPercentage;
    private int remainingDurationTicks;

    public ThornsEffect(double reflectPercentage, double durationInSeconds) {
        this.reflectPercentage = reflectPercentage;
        this.remainingDurationTicks = (int) (durationInSeconds * GameConstants.TICKS_PER_SECOND);
    }

    @Override
    public void onDamageTaken(double finalDamage, GameState state, String myId, String attackerId, CombatEngine engine, java.util.List<com.jtjmpm.messages.CombatEventMessage> outEvents) {
        if (finalDamage > 0 && attackerId != null && !attackerId.equals(myId)) {
            double reflectedDamage = finalDamage * reflectPercentage;
            outEvents.add(engine.applyStatusDamage(state, attackerId, reflectedDamage));
        }
    }

    @Override
    public void decreaseDuration() {
        remainingDurationTicks--;
    }

    @Override
    public boolean isExpired() {
        return remainingDurationTicks <= 0;
    }

    @Override
    public int getRemainingDuration() {
        return remainingDurationTicks / GameConstants.TICKS_PER_SECOND;
    }

    @Override
    public String getName() {
        return "THORNS";
    }
}