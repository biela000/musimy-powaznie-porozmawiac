package com.jtjmpm.api.model.status;

import com.jtjmpm.api.model.core.GameConstants;

public abstract class AbstractTemporaryPeriodicEffect extends AbstractPeriodicEffect {

    protected int remainingDurationTicks;

    public AbstractTemporaryPeriodicEffect(double intervalInSeconds, double durationInSeconds) {
        super(intervalInSeconds);
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
}
