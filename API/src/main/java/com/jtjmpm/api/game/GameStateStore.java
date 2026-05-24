package com.jtjmpm.api.game;

import com.jtjmpm.GameState;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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


    //HANDLING LOBBIES

    public boolean createLobby(String lobbyID, String sessionID){
        GameState newState = new GameState(sessionID);

        if(lobbies.containsKey(lobbyID)) return false;
        lobbies.put(lobbyID, newState);
        sessionToLobby.put(sessionID, lobbyID);
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

    public boolean removeSession(String sessionID) { //returns true if player was in a lobby and left successfully, false otherwise
        String lobbyId = sessionToLobby.remove(sessionID);
        // if host leaves, lobby closes, if the second player leaves, the lobby remains
        if (lobbyId != null) {
            GameState state = lobbies.get(lobbyId);
            if (state != null) {
                String p1 = state.getPlayer1Id();
                String p2 = state.getPlayer2Id();

                if (p1.equals(sessionID)) {
                    sessionToLobby.remove(p1);
                    lobbies.remove(lobbyId);
                } else if (p2.equals(sessionID)) sessionToLobby.remove(p2);
            }
            return true;
        }
        return false;
    }

    //GAMEPLAY LOGIC

    public boolean setPlayerReady(String sessionID) {
        String lobbyId = sessionToLobby.get(sessionID);
        if (lobbyId != null) {
            GameState state = lobbies.get(lobbyId);
            if (state != null) {
                //returns true if both players are ready
                return state.setPlayerReady(sessionID);
            }
        }
        return false;
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

    public List<String> getPlayersIdsInLobby(String lobbyId) {
        List<String> ids = new ArrayList<>();
        GameState state = lobbies.get(lobbyId);

        if (state != null) {
            if (state.getPlayer1Id() != null) ids.add(state.getPlayer1Id());
            if (state.getPlayer2Id() != null) ids.add(state.getPlayer2Id());
        }
        return ids;
    }

    public GameState getPlayersLobby(String sessionID){
        return lobbies.get(getLobbyIdForPlayer(sessionID));
    }
}
