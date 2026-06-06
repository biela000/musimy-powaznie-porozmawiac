package com.jtjmpm.api.model;

import com.jtjmpm.api.config.GameConstants;

public class BlindnessEffect implements StatusEffect {
    private int remainingDurationTicks;

    public BlindnessEffect(double durationInSeconds) {
        this.remainingDurationTicks = (int) (durationInSeconds * GameConstants.TICKS_PER_SECOND);
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
    public boolean isVisibleOnUI() {
        return true;
    }

    @Override
    public String getName() {
        return "BLINDNESS";
    }
}