package com.jtjmpm.api.controller;

import com.google.gson.Gson;
import com.jtjmpm.api.model.*;
import com.jtjmpm.api.utils.SocketUtils;
import com.jtjmpm.messages.*;
import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import com.jtjmpm.MessageType;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LobbyController implements MessageController {
    private final static int START_GAME_DELAY_SECONDS = 5;

    private final GameStateStore store;
    private final SessionRegistry registry;
    private final SpellRegistry spellRegistry;
    private final Gson gson;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LobbyController(GameStateStore store, SessionRegistry registry, Gson gson, SpellRegistry spellRegistry) {
        this.store = store;
        this.registry = registry;
        this.gson = gson;
        this.spellRegistry = spellRegistry;
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

                GameStateUpdateMessage updateMessage = new GameStateUpdateMessage(lobby.toDTO());
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
        System.out.println("Player with session ID: " + SocketUtils.getSessionId(conn) + " is leaving his lobby");
    }

    private void handleToggleReady(WebSocket conn, String rawJson) {
        String sessionId = SocketUtils.getSessionId(conn);
        System.out.println("Toggling ready state of session: " + sessionId + " ...");

        try {
            ReadyMessage readyMsg = gson.fromJson(rawJson, ReadyMessage.class);

            GameState lobby = store.getPlayersLobby(sessionId);
            if(lobby == null) return;

            Player player = lobby.getPlayer(sessionId);

            if (!player.isReady()) {
                List<String> spells = readyMsg.selectedSpells;

                if (spells == null || spells.size() != 4) {
                    System.err.println("Ready failed, not a valid loadout " + sessionId);
                    return;
                }

                for (String spellId : spells) {
                    if (spellRegistry.getSpell(spellId) == null) {
                        System.err.println("Ready failed, not a valid spell: " + spellId + "' by: " + sessionId);
                        return;
                    }
                }

                player.setSpellLoadout(spells);
            }

            player.toggleReady();

            List<String> playerIds = store.getPlayerIdsFromLobby(lobby.getName());

            GameStateUpdateMessage responseMessage = new GameStateUpdateMessage(lobby.toDTO());
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
