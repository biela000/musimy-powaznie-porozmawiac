package com.jtjmpm.api.service;

import com.google.gson.Gson;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.api.model.PatternEngine.PatternGenerator;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.messages.MatchStatus;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.Spell;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.messages.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

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

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    // This could be in GameController, but its probably a bit cleaner here
    public void handlePlayerSpellCast(GameState gameState, String casterId, String targetId, Spell spell,
                                      double accuracy, PlayerMoveResult moveResult, List<String> playerIds) {

        if (gameState.getStatus() != MatchStatus.IN_PROGRESS) {
            System.out.println("Move declined, game is not in progress");
            return;
        }

        executeAndEvaluate(gameState, playerIds, (outEvents) -> {
            Player caster = gameState.getPlayer(casterId);
            Player target = gameState.getPlayer(targetId);

            if (caster.getHp() <= 0) {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), CastStatus.FAILED_DEATH)));
                return;
            }

            if (caster.getMana() < spell.manaCost()) {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), CastStatus.FAILED_MANA)));
                return;
            }

            combatEngine.applyManaUsage(gameState, casterId, spell.manaCost());

            if (target == null || accuracy < 0.5) {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), CastStatus.FAILED_ACCURACY)));
                return;
            }

            SpellCastResult result = spell.effect().cast(gameState, casterId, targetId, accuracy, combatEngine);

            MoveResultMessage successMsg = new MoveResultMessage(moveResult, casterId, spell.name(),
                    spell.castDurationMs(), CastStatus.SUCCESS);
            successMsg.accuracyRating = moveResult.getAccuracyRating();
            registry.broadcast(playerIds, gson.toJson(successMsg));
            scheduleImpact(gameState, playerIds, result.delayMs, result.impactActions);
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

                            System.out.println("Game Started");

                            List<PatternGenerator.NamedShape> pool = PatternGenerator.shuffledPool(PatternGenerator.Difficulty.EASY, 64);
                            List<Player> players = gameState.getPlayers();
                            for (int i = 0; i < players.size(); i++) {
                                PatternGenerator.NamedShape shape = pool.get(i % pool.size());
                                players.get(i).setCurrentPattern(shape.points(), shape.name());
                                registry.sendToSession(players.get(i).getId(),
                                        gson.toJson(new StartGameMessage(shape.points(), shape.name())));
                            }
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

                GameStateUpdateMessage updateMessage = new GameStateUpdateMessage(gameState.toDTO(), outEvents);
                registry.broadcast(playerIds, gson.toJson(updateMessage));

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