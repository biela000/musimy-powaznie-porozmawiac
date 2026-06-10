package com.jtjmpm.api.service;

import com.google.gson.Gson;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.api.model.PatternEngine.PatternGenerator;
import com.jtjmpm.api.model.core.GameState;
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

    /** Rounds a player must win to win the match (best-of-5 → first to 3). */
    private static final int WINS_TO_WIN = 3;

    /** Delay after all-ready before the navigation message is sent (gives clients time to settle). */
    private final static int START_GAME_DELAY_SECONDS = 1;

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

    // -------------------------------------------------------------------------
    // Spell cast handling
    // -------------------------------------------------------------------------

    public void handlePlayerSpellCast(GameState gameState, String casterId, String targetId, Spell spell,
                                      double accuracy, PlayerMoveResult moveResult, List<String> playerIds) {

        if (gameState.getStatus() != MatchStatus.IN_PROGRESS) {
            System.out.println("Move declined, game is not in progress (status=" + gameState.getStatus() + ")");
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

            if (target == null || accuracy < 0.35) {
                registry.broadcast(playerIds, gson.toJson(new MoveResultMessage(moveResult, casterId, spell.name(),
                        spell.castDurationMs(), CastStatus.FAILED_ACCURACY)));
                return;
            }
            caster.registerSpellCast(spell.element());
            caster.triggerSuccessfulCastHook();
            SpellCastResult result = spell.effect().cast(gameState, casterId, targetId, accuracy, combatEngine);

            MoveResultMessage successMsg = new MoveResultMessage(moveResult, casterId, spell.name(),
                    spell.castDurationMs(), CastStatus.SUCCESS);
            successMsg.accuracyRating = moveResult.getAccuracyRating();
            successMsg.actualTargetId = result.actualTargetId;
            registry.broadcast(playerIds, gson.toJson(successMsg));
            scheduleImpact(gameState, playerIds, result.delayMs, result.impactActions);
        });
    }

    // -------------------------------------------------------------------------
    // Ready / game start
    // -------------------------------------------------------------------------

    public void handlePlayerReadyToggle(GameState gameState, String playerId, List<String> validSpells,
                                        List<String> playerIds) {
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

            registry.broadcast(playerIds, gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), Collections.emptyList())));

            if (gameState.isReady() && gameState.getStatus() == MatchStatus.LOBBY) {
                System.out.println("All players ready — starting game in " + START_GAME_DELAY_SECONDS + "s");

                scheduler.schedule(() -> {
                    synchronized (gameState) {
                        if (gameState.isReady() && gameState.getStatus() == MatchStatus.LOBBY) {
                            // Status = BETWEEN_ROUNDS blocks spell casts during the countdown.
                            gameState.setStatus(MatchStatus.BETWEEN_ROUNDS);
                            System.out.println("Game navigating — countdown starting");

                            // Pattern-less StartGameMessage triggers navigation on the desktop.
                            registry.broadcast(playerIds, gson.toJson(new StartGameMessage()));

                            // Countdown ticks (game screen will be visible by the time these arrive).
                            scheduler.schedule(() -> registry.broadcast(playerIds, gson.toJson(new CountdownMessage(3))),
                                    1, TimeUnit.SECONDS);
                            scheduler.schedule(() -> registry.broadcast(playerIds, gson.toJson(new CountdownMessage(2))),
                                    2, TimeUnit.SECONDS);
                            scheduler.schedule(() -> registry.broadcast(playerIds, gson.toJson(new CountdownMessage(1))),
                                    3, TimeUnit.SECONDS);

                            // At t+4s: flip to IN_PROGRESS, send "Battle!" + per-player patterns.
                            scheduler.schedule(() -> {
                                synchronized (gameState) {
                                    if (gameState.getStatus() != MatchStatus.BETWEEN_ROUNDS) return;
                                    gameState.setStatus(MatchStatus.IN_PROGRESS);
                                    System.out.println("Game Started (round 1)");

                                    registry.broadcast(playerIds, gson.toJson(new CountdownMessage(0)));

                                    List<PatternGenerator.NamedShape> pool =
                                            PatternGenerator.shuffledPool(PatternGenerator.Difficulty.EASY, 64);
                                    List<Player> players = gameState.getPlayers();
                                    for (int i = 0; i < players.size(); i++) {
                                        PatternGenerator.NamedShape shape = pool.get(i % pool.size());
                                        players.get(i).setCurrentPattern(shape.points(), shape.name());
                                        registry.sendToSession(players.get(i).getId(),
                                                gson.toJson(new RoundStartMessage(shape.points(), shape.name())));
                                    }
                                }
                            }, 4, TimeUnit.SECONDS);
                        } else {
                            System.out.println("Game start interrupted");
                        }
                    }
                }, START_GAME_DELAY_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Core evaluation loop
    // -------------------------------------------------------------------------

    /**
     * All events that affect the game state must go through this method.
     * It runs the action, then checks whether the current round has ended.
     * If so it either starts the next round (best-of-3) or ends the match.
     */
    public void executeAndEvaluate(GameState gameState, List<String> playerIds,
                                   Consumer<List<CombatEventMessage>> gameAction) {
        synchronized (gameState) {
            List<CombatEventMessage> outEvents = new ArrayList<>();
            gameAction.accept(outEvents);

            if (gameState.getStatus() != MatchStatus.IN_PROGRESS) return;

            if (!gameState.isRoundOver()) {
                // Normal tick — just broadcast the updated state.
                registry.broadcast(playerIds,
                        gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), outEvents)));
                return;
            }

            // ---- Round ended ----
            gameState.setStatus(MatchStatus.BETWEEN_ROUNDS);

            // Award win to the surviving player (null means simultaneous death → draw round).
            String roundWinnerId = gameState.getRoundWinnerId();
            if (roundWinnerId != null) {
                gameState.getPlayer(roundWinnerId).addWin();
            }

            // Broadcast the final state of this round (updated wins, 0 HP on loser).
            registry.broadcast(playerIds,
                    gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), outEvents)));

            // Tell clients the round is over.
            registry.broadcast(playerIds, gson.toJson(new RoundOverMessage(roundWinnerId)));

            // Check if the match is won.
            boolean matchOver = roundWinnerId != null
                    && gameState.getPlayer(roundWinnerId).getWins() >= WINS_TO_WIN;

            if (matchOver) {
                gameState.setStatus(MatchStatus.GAME_OVER);
                System.out.println("MATCH OVER — winner: " + roundWinnerId);
                registry.broadcast(playerIds, gson.toJson(new GameOverMessage(roundWinnerId, GameOverReason.WIN)));
            } else {
                // Start the next round after a short inter-round break.
                System.out.println("Round over — scheduling next round. Winner of round: " + roundWinnerId);
                scheduleRoundStart(gameState, playerIds);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Round start sequence (called after a non-final round ends)
    // -------------------------------------------------------------------------

    private void scheduleRoundStart(GameState gameState, List<String> playerIds) {
        // 3-second silent pause after round ends, then countdown ticks: 3 at t+3s, 2 at t+4s, 1 at t+5s.
        scheduler.schedule(() -> registry.broadcast(playerIds, gson.toJson(new CountdownMessage(3))),
                3, TimeUnit.SECONDS);
        scheduler.schedule(() -> registry.broadcast(playerIds, gson.toJson(new CountdownMessage(2))),
                4, TimeUnit.SECONDS);
        scheduler.schedule(() -> registry.broadcast(playerIds, gson.toJson(new CountdownMessage(1))),
                5, TimeUnit.SECONDS);

        // At t+6s: reset state, send "Battle!" signal, broadcast reset state, send per-player patterns.
        scheduler.schedule(() -> {
            synchronized (gameState) {
                if (gameState.getStatus() != MatchStatus.BETWEEN_ROUNDS) {
                    System.out.println("Round start cancelled — status changed to " + gameState.getStatus());
                    return;
                }

                gameState.resetRound();
                gameState.setStatus(MatchStatus.IN_PROGRESS);

                System.out.println("New round started");

                // "Battle!" signal
                registry.broadcast(playerIds, gson.toJson(new CountdownMessage(0)));

                // Reset state broadcast
                registry.broadcast(playerIds,
                        gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), Collections.emptyList())));

                // Per-player new patterns via RoundStartMessage
                List<PatternGenerator.NamedShape> pool =
                        PatternGenerator.shuffledPool(PatternGenerator.Difficulty.EASY, 64);
                List<Player> players = gameState.getPlayers();
                for (int i = 0; i < players.size(); i++) {
                    PatternGenerator.NamedShape shape = pool.get(i % pool.size());
                    players.get(i).setCurrentPattern(shape.points(), shape.name());
                    registry.sendToSession(players.get(i).getId(),
                            gson.toJson(new RoundStartMessage(shape.points(), shape.name())));
                }
            }
        }, 6, TimeUnit.SECONDS);
    }

    // -------------------------------------------------------------------------
    // Delayed impact scheduling
    // -------------------------------------------------------------------------

    public void scheduleImpact(GameState gameState, List<String> playerIds, long delayMs,
                               Consumer<List<CombatEventMessage>> impactAction) {
        scheduler.schedule(() ->
                executeAndEvaluate(gameState, playerIds, impactAction), delayMs, TimeUnit.MILLISECONDS);
    }
}
