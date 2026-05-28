package com.jtjmpm.api.controller;

import com.google.gson.Gson;
import com.jtjmpm.GameState;
import com.jtjmpm.Player;
import com.jtjmpm.api.model.GameStateStore;
import com.jtjmpm.api.model.SessionRegistry;
import com.jtjmpm.api.utils.SocketUtils;
import com.jtjmpm.messages.*;
import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import com.jtjmpm.MessageType;

import java.util.List;

@Component
public class LobbyController implements MessageController {
    private final GameStateStore store;
    private final SessionRegistry registry;
    private final Gson gson;

    public LobbyController(GameStateStore store, SessionRegistry registry, Gson gson) {
        this.store = store;
        this.registry = registry;
        this.gson = gson;
    }

    @Override
    public List<MessageType> supportedTypes() {
        return List.of(
                MessageType.CREATE_LOBBY,
                MessageType.JOIN_LOBBY,
                MessageType.LEAVE_LOBBY
        );
    }

    @Override
    public void handle(WebSocket conn, String rawJson) {
        WsMessage message = gson.fromJson(rawJson, WsMessage.class);

        switch (message.type) {
            case MessageType.CREATE_LOBBY -> handleCreate(conn, rawJson);
            case MessageType.JOIN_LOBBY -> handleJoin(conn, rawJson);
            case MessageType.LEAVE_LOBBY -> handleLeave(conn, rawJson);
        }
    }

    private void handleCreate(WebSocket conn, String rawJson) {
        LobbyMessage lobbyMessage = gson.fromJson(rawJson, LobbyMessage.class);
        String sessionId = SocketUtils.getSessionId(conn);
        System.out.println(sessionId + " is creating a lobby named " + lobbyMessage.lobbyName);

        try {
            if (store.createLobby(lobbyMessage.lobbyName, sessionId)) {
                LobbyJoinedMessage responseMessage = new LobbyJoinedMessage(lobbyMessage.lobbyName);
                conn.send(gson.toJson(responseMessage));
            } else {
                LobbyErrorMessage errorMessage = new LobbyErrorMessage(
                        "Lobby named " + lobbyMessage.lobbyName + " already exists..."
                );
                String jsonResponse = gson.toJson(errorMessage);
                conn.send(jsonResponse);
            }
        } catch (Exception e) {
            System.err.println("Error while creating lobby from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoin(WebSocket conn, String rawJson) {
        LobbyMessage lobbyMessage = gson.fromJson(rawJson, LobbyMessage.class);
        String sessionId = SocketUtils.getSessionId(conn);
        System.out.println(sessionId + " is joining a lobby named " + lobbyMessage.lobbyName);

        try {
            if (store.connectToLobby(lobbyMessage.lobbyName, sessionId)) {
                LobbyJoinedMessage responseMessage = new LobbyJoinedMessage(lobbyMessage.lobbyName);
                conn.send(gson.toJson(responseMessage));

                GameState lobby = store.getPlayersLobby(sessionId);
                List<String> playerIds = lobby.getPlayers().stream().map(Player::getId).toList();

                GameStateUpdateMessage updateMessage = new GameStateUpdateMessage(lobby);
                registry.broadcast(playerIds, gson.toJson(updateMessage));
            } else {
                LobbyErrorMessage errorMessage = new LobbyErrorMessage(
                        "Lobby named " + lobbyMessage.lobbyName + " is full or doesn't exist..."
                );
                String jsonResponse = gson.toJson(errorMessage);
                conn.send(jsonResponse);
            }
        } catch (Exception e) {
            System.err.println("Error while creating lobby from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
