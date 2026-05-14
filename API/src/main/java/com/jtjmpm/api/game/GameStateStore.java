package com.jtjmpm.api.game;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStateStore {
    private final ConcurrentHashMap<String, GameState> battles = new ConcurrentHashMap<>();

    public GameState getOrCreate(String battleId) {
        return battles.computeIfAbsent(battleId, id -> new GameState(id));
    }

    public void remove(String battleId) {
        battles.remove(battleId);
    }
}
