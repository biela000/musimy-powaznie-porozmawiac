package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;

public interface SpellEffect {
    SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine);
}
