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
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LobbyController implements MessageController {
    private final static int START_GAME_DELAY_SECONDS = 5;

    private final GameStateStore store;
    private final SessionRegistry registry;
    private final Gson gson;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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
                MessageType.LEAVE_LOBBY,
                MessageType.TOGGLE_READY
        );
    }

    @Override
    public void handle(WebSocket conn, String rawJson) {
        WsMessage message = gson.fromJson(rawJson, WsMessage.class);

        switch (message.type) {
            case MessageType.CREATE_LOBBY -> handleCreate(conn, rawJson);
            case MessageType.JOIN_LOBBY -> handleJoin(conn, rawJson);
            case MessageType.LEAVE_LOBBY -> handleLeave(conn, rawJson);
            case MessageType.TOGGLE_READY -> handleToggleReady(conn, rawJson);
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
                List<String> playerIds = store.getPlayerIdsFromLobby(lobby.getName()).stream()
                        .filter(id -> !id.equals(sessionId))
                        .toList();

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

    private void handleLeave(WebSocket conn, String rawJson) {
        String sessionId = SocketUtils.getSessionId(conn);
        System.out.println("Player with session ID: " + sessionId + " is leaving his lobby");

        try {
            if(store.removeSession(sessionId)){
                GameState lobby = store.getPlayersLobby(sessionId);

                LobbyLeftMessage responseMessage = new LobbyLeftMessage(sessionId);
                String jsonResponse = gson.toJson(responseMessage);

                for (Player player : lobby.getPlayers()){
                    if(Objects.equals(player.getId(), sessionId)) continue;
                    registry.get(player.getId()).send(jsonResponse);
                }
                conn.send(jsonResponse);

            }
            else{
                LobbyErrorMessage errorMessage = new LobbyErrorMessage("Player " + sessionId + "isn't in any lobby or there was an error with his lobby...");
                String jsonResponse = gson.toJson(errorMessage);
                conn.send(jsonResponse);
            }
        } catch (Exception e) {
            System.err.println("Error while leaving lobby: from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleToggleReady(WebSocket conn, String rawJson) {
        String sessionId = SocketUtils.getSessionId(conn);
        System.out.println("Toggling ready state of session: " + sessionId + " ...");

        try {
            store.togglePlayerReady(sessionId);

            GameState lobby = store.getPlayersLobby(sessionId);
            List<String> playerIds = store.getPlayerIdsFromLobby(lobby.getName());

            GameStateUpdateMessage responseMessage = new GameStateUpdateMessage(lobby);
            registry.broadcast(playerIds, gson.toJson(responseMessage));

            if (lobby.isReady()) {
                StartGameMessage startGameMessage = new StartGameMessage();

                scheduler.schedule(() -> {
                    if (lobby.isReady()) {
                        registry.broadcast(playerIds, gson.toJson(startGameMessage));
                    }
                }, START_GAME_DELAY_SECONDS, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            System.err.println("Error while toggling ready state from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
