package com.jtjmpm.api.service;

import com.jtjmpm.api.model.core .GameConstants;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.messages.MatchStatus;
import com.jtjmpm.api.model.core.Player;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameTickService {

    private final GameStateStore store;
    private final MatchSupervisor matchSupervisor;
    private final CombatEngine combatEngine;

    public GameTickService(GameStateStore store, MatchSupervisor matchSupervisor, CombatEngine combatEngine) {
        this.store = store;
        this.matchSupervisor = matchSupervisor;
        this.combatEngine = combatEngine;
    }

    @Scheduled(fixedRate = 1000 / GameConstants.TICKS_PER_SECOND)
    public void processServerTick() {
        List<GameState> activeGames = store.getAllLobbies();

        for (GameState gameState : activeGames) {

            if (gameState.getStatus() != MatchStatus.IN_PROGRESS) {
                continue;
            }

            List<String> playerIds = store.getPlayerIdsFromLobby(gameState.getName());

            matchSupervisor.executeAndEvaluate(gameState, playerIds, (outEvents) -> {
                for (Player player : gameState.getPlayers()) {
                    player.processEffects(gameState, combatEngine, outEvents);
                }
            });
        }
    }
}