package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.api.model.*;
import com.jtjmpm.messages.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
    public void handlePlayerSpellCast(GameState gameState, String casterId, String targetId, Spell spell,
                                      double accuracy, PlayerMoveResult moveResult, List<String> playerIds) {

        if (gameState.getStatus() != MatchStatus.IN_PROGRESS) {
            System.out.println("Move declined, game is not in progress");
            return;
        }

        executeAndEvaluate(gameState, playerIds, (outEvents) -> {
            // can put logic that is the same for all moves here
            // but then we'd have to change SpellCastResult and SpellEffect.cast()
            SpellCastResult result = spell.effect().cast(gameState, casterId, targetId, accuracy, combatEngine);

            if (result.status != CastStatus.SUCCESS) {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), result.status)));
            }
            else {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), result.status)));
                scheduleImpact(gameState, playerIds, result.delayMs, result.impactActions);
            }
        });
    }

    // All moves that affect the game state have to go through this method
    public void executeAndEvaluate(GameState gameState, List<String> playerIds,
                                   Consumer<List<CombatEventMessage>> gameAction) {

        // all game state changes go through this synchronized code
        synchronized (gameState){
            List<CombatEventMessage> outEvents = new ArrayList<>();

            gameAction.accept(outEvents);

            if (gameState.isGameOver() && gameState.getStatus() == MatchStatus.IN_PROGRESS) {

                gameState.setStatus(MatchStatus.GAME_OVER);

                if (gameState.isDraw()) {
                    System.out.println("GAME OVER, DRAW");
                    registry.broadcast(playerIds, gson.toJson(new GameOverMessage(null, GameOverReason.DRAW)));
                } else {
                    String winnerId = gameState.getWinnerId();
                    System.out.println("GAME OVER, WINNER " + winnerId);
                    registry.broadcast(playerIds, gson.toJson(new GameOverMessage(winnerId, GameOverReason.WIN)));
                }

            } else if(gameState.getStatus() == MatchStatus.IN_PROGRESS) {
                GameStateUpdateMessage updateMessage = new GameStateUpdateMessage(gameState.toDTO(), outEvents);

                registry.broadcast(playerIds, gson.toJson(updateMessage));
            }
        }
    }

    public void scheduleImpact(GameState gameState, List<String> playerIds, long delayMs,
                               Consumer<List<CombatEventMessage>> impactAction) {
        scheduler.schedule(() ->
                executeAndEvaluate(gameState, playerIds, impactAction), delayMs, TimeUnit.MILLISECONDS
        );
    }
}