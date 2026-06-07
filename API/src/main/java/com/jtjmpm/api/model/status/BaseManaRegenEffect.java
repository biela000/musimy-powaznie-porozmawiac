package com.jtjmpm.api.model.status;

import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public class BaseManaRegenEffect extends AbstractPeriodicEffect {

    private final double manaPerInterval;

    public BaseManaRegenEffect(double manaPerTick, double intervalInSeconds) {
        super(intervalInSeconds);
        this.manaPerInterval = manaPerTick;
    }

    @Override
    protected void applyEffectLogic(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents) {
        engine.applyManaRegen(state, targetId, manaPerInterval);
    }

    @Override
    public String getName() {
        return "BASE_MANA_REGEN";
    }

    @Override
    public boolean isVisibleOnUI() {
        return false;
    }
}
