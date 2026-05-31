package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public interface StatusEffect {
    boolean tick(GameState gameState, String targetId, CombatEngine combatEngine);
}
