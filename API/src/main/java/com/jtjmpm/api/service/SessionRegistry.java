package com.jtjmpm.api.service;

import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {
    private final ConcurrentHashMap<String, WebSocket> sessions = new ConcurrentHashMap<>();

    public void register(String sessionId, WebSocket conn) {
        sessions.put(sessionId, conn);
    }

    public void unregister(String sessionId) {
        sessions.remove(sessionId);
    }

    public WebSocket get(String sessionId) {
        return sessions.get(sessionId);
    }

    public void sendToSession(String sessionId, String json) {
        WebSocket ws = sessions.get(sessionId);
        if (ws != null && ws.isOpen()) {
            try {
                ws.send(json);
            } catch (Exception e) {
                sessions.remove(sessionId, ws);
            }
        }
    }

    public void broadcast(List<String> sessionIds, String json) {
        sessionIds.forEach(id -> sendToSession(id, json));
    }
}
