package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.api.model.*;
import com.jtjmpm.messages.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class MatchSupervisor {

    private final static int START_GAME_DELAY_SECONDS = 5;

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

    public void handlePlayerReadyToggle(GameState gameState, String playerId, List<String> validSpells, List<String> playerIds) {

        synchronized (gameState) {
            Player player = gameState.getPlayer(playerId);
            if (player == null) return;

            if (!player.isReady()) {
                player.setSpellLoadout(validSpells);
                player.setReady(true);
                System.out.println("Player: " + playerId + " is ready");
            } else {
                player.setReady(false);
                System.out.println("Player: " + playerId + " stopped being ready");
            }

            GameStateUpdateMessage responseMessage = new GameStateUpdateMessage(gameState.toDTO(), Collections.emptyList());
            registry.broadcast(playerIds, gson.toJson(responseMessage));

            if (gameState.isReady() && gameState.getStatus() == MatchStatus.LOBBY) {

                System.out.println("All players ready");

                scheduler.schedule(() -> {
                    synchronized (gameState) {
                        if (gameState.isReady() && gameState.getStatus() == MatchStatus.LOBBY) {
                            gameState.setStatus(MatchStatus.IN_PROGRESS);

                            registry.broadcast(playerIds, gson.toJson(new StartGameMessage()));
                            System.out.println("Game Started");
                        } else {
                            System.out.println("Game start interrupted");
                        }
                    }

                }, START_GAME_DELAY_SECONDS, TimeUnit.SECONDS); // delay after starting game
            }
        }
    }


    // All events that affect the game state have to go through this method
    public void executeAndEvaluate(GameState gameState, List<String> playerIds,
                                   Consumer<List<CombatEventMessage>> gameAction) {

        // all events go through this synchronized code
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