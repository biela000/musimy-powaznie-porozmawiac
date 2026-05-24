package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.*;
import com.jtjmpm.api.game.game_logic.GestureToScore;
import com.jtjmpm.api.game.game_logic.PatternGenerator;
import com.jtjmpm.api.game.game_logic.RotationVectorParser;
import com.jtjmpm.api.game.game_logic.ShapeNormalizer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GameHandler extends WebSocketServer {

    private final ConcurrentHashMap<String, WebSocket> activeSessions = new ConcurrentHashMap<>();
    private final GameStateStore store;
    private final Gson gson;

    public GameHandler(InetSocketAddress address, GameStateStore store) {
        super(address);
        this.store = store;
        this.gson = new Gson();
    }

    // CONNECTIONS

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String sessionId = getSessionId(conn);
        System.out.println("Connected, session ID: " + sessionId);
        activeSessions.put(sessionId, conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String sessionId = getSessionId(conn);
        System.out.println("Connection closed, session ID: " + sessionId);
        activeSessions.remove(sessionId);
        store.removeSession(sessionId);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        String sessionId = conn != null ? getSessionId(conn) : "unknown";
        System.err.println("Connection failed, session ID: " + sessionId + " " + ex.getMessage());
        if (conn != null) {
            activeSessions.remove(sessionId);
            store.removeSession(sessionId);
        }
    }

    @Override
    public void onStart() {
        System.out.println("GameHandler WebSocket server started on port " + getPort());
    }

    // HANDLING MESSAGES

    @Override
    public void onMessage(WebSocket conn, String rawJson) {
        try {
            WsMessage message = gson.fromJson(rawJson, WsMessage.class);

            switch (message.type) {
                case "CREATE_LOBBY":
                    LobbyMessage createLobbyMessage = gson.fromJson(rawJson, LobbyMessage.class);
                    handleCreateLobby(conn, createLobbyMessage.lobbyName);
                    break;
                case "JOIN_LOBBY":
                    LobbyMessage joinLobbyMessage = gson.fromJson(rawJson, LobbyMessage.class);
                    handleJoinLobby(conn, joinLobbyMessage.lobbyName);
                    break;
                case "PLAYER_MOVE":
                    PlayerMoveMessage playerMoveMessage = gson.fromJson(rawJson, PlayerMoveMessage.class);
                    handlePlayerMove(conn, playerMoveMessage.move);
                    break;
                case "LEAVE_LOBBY":
                    handleLeaveLobby(conn);
                    break;
                case "TOGGLE_READY":
                    handlePlayerReady(conn);
                    break;
                default:
                    System.out.println("Unknown message type: " + message.type);
            }
        } catch (Exception e) {
            System.err.println("Parsing error, session ID: " + getSessionId(conn) + ": " + e.getMessage());
        }
    }

    private void handlePlayerReady(WebSocket conn){
        String sessionId = getSessionId(conn);
        System.out.println("Toggling ready state of session: " + sessionId + " ...");

        try {
            boolean areBothPlayersReady = store.setPlayerReady(sessionId);

            GameState lobby = store.getPlayersLobby(sessionId);

            GameStateUpdateMessage responseMessage = new GameStateUpdateMessage(lobby);
            String jsonResponse = gson.toJson(responseMessage);

            if (areBothPlayersReady) {
                WebSocket player1 = activeSessions.get(lobby.getPlayer1Id());
                WebSocket player2 = activeSessions.get(lobby.getPlayer2Id());

                player1.send(gson.toJson(new StartGameMessage()));
                player2.send(gson.toJson(new StartGameMessage()));
            }

            conn.send(jsonResponse);
        } catch (Exception e) {
            System.err.println("Error while toggling ready state from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleCreateLobby(WebSocket conn, String lobbyName) {
        String sessionId = getSessionId(conn);
        System.out.println("Creating a lobby: " + lobbyName + " " + sessionId + " is creating a lobby");

        try {
            if (store.createLobby(lobbyName, sessionId)) {
                LobbyJoinedMessage responseMessage = new LobbyJoinedMessage(lobbyName, store.getPlayersLobby(sessionId));
                String jsonResponse = gson.toJson(responseMessage);
                conn.send(jsonResponse);
            } else {
                LobbyErrorMessage errorMessage = new LobbyErrorMessage("Lobby named " + lobbyName + " already exists...");
                String jsonResponse = gson.toJson(errorMessage);
                conn.send(jsonResponse);
            }
        } catch (Exception e) {
            System.err.println("Error while creating lobby from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoinLobby(WebSocket conn, String lobbyName) {
        String sessionId = getSessionId(conn);
        System.out.println("Joining a lobby: " + lobbyName + " " + sessionId + " is joining a lobby");

        try {
            if (store.connectToLobby(lobbyName, sessionId)) {
                LobbyJoinedMessage responseMessage = new LobbyJoinedMessage(lobbyName, store.getPlayersLobby(sessionId));
                String jsonResponse = gson.toJson(responseMessage);
                conn.send(jsonResponse);
            } else {
                LobbyErrorMessage errorMessage = new LobbyErrorMessage("Lobby named " + lobbyName + " is full or doesn't exist...");
                String jsonResponse = gson.toJson(errorMessage);
                conn.send(jsonResponse);
            }
        } catch (Exception e) {
            System.err.println("Error while creating lobby from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePlayerMove(WebSocket conn, List<ControllerRotation> move) {
        String sessionId = getSessionId(conn);
        System.out.println("Receiving a move from: " + sessionId + " (size: " + move.size() + ")");

        try {
            RotationVectorParser parser = new RotationVectorParser();
            List<Point2D> normalPoints = parser.processBatch(move);
            List<Point2D> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 3);
            List<Point2D> circlePattern = PatternGenerator.createCircle(64);

            double accuracyScore = GestureToScore.getScore(circlePattern, move);

            System.out.println("Accuracy for session: " + sessionId + " equals: " + Math.round(accuracyScore * 100));
            MoveResultMessage resultMessage = new MoveResultMessage(normalizedPoints, accuracyScore);
            String jsonResponse = gson.toJson(resultMessage);
            conn.send(jsonResponse);
        } catch (Exception e) {
            System.err.println("Error while calculating score from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLeaveLobby(WebSocket conn) {
        String sessionId = getSessionId(conn);
        System.out.println("Player with session ID: " + getSessionId(conn) + " is leaving his lobby");

        try {
            if(store.removeSession(sessionId)){
                GameState lobby = store.getPlayersLobby(sessionId);
                String lobbyName = store.getLobbyIdForPlayer(sessionId);

                LobbyLeftMessage responseMessage = new LobbyLeftMessage(lobbyName, lobby);
                String jsonResponse = gson.toJson(responseMessage);


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



    // UTILITIES

    private String getSessionId(WebSocket conn) {
        return conn.getRemoteSocketAddress().toString();
    }
}