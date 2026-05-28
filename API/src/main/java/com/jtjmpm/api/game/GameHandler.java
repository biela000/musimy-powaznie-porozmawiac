package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.*;
import com.jtjmpm.api.model.GameStateStore;
import com.jtjmpm.api.model.PatternEngine.GestureToScore;
import com.jtjmpm.api.model.PatternEngine.PatternGenerator;
import com.jtjmpm.api.model.PatternEngine.RotationVectorParser;
import com.jtjmpm.api.model.PatternEngine.ShapeNormalizer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.geom.Point2D;

public class GameHandler extends WebSocketServer {
    private final ConcurrentHashMap<String, WebSocket> activeSessions = new ConcurrentHashMap<>();
    private final GameStateStore store;
    private final Gson gson;

    public GameHandler(InetSocketAddress address, GameStateStore store) {
        super(address);
        this.store = store;
        this.gson = new Gson();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String sessionId = getSessionId(conn);
        activeSessions.put(sessionId, conn);

        System.out.println("Connected, session ID: " + sessionId);

        conn.send(gson.toJson(new WelcomeMessage(sessionId)));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String sessionId = getSessionId(conn);
        store.removeSession(sessionId);
        activeSessions.remove(sessionId);

        System.out.println("Connection closed, session ID: " + sessionId);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        String sessionId = conn != null ? getSessionId(conn) : "unknown";
        System.err.println("Connection failed, session ID: " + sessionId + " " + ex.getMessage());
        if (conn != null) {
            store.removeSession(sessionId);
            activeSessions.remove(sessionId);
        }
    }

    @Override
    public void onStart() {
        System.out.println("GameHandler WebSocket server started on port " + getPort());
    }

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
            store.setPlayerReady(sessionId);

            GameState lobby = store.getPlayersLobby(sessionId);

            GameStateUpdateMessage responseMessage = new GameStateUpdateMessage(lobby);
            String jsonResponse = gson.toJson(responseMessage);

            if (lobby.getPlayer1Ready() && lobby.getPlayer2Ready()) {
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

    }

    private void handleJoinLobby(WebSocket conn, String lobbyName) {

    }

    private void handlePlayerMove(WebSocket conn, List<ControllerRotation> move) {
        String sessionId = getSessionId(conn);
        System.out.println("Receiving a move from: " + sessionId + " (size: " + move.size() + ")");

        try {
            RotationVectorParser parser = new RotationVectorParser();
            List<Point2D.Double> normalPoints = parser.processBatch(move);
            List<Point2D.Double> normalizedPoints = ShapeNormalizer.preProcess(normalPoints, 64, 3);
            List<Point2D.Double> circlePattern = PatternGenerator.createCircle(64);

            double accuracyScore = GestureToScore.getScore(circlePattern, move);

            System.out.println("Acurracy for session: " + sessionId + " equals: " + Math.round(accuracyScore * 100));
            MoveResultMessage resultMessage = new MoveResultMessage(normalizedPoints, accuracyScore, sessionId);
            String jsonResponse = gson.toJson(resultMessage);

            GameState lobby = store.getPlayersLobby(sessionId);

            WebSocket player1 = activeSessions.get(lobby.getPlayer1Id());
            WebSocket player2 = activeSessions.get(lobby.getPlayer2Id());

            player1.send(jsonResponse);
            player2.send(jsonResponse);
        } catch (Exception e) {
            System.err.println("Error while calcultaing score from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLeaveLobby(WebSocket conn) {
        // TODO
    }

    // UTILITIES

    private String getSessionId(WebSocket conn) {
        return conn.getRemoteSocketAddress().toString();
    }
}