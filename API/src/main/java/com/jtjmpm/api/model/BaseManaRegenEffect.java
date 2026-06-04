package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public class BaseManaRegenEffect implements StatusEffect {
    private final double valuePerTick;

    public BaseManaRegenEffect(double valuePerTick) {
        this.valuePerTick = valuePerTick;
    }

    @Override
    public void onTick(GameState gameState, String targetId, CombatEngine combatEngine, List<CombatEventMessage> outEvents) {
        combatEngine.applyManaRegen(gameState, targetId, valuePerTick);
    }

    @Override
    public void decreaseDuration() {
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public String getName() {
        return "BASE MANA REGEN";
    }

    @Override
    public int getRemainingDuration() {
        return -1;
    }

    @Override
    public boolean isVisibleOnUI() {
        return false;
    }
}
