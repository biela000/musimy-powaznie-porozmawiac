package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.api.model.GameState;
import com.jtjmpm.api.model.SessionRegistry;
import com.jtjmpm.api.model.Spell;
import com.jtjmpm.api.model.SpellCastResult;
import com.jtjmpm.messages.CastStatus;
import com.jtjmpm.messages.GameStateUpdateMessage;
import com.jtjmpm.messages.MoveResultMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class MatchSupervisor {

    private final SessionRegistry registry;
    private final Gson gson;
    private final CombatEngine combatEngine;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public MatchSupervisor(SessionRegistry registry, Gson gson, CombatEngine combatEngine) {
        this.registry = registry;
        this.gson = gson;
        this.combatEngine = combatEngine;
    }

    // This could be in GameController, but its probably a bit cleaner here
    public void handlePlayerSpellCast(GameState gameState, String casterId, String targetId, Spell spell, double accuracy, PlayerMoveResult moveResult, List<String> playerIds) {

        executeAndEvaluate(gameState, playerIds, () -> {
            // can put logic that is the same for all moves here
            // but then we'd have to change SpellCastResult and SpellEffect.apply()
            SpellCastResult result = spell.effect().cast(gameState, casterId, targetId, accuracy, combatEngine);

            if (result.status != CastStatus.SUCCESS) {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), result.status)));
            }
            else {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), result.status)));
                scheduleImpact(gameState, playerIds, result.delayMs, result.impactAction);
            }
        });
    }

    // All changes affecting the game state have to go through this method
    public void executeAndEvaluate(GameState gameState, List<String> playerIds, Runnable gameAction) {
        gameAction.run();

        if (gameState.isGameOver()) {
            String winnerId = gameState.getWinnerId();
            System.out.println("Game Over! Winner: " + winnerId);
            //TODO
            // end the game, send game over message or smth like that
        } else {
            registry.broadcast(playerIds, gson.toJson(new GameStateUpdateMessage(gameState.toDTO())));
        }
    }

    public void scheduleImpact(GameState gameState, List<String> playerIds, long delayMs, Runnable impactAction) {
        scheduler.schedule(() -> {
            synchronized (gameState) {
                executeAndEvaluate(gameState, playerIds, impactAction);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }
}