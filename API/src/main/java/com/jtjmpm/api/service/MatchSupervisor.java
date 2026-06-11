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

    private static final int WINS_TO_WIN = 2;

    private static final int START_GAME_DELAY_SECONDS = 1;

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

                            gameState.setStatus(MatchStatus.BETWEEN_ROUNDS);
                            System.out.println("Game navigating — countdown starting");

                            registry.broadcast(playerIds, gson.toJson(new StartGameMessage()));

                            scheduleCountdown(playerIds, 2);

                            scheduler.schedule(() -> {
                                synchronized (gameState) {
                                    if (gameState.getStatus() != MatchStatus.BETWEEN_ROUNDS) return;
                                    gameState.setStatus(MatchStatus.IN_PROGRESS);
                                    System.out.println("Game Started (round 1)");

                                    registry.broadcast(playerIds, gson.toJson(new CountdownMessage(0)));

                                    assignNewPatterns(gameState);
                                }
                            }, 5, TimeUnit.SECONDS);
                        } else {
                            System.out.println("Game start interrupted");
                        }
                    }
                }, START_GAME_DELAY_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    public void executeAndEvaluate(GameState gameState, List<String> playerIds,
                                   Consumer<List<CombatEventMessage>> gameAction) {
        synchronized (gameState) {
            List<CombatEventMessage> outEvents = new ArrayList<>();
            gameAction.accept(outEvents);

            if (gameState.getStatus() != MatchStatus.IN_PROGRESS) return;

            if (!gameState.isRoundOver()) {

                registry.broadcast(playerIds,
                        gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), outEvents)));
                return;
            }

            gameState.setStatus(MatchStatus.BETWEEN_ROUNDS);

            String roundWinnerId = gameState.getRoundWinnerId();
            if (roundWinnerId != null) {
                gameState.getPlayer(roundWinnerId).addWin();
            }

            registry.broadcast(playerIds,
                    gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), outEvents)));

            registry.broadcast(playerIds, gson.toJson(new RoundOverMessage(roundWinnerId)));

            boolean matchOver = roundWinnerId != null
                    && gameState.getPlayer(roundWinnerId).getWins() >= WINS_TO_WIN;

            if (matchOver) {
                gameState.setStatus(MatchStatus.GAME_OVER);
                System.out.println("MATCH OVER — winner: " + roundWinnerId);
                registry.broadcast(playerIds, gson.toJson(new GameOverMessage(roundWinnerId, GameOverReason.WIN)));
            } else {

                System.out.println("Round over — scheduling next round. Winner of round: " + roundWinnerId);
                scheduleRoundStart(gameState, playerIds);
            }
        }
    }

    public void restartMatch(GameState gameState, List<String> playerIds) {
        synchronized (gameState) {
            System.out.println("Match returning to lobby: " + gameState.getName());

            for (Player player : gameState.getPlayers()) {
                player.setWins(0);
                player.clearSpellLoadout();
            }

            gameState.resetAllPlayersReady();
            gameState.setStatus(MatchStatus.LOBBY);
            gameState.resetRound();

            registry.broadcast(playerIds, gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), Collections.emptyList())));
        }
    }

    private void scheduleRoundStart(GameState gameState, List<String> playerIds) {
        scheduleCountdown(playerIds, 3);

        scheduler.schedule(() -> {
            synchronized (gameState) {
                if (gameState.getStatus() != MatchStatus.BETWEEN_ROUNDS) {
                    System.out.println("Round start cancelled — status changed to " + gameState.getStatus());
                    return;
                }

                gameState.resetRound();
                gameState.setStatus(MatchStatus.IN_PROGRESS);

                System.out.println("New round started");

                registry.broadcast(playerIds, gson.toJson(new CountdownMessage(0)));

                registry.broadcast(playerIds,
                        gson.toJson(new GameStateUpdateMessage(gameState.toDTO(), Collections.emptyList())));

                assignNewPatterns(gameState);
            }
        }, 6, TimeUnit.SECONDS);
    }

    private void scheduleCountdown(List<String> playerIds, long firstDelaySeconds) {
        for (int i = 0; i < 3; i++) {
            int value = 3 - i;
            scheduler.schedule(() -> registry.broadcast(playerIds, gson.toJson(new CountdownMessage(value))),
                    firstDelaySeconds + i, TimeUnit.SECONDS);
        }
    }

    private void assignNewPatterns(GameState gameState) {
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

    public void scheduleImpact(GameState gameState, List<String> playerIds, long delayMs,
                               Consumer<List<CombatEventMessage>> impactAction) {
        scheduler.schedule(() ->
                executeAndEvaluate(gameState, playerIds, impactAction), delayMs, TimeUnit.MILLISECONDS);
    }
}
