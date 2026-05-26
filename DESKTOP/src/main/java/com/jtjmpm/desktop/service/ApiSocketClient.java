package com.jtjmpm.desktop.service;

import com.google.gson.Gson;
import com.jtjmpm.messages.WsMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class ApiSocketClient {
    private static ApiSocketClient instance;

    private final Gson gson = new Gson();
    private WebSocketClient client;
    private Consumer<String> onMessageCallback;
    private String myPlayerId;

    public String getMyPlayerId() {
        return myPlayerId;
    }

    public void setMyPlayerId(String s) {
        myPlayerId = s;
    }

    public static ApiSocketClient getInstance() {
        if (instance == null) {
            instance = new ApiSocketClient();
        }
        return instance;
    }

    public static void handleUnknownMessage(WsMessage message) {
        System.out.println("Unknown message type: " + message.type);
    }

    public void setOnMessageCallback(Consumer<String> callback) {
        this.onMessageCallback = callback;
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
                    System.out.println("API message: " + message);

                    if (onMessageCallback != null) {
                        onMessageCallback.accept(message);
                    }

                    System.out.println("API message received");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from API: " + reason);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
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
