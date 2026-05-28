package com.jtjmpm.api.controller;

import com.jtjmpm.MessageType;
import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageDispatcher {
    private final Map<MessageType, MessageController> routingTable = new HashMap<>();

    public MessageDispatcher(List<MessageController> controllers) {
        for (MessageController c : controllers) {
            for (MessageType type : c.supportedTypes()) {
                routingTable.put(type, c);
            }
        }
    }

    public void dispatch(WebSocket conn, String rawJson, MessageType messageType) {
        MessageController controller = routingTable.get(messageType);
        if (controller != null) {
            controller.handle(conn, rawJson);
        } else {
            System.out.println("Unknown message type: " + messageType);
        }
    }
}
