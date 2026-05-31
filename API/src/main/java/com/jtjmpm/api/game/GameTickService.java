package com.jtjmpm.api.game;

import com.jtjmpm.api.model.GameState;
import com.jtjmpm.api.model.GameStateStore;
import com.jtjmpm.api.model.Player;
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

    @Scheduled(fixedRate = 1000)
    public void processServerTick() {
        List<GameState> activeGames = store.getAllActiveLobbies();

        for (GameState gameState : activeGames) {
            List<String> playerIds = store.getPlayerIdsFromLobby(gameState.getName());

            matchSupervisor.executeAndEvaluate(gameState, playerIds, () -> {
                for (Player player : gameState.getPlayers()) {
                    combatEngine.applyManaRegen(gameState, player.getId(), 10.0);
                    player.processEffects(gameState, combatEngine);
                }
            });
        }
    }
}