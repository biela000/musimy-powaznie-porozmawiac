package com.jtjmpm.api.model.status;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public class VampirismEffect implements StatusEffect {
    private final double accuracy;
    private int remainingDurationTicks;

    public VampirismEffect(double durationInSeconds, double accuracy) {
        this.accuracy = accuracy;
        this.remainingDurationTicks = (int) (durationInSeconds * com.jtjmpm.api.model.core.GameConstants.TICKS_PER_SECOND);
    }

    @Override
    public void onAttackLanded(CombatEventMessage event, GameState state, String myId, CombatEngine engine, java.util.List<CombatEventMessage> outEvents) {
        double healAmount = event.value * 0.5 * accuracy;
        outEvents.add(engine.applyHeal(state, myId, healAmount));
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
        return remainingDurationTicks / com.jtjmpm.api.model.core.GameConstants.TICKS_PER_SECOND;
    }

    @Override
    public String getName() {
        return "VAMPIRISM";
    }
}
