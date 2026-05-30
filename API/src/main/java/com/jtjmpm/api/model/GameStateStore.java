package com.jtjmpm.api.model;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStateStore {
    private final ConcurrentHashMap<String, String> sessionToLobby = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, GameState> lobbies = new ConcurrentHashMap<>();

    public GameStateStore() {
    }

    public List<String> getPlayerIdsFromLobby(String lobbyName) {
        return lobbies.get(lobbyName).getPlayers().stream().map(Player::getId).toList();
    }

    public boolean createLobby(String lobbyName, String sessionId){
        GameState newState = new GameState(lobbyName, sessionId);

        if (lobbies.containsKey(lobbyName)) return false;
        lobbies.put(lobbyName, newState);
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

    public void removeSession(String sessionId) {
        String lobbyId = sessionToLobby.remove(sessionId);

        if (lobbyId != null) {
            GameState state = lobbies.get(lobbyId);

            if (state != null && state.getPlayers().isEmpty()) {
                lobbies.remove(lobbyId);
            }
        }
    }

    //GAMEPLAY LOGIC

    public void togglePlayerReady(String sessionId) {
        String lobbyId = sessionToLobby.get(sessionId);
        lobbies.get(lobbyId).getPlayer(sessionId).toggleReady();
    }

    public GameState applyDamage(String lobbyId, String targetSessionId, int damageAmount) {
        GameState state = lobbies.get(lobbyId);
        if (state != null) {
            state.applyDamage(targetSessionId, damageAmount);
            return state;
        }
        return null;
    }

    //GETTERS

    public String getLobbyIdForPlayer(String sessionID) {
        return sessionToLobby.get(sessionID);
    }

    public GameState getPlayersLobby(String sessionID){
        return lobbies.get(getLobbyIdForPlayer(sessionID));
    }
}
