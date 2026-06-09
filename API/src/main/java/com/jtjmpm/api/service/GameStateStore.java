package com.jtjmpm.api.service;


import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStateStore {
    private final ConcurrentHashMap<String, String> sessionToLobby = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, GameState> lobbies = new ConcurrentHashMap<>();

    public GameStateStore() {
    }

    public boolean createLobby(String lobbyName, String sessionId){
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

    /**
     * Removes the session from its lobby. If the lobby still has players, resets all their
     * ready states and returns the lobby GameState so the caller can broadcast the update.
     * Returns null if the lobby is now empty or the session wasn't in one.
     */
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

    //GETTERS

    public String getLobbyIdForPlayer(String sessionID) {
        return sessionToLobby.get(sessionID);
    }

    public GameState getPlayersLobby(String sessionID){
        return lobbies.get(getLobbyIdForPlayer(sessionID));
    }

    public List<String> getPlayerIdsFromLobby(String lobbyName) {
        return lobbies.get(lobbyName).getPlayers().stream().map(Player::getId).toList();
    }

    public List<GameState> getAllLobbies() {
        return lobbies.values().stream().toList();
    }
}
