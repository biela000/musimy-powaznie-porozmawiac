package com.jtjmpm.api.model;

public interface SpellEffect {
    void apply(GameState state, String casterId, String targetId, double accuracy);
}
