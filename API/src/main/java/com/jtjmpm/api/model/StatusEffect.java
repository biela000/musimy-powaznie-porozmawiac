package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;

import java.util.List;

public interface StatusEffect {
    default void onTick(GameState gameState, String targetId, CombatEngine combatEngine, List<CombatEventMessage> outEvents) { };

    default double modifySpellIncomingDamage(double rawDamage) {
        return rawDamage;
    }

    //TODO change these two
    default void onDamageTaken(double finalDamage, GameState state, String myId, String attackerId, CombatEngine engine) {}

    default void onAttackLanded(CombatEventMessage event, GameState state, String myId, CombatEngine engine) {}

    void decreaseDuration();
    boolean isExpired();

    // if we want effects to do other things like modify output damage or input damage etc. we can delcare methods for that here
}
