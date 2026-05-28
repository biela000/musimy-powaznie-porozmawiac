package com.jtjmpm.api.game;

import com.google.gson.Gson;
import com.jtjmpm.*;
import com.jtjmpm.api.controller.MessageDispatcher;
import com.jtjmpm.api.model.GameStateStore;
import com.jtjmpm.api.model.SessionRegistry;
import com.jtjmpm.api.utils.SocketUtils;
import com.jtjmpm.messages.WelcomeMessage;
import com.jtjmpm.messages.WsMessage;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class GameHandler extends WebSocketServer {
    private final SessionRegistry registry;
    private final MessageDispatcher dispatcher;
    private final GameStateStore store;
    private final Gson gson;

    public GameHandler(
            InetSocketAddress address,
            SessionRegistry registry,
            GameStateStore store,
            MessageDispatcher dispatcher
    ) {
        super(address);
        this.registry = registry;
        this.store = store;
        this.dispatcher = dispatcher;
        this.gson = new Gson();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String sessionId = SocketUtils.getSessionId(conn);
        registry.register(sessionId, conn);
        conn.send(gson.toJson(new WelcomeMessage(sessionId)));

        System.out.println("Connected, session ID: " + sessionId);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String sessionId = SocketUtils.getSessionId(conn);
        registry.unregister(sessionId);
        store.removeSession(sessionId);

        System.out.println("Connection closed, session ID: " + sessionId);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            String sessionId = SocketUtils.getSessionId(conn);
            registry.unregister(sessionId);
            store.removeSession(sessionId);
            System.err.println("Connection failed, session ID: " + sessionId + " " + ex.getMessage());
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
            dispatcher.dispatch(conn, rawJson, message.type);
        } catch (Exception e) {
            System.err.println("Parse error from " + SocketUtils.getSessionId(conn));
        }
    }
}