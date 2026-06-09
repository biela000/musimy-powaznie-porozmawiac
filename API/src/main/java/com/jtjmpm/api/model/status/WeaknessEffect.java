package com.jtjmpm.api.model.status;


import com.jtjmpm.api.model.core.GameConstants;

public class WeaknessEffect implements StatusEffect {
    private int remainingDurationTicks;
    private final double accuracy;

    public WeaknessEffect(double durationInSeconds, double accuracy) {
        this.remainingDurationTicks = (int) (durationInSeconds * GameConstants.TICKS_PER_SECOND);
        this.accuracy = accuracy;
    }

    @Override
    public double modifyOutgoingDamage(double rawDamage) {
        return rawDamage * (1.0 - (0.5 * accuracy));
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
        return "WEAKNESS";
    }
}