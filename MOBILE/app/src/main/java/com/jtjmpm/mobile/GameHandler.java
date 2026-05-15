package com.jtjmpm.mobile;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class GameHandler {
    private static GameHandler instance;
    private WebSocketClient client;
    private GameHandler() {}

    public static GameHandler getInstance() {
        if (instance == null) {
            instance = new GameHandler();
        }
        return instance;
    }

    public void connect(String url) {
        if (client != null && client.isOpen()) return;

        try {
            client = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {

                }

                @Override
                public void onMessage(String message) {

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                }

                @Override
                public void onError(Exception ex) {

                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        if (client != null && client.isOpen()) {
            client.send(message);
        }
    }

    public void close() {
        if (client != null) client.close();
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }
}
