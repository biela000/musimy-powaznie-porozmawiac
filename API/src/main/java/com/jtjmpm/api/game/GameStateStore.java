package com.jtjmpm.api.game;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStateStore {
    private final ConcurrentHashMap<String, String> sessionToLobby = new ConcurrentHashMap<>(); //Stores lobby ids for sessions
    private final ConcurrentHashMap<String, GameState> lobbies = new ConcurrentHashMap<>(); //Stores GameStates for lobbies

    /*
    public GameState getOrCreate(String battleId) {
        return battles.computeIfAbsent(battleId, id -> new GameState(id));
    }
    */


    //HANDLING LOBBIES

    public void CreateLobby(String LobbyID, String sessionID){

    }

    public void ConnectToLobby(String LobbyID, String sessionID) {

    }

    public void removeSession(String hostID) {

    }
}
