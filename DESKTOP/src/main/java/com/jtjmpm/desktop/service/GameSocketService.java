package com.jtjmpm.desktop.service;

import com.google.gson.Gson;
import com.jtjmpm.PlayerMoveMessage;
import com.jtjmpm.WsMessage;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class GameSocketService extends WebSocketServer {
    private WebSocket connectedClient = null;
    private final Gson gson = new Gson();

    public GameSocketService(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (connectedClient != null) {
            conn.close(4001, "Server already has a client.");
            System.out.println("Rejected extra connection from: " + conn.getRemoteSocketAddress());
            return;
        }
        connectedClient = conn;
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn == connectedClient) {
            connectedClient = null;
            System.out.println("Client disconnected: " + reason);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case "PLAYER_MOVE" -> {
                PlayerMoveMessage move = gson.fromJson(message, PlayerMoveMessage.class);
                handlePlayerMove(move);
            }
            default -> System.out.println("Unknown message type: " + base.type);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Service started on port: " + getPort());
    }

    private void handlePlayerMove(PlayerMoveMessage move) {
        System.out.println("Received " + move.move.size() + " rotation samples");
    }

    public boolean hasClient() {
        return connectedClient != null;
    }
}
