package com.jtjmpm.desktop.service;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ApiSocketClient {
    private static ApiSocketClient instance;
    private WebSocketClient client;
    private final Gson gson = new Gson();

    public static ApiSocketClient getInstance() {
        if (instance == null) {
            instance = new ApiSocketClient();
        }
        return instance;
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
