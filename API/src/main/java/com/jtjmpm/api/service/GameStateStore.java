package com.jtjmpm.api.service;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStateStore {
    private final ConcurrentHashMap<String, String> sessionToLobby = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, GameState> lobbies = new ConcurrentHashMap<>();

    public boolean createLobby(String lobbyName, String sessionId) {
        GameState newState = new GameState(lobbyName, sessionId);

        if (lobbies.putIfAbsent(lobbyName, newState) != null) return false;
        sessionToLobby.put(sessionId, lobbyName);
        return true;
    }

    public boolean connectToLobby(String lobbyId, String sessionId) {
        GameState state = lobbies.get(lobbyId);
        if (state != null) {
            boolean joined = state.addPlayer(sessionId);
            if (joined) {
                sessionToLobby.put(sessionId, lobbyId);
                return true;
            }
        }
        return false;
    }

    public GameState removeSession(String sessionId) {
        String lobbyId = sessionToLobby.remove(sessionId);
        if (lobbyId == null) return null;

        GameState state = lobbies.get(lobbyId);
        if (state == null) return null;

        synchronized (state) {
            state.removePlayer(sessionId);
            if (state.getPlayers().isEmpty()) {
                lobbies.remove(lobbyId);
                return null;
            }
            state.resetAllPlayersReady();
            return state;
        }
    }

    public List<String> destroyLobbyAndGetPlayers(String sessionId) {
        String lobbyId = sessionToLobby.get(sessionId);
        if (lobbyId == null) return Collections.emptyList();

        GameState state = lobbies.remove(lobbyId);
        if (state == null) return Collections.emptyList();

        List<String> playerIds = new ArrayList<>();
        synchronized (state) {
            for (Player player : state.getPlayers()) {
                String id = player.getId();
                sessionToLobby.remove(id);
                playerIds.add(id);
            }
        }
        return playerIds;
    }

    public String getLobbyIdForPlayer(String sessionID) {
        return sessionToLobby.get(sessionID);
    }

    public GameState getPlayersLobby(String sessionID) {
        return lobbies.get(getLobbyIdForPlayer(sessionID));
    }

    public List<String> getPlayerIdsFromLobby(String lobbyName) {
        return lobbies.get(lobbyName).getPlayers().stream().map(Player::getId).toList();
    }

    public List<GameState> getAllLobbies() {
        return lobbies.values().stream().toList();
    }
}
