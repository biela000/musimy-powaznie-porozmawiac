package com.jtjmpm.api.model.status;

public class ShieldEffect implements StatusEffect {
    private final double accuracy;
    private boolean expired = false;

    public ShieldEffect(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public double modifySpellIncomingDamage(double rawDamage) {
        expired = true;
        return rawDamage * (1.0 - (0.8 * accuracy));
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public String getName() {
        return "SHIELD";
    }
}