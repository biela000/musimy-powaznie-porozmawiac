package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.ControllerRotation;
import com.jtjmpm.LobbyMessage;
import com.jtjmpm.PlayerMoveMessage;
import com.jtjmpm.WsMessage;
import com.jtjmpm.Point2D;
import com.jtjmpm.MoveResultMessage;
import com.jtjmpm.api.game.game_logic.GestureToScore;
import com.jtjmpm.api.game.game_logic.PatternGenerator;
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
                    handleCreateLobby(conn, ((LobbyMessage) message).lobbyName);
                    break;
                case "JOIN_LOBBY":
                    handleJoinLobby(conn, ((LobbyMessage) message).lobbyName);
                    break;
                case "PLAYER_MOVE":
                    PlayerMoveMessage playerMoveMessage = gson.fromJson(rawJson, PlayerMoveMessage.class);
                    handlePlayerMove(conn, playerMoveMessage.move);
                    break;
                case "LEAVE_LOBBY":
                    handleLeaveLobby(conn);
                    break;
                default:
                    System.out.println("Unknown message type: " + message.type);
            }
        } catch (Exception e) {
            System.err.println("Parsing error, session ID: " + getSessionId(conn) + ": " + e.getMessage());
        }
    }

    // HELPER FUNCTIONS FOR HANDLING MESSAGES

    private void handleCreateLobby(WebSocket conn, String lobbyName) {
        // TODO
        System.out.println("Creating a lobby: " + lobbyName + " is creating a lobby");
    }

    private void handleJoinLobby(WebSocket conn, String lobbyName) {
        // TODO
        System.out.println("Joining a lobby: " + lobbyName + " is joining a lobby");
    }

    private void handlePlayerMove(WebSocket conn, List<ControllerRotation> move) {
        // TODO
        // Dodajecie klase MoveResultMessage
        // Obliczacie to co macie tutaj
        // Wynik przesylacie komenda:
        // conn.send(MoveResultMessage);
        String sessionId = getSessionId(conn);
        System.out.println("Receiving a move from: " + sessionId + " (size: " + move.size() + ")");

        try {
            List<Point2D> circlePattern = PatternGenerator.createCircle(64);

            double accuracyScore = GestureToScore.getScore(circlePattern, move);

            System.out.println("Acurracy for session: " + sessionId + " equals: " + accuracyScore);

            MoveResultMessage resultMessage = new MoveResultMessage(accuracyScore);

            String jsonResponse = gson.toJson(resultMessage);
            conn.send(jsonResponse);

            //TODO
            //Update game state

        } catch (Exception e) {
            System.err.println("Error while calcultaing score from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLeaveLobby(WebSocket conn) {
        // TODO
        System.out.println("Player with session ID: " + getSessionId(conn) + " is leaving his lobby");
    }

    // UTILITIES

    private String getSessionId(WebSocket conn) {
        return conn.getRemoteSocketAddress().toString();
    }
}