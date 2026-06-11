package com.jtjmpm.desktop.service;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.messages.WelcomeMessage;
import com.jtjmpm.messages.WsMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.jtjmpm.desktop.model.GameStateManager;
import com.jtjmpm.desktop.utils.DesktopConstants;

public class ApiSocketClient {
    private static final int RECONNECT_DELAY_SECONDS = DesktopConstants.RECONNECT_DELAY_SECONDS;

    private static volatile ApiSocketClient instance;

    private final Gson gson = new Gson();
    private WebSocketClient client;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> reconnectTask;
    private Consumer<String> onMessageCallback;
    private Runnable onConnected;
    private String serverUrl;

    public void setServerUrl(String s) {
        serverUrl = s;
    }

    public void setOnConnected(Runnable onConnected) {
        this.onConnected = onConnected;
    }

    public static ApiSocketClient getInstance() {
        if (instance == null) {
            synchronized (ApiSocketClient.class) {
                if (instance == null) {
                    instance = new ApiSocketClient();
                }
            }
        }
        return instance;
    }

    public static void handleUnknownMessage(WsMessage message) {
        System.out.println("Unknown message type: " + message.type);
    }

    public void setOnMessageCallback(Consumer<String> callback) {
        this.onMessageCallback = callback;
    }

    public void connect() {
        if (serverUrl == null) return;
        try {
            client = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("Connected to API");
                    cancelReconnect();
                    if (onConnected != null) onConnected.run();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("API message: " + message);

                    if (onMessageCallback != null) {
                        onMessageCallback.accept(message);
                    } else {
                        WelcomeMessage welcomeMessage = gson.fromJson(message, WelcomeMessage.class);
                        if (welcomeMessage.type.equals(MessageType.WELCOME)) {
                            GameStateManager.getInstance().setHostId(welcomeMessage.myPlayerId);
                        }
                    }

                    System.out.println("API message received");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from API: " + reason);
                    scheduleReconnect();
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

    private void scheduleReconnect() {
        if (reconnectTask == null || reconnectTask.isDone()) {
            reconnectTask = scheduler.scheduleWithFixedDelay(
                    this::connect,
                    RECONNECT_DELAY_SECONDS, RECONNECT_DELAY_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }

    private void cancelReconnect() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
    }

    public void send(Object message) {
        if (client != null && client.isOpen()) {
            client.send(gson.toJson(message));
        }
    }

    public void close() {
        cancelReconnect();
        scheduler.shutdown();
        if (client != null) {
            client.close();
        }
    }
}
