package com.jtjmpm.mobile;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class GameHandler {
    private static volatile GameHandler instance;
    private WebSocketClient client;

    private GameHandler() {}

    public static GameHandler getInstance() {
        if (instance == null) {
            synchronized (GameHandler.class) {
                if (instance == null) {
                    instance = new GameHandler();
                }
            }
        }
        return instance;
    }

    public void connect(String url, Runnable onConnected, Runnable onError) {
        if (client != null && client.isOpen()) return;
        Log.d("DEV", "ws://" + url);

        try {
            client = new WebSocketClient(new URI("ws://" + url)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    if (onConnected != null) onConnected.run();
                }

                @Override
                public void onMessage(String message) {
                    // The mobile session is not associated with any lobby on the server,
                    // so incoming messages (e.g. WELCOME) require no action here.
                    Log.d("GameHandler", "Message: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                }

                @Override
                public void onError(Exception ex) {
                    if (onError != null) onError.run();
                }
            };

            client.connect();
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
