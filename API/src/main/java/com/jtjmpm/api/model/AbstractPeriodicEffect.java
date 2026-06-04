package com.jtjmpm.api.model;

import com.jtjmpm.api.config.GameConstants;
import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;
import java.util.List;

public abstract class AbstractPeriodicEffect implements StatusEffect {

    private final int intervalTicks;
    private int tickCounter = 0;

    public AbstractPeriodicEffect(double intervalInSeconds) {
        this.intervalTicks = (int) (intervalInSeconds * GameConstants.TICKS_PER_SECOND);
    }

    @Override
    public final void onTick(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents) {
        tickCounter++;

        if (tickCounter >= intervalTicks) {
            applyEffectLogic(state, targetId, engine, outEvents);
            tickCounter = 0;
        }
    }

    // this method should implement logic that happens every tick for a specific effect
    protected abstract void applyEffectLogic(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents);
}