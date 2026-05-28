package com.jtjmpm.api.model;

import com.jtjmpm.GameState;
import com.jtjmpm.Player;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStateStore {
    private final ConcurrentHashMap<String, String> sessionToLobby = new ConcurrentHashMap<>(); //Stores lobby ids for sessions
    private final ConcurrentHashMap<String, GameState> lobbies = new ConcurrentHashMap<>(); //Stores GameStates for lobbies
    private final ResourcePatternResolver resourcePatternResolver;

    public GameStateStore(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<String> getPlayerIdsFromLobby(String lobbyName) {
        return lobbies.get(lobbyName).getPlayers().stream().map(Player::getId).toList();
    }

    //HANDLING LOBBIES

    public boolean createLobby(String lobbyName, String sessionId){
        GameState newState = new GameState(lobbyName, sessionId);

        if(lobbies.containsKey(lobbyName)) return false;
        lobbies.put(lobbyName, newState);
        sessionToLobby.put(sessionId, lobbyName);
        return true;
    }

    public boolean connectToLobby(String lobbyID, String sessionID) {
        GameState state = lobbies.get(lobbyID);
        if (state != null) {
            //joined is false if the lobby is full and player failed to join
            boolean joined = state.addPlayer2(sessionID);
            if (joined) {
                sessionToLobby.put(sessionID, lobbyID);
                return true;
            }
        }
        return false;
    }

    public void removeSession(String sessionID) {
        String lobbyId = sessionToLobby.remove(sessionID);

        //If any of the players disconnects from the lobby we close it (might have to change this logic later)
        if (lobbyId != null) {
            GameState state = lobbies.get(lobbyId);

            if (state != null) {
                String p1 = state.getPlayer1Id();
                String p2 = state.getPlayer2Id();

                if (p1 != null) sessionToLobby.remove(p1);
                if (p2 != null) sessionToLobby.remove(p2);

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
