package com.jtjmpm.api.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, WebSocketSession> codeToWSS = new ConcurrentHashMap<>();
    private final GameStateStore store;

    @Autowired
    public GameHandler(GameStateStore store) { //Constructor so its possible to create a new GameHandler for testing
        this.store=store;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connected, session ID: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed, session ID: " + session.getId());
        store.removeSession(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Connection failed, session ID: " + session.getId() + exception.getMessage());
        store.removeSession(session.getId());
    }
}
