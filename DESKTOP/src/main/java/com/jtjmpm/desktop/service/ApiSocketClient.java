package com.jtjmpm.desktop.service;

import com.google.gson.Gson;
import com.jtjmpm.WsMessage;
import com.jtjmpm.ShapeMessage;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class ApiSocketClient {
    private static ApiSocketClient instance;
    private WebSocketClient client;
    private final Gson gson = new Gson();

    private Consumer<ShapeMessage> onShapeReceived;

    public static ApiSocketClient getInstance() {
        if (instance == null) {
            instance = new ApiSocketClient();
        }
        return instance;
    }

    public void setOnShapeReceived(Consumer<ShapeMessage> callback) {
        this.onShapeReceived = callback;
    }

    public void connect(String url, Runnable onConnected) {
        try {
            client = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("Connected to API");
                    if (onConnected != null) onConnected.run();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("API message received");
                    try {
                        WsMessage base = gson.fromJson(message, WsMessage.class);
                        if (base == null || base.type == null) return;
                        switch (base.type) {
                            case "SHAPE" -> handleShape(message);
                            case "MOVE_RESULT" -> handleMoveResult(message);
                            default -> System.out.println("Unknown message type: " + base.type);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse incoming WebSocket message");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from API: " + reason);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }

                private void handleShape(String message){
                    ShapeMessage shapeMsg = gson.fromJson(message, ShapeMessage.class);
                    if (onShapeReceived != null && shapeMsg != null) {
                        Platform.runLater(() -> onShapeReceived.accept(shapeMsg));
                    }
                }

                private void handleMoveResult(String message){
                    System.out.println("Message: " + message);
                }
            };
            new Thread(() -> {
                try { client.connectBlocking(); }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void send(Object message) {
        if (client != null && client.isOpen()) {
            client.send(gson.toJson(message));
        }
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
