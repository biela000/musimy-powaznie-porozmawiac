package com.jtjmpm.api.model;

public class DamageUpEffect implements StatusEffect {
    private final double accuracy;
    private boolean expired = false;

    public DamageUpEffect(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public double modifyOutgoingDamage(double rawDamage) {
        expired = true;
        return rawDamage * (1.0 + (0.5 * accuracy));
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public String getName() {
        return "DAMAGE_UP";
    }
}