package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.api.model.GameState;
import com.jtjmpm.api.model.SessionRegistry;
import com.jtjmpm.messages.GameStateUpdateMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchSupervisor {

    private final SessionRegistry registry;
    private final Gson gson;

    public MatchSupervisor(SessionRegistry registry, Gson gson) {
        this.registry = registry;
        this.gson = gson;
    }

    public void executeAndEvaluate(GameState gameState, List<String> playerIds, Runnable gameAction) {
        gameAction.run();

        if (gameState.isGameOver()) {
            String winnerId = gameState.getWinnerId();
            System.out.println("Game Over! Winner: " + winnerId);
            //TODO
            //finish the game, send game over message or smth like that
        } else {
            registry.broadcast(playerIds, gson.toJson(new GameStateUpdateMessage(gameState.toDTO())));
        }
    }
}