package com.jtjmpm.api.model.status;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public class DarkMagicEffect extends AbstractPeriodicEffect {
    private final double damagePerInterval;
    private boolean broken = false;

    public DarkMagicEffect(double damagePerInterval, double intervalInSeconds) {
        super(intervalInSeconds);
        this.damagePerInterval = damagePerInterval;
    }

    @Override
    protected void applyEffectLogic(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents) {
        outEvents.add(engine.applyStatusDamage(state, targetId, damagePerInterval));
    }

    @Override
    public void onSuccessfulSpellCast() {
        this.broken = true;
        System.out.println("Dark Magic on " + getName() + " was broken by a spell cast!");
    }

    @Override
    public boolean isExpired() {
        return broken;
    }

    @Override
    public String getName() {
        return "DARK_MAGIC";
    }

    @Override
    public boolean isPositive() {
        return false;
    }
}
