package com.jtjmpm.desktop.service;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.messages.PlayerMoveMessage;
import com.jtjmpm.messages.WsMessage;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class GameSocketService extends WebSocketServer {
    private WebSocket connectedClient = null;
    private final Gson gson = new Gson();
    private static GameSocketService instance;
    private static int port = 8080;

    private Runnable onClientConnected;
    private Runnable onClientDisconnected;

    public static void setPort(int p) {
        port = p;
    }

    public static GameSocketService getInstance() {
        if (instance == null) {
            instance = new GameSocketService();
        }
        return instance;
    }

    private GameSocketService() {
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
        if (onClientConnected != null) {
            onClientConnected.run();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn == connectedClient) {
            connectedClient = null;
            System.out.println("Client disconnected: " + reason);

            if (onClientDisconnected != null) {
                onClientDisconnected.run();
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case MessageType.PLAYER_MOVE -> {
                PlayerMoveMessage move = gson.fromJson(message, PlayerMoveMessage.class);
                handlePlayerMove(move);
            }
            default -> System.out.println("Unknown message type: " + base.type);
        }
    }

    @Override
    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        return super.getLocalSocketAddress(conn);
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
        for (int i = 0; i < move.move.size(); i++) {
            System.out.println(move.move.get(i).x + " " + move.move.get(i).y + " " + move.move.get(i).z + " " + move.move.get(i).scalar);
        }
        ApiSocketClient.getInstance().send(move);
    }

    public boolean hasClient() {
        return connectedClient != null;
    }

    public void setOnClientConnected(Runnable callback) {
        this.onClientConnected = callback;
    }

    public void setOnClientDisconnected(Runnable callback) {
        this.onClientDisconnected = callback;
    }


}
