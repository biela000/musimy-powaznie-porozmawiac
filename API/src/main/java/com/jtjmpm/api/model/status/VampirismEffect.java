package com.jtjmpm.api.model.status;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public class VampirismEffect extends AbstractTemporaryPeriodicEffect {
    private final double accuracy;

    public VampirismEffect(double durationInSeconds, double accuracy) {
        super(1.0, durationInSeconds);
        this.accuracy = accuracy;
    }

    @Override
    protected void applyEffectLogic(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents) {
    }

    @Override
    public void onAttackLanded(CombatEventMessage event, GameState state, String myId, CombatEngine engine) {
        double healAmount = event.value * 0.5 * accuracy;
        engine.applyHeal(state, myId, healAmount);
    }

    @Override
    public String getName() {
        return "VAMPIRISM";
    }
}