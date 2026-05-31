package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public interface StatusEffect {
    default boolean tick(GameState gameState, String targetId, CombatEngine combatEngine) { return true; };

    // if we want effects to do other things like modify output damage or input damage etc. we can delcare methods for that here
}
