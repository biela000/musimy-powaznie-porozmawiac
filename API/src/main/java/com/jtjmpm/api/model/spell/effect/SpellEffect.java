package com.jtjmpm.api.model.spell.effect;

import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.spell.SpellCastResult;

public interface SpellEffect {
    SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine);
}
