package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public interface SpellEffect {
    void apply(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine);
}
