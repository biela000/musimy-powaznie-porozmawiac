package com.jtjmpm.api.model.status;

import com.jtjmpm.api.model.core.GameConstants;
import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.api.model.core.GameState;
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

    protected abstract void applyEffectLogic(GameState state, String targetId, CombatEngine engine, List<CombatEventMessage> outEvents);
}
