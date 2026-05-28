package com.jtjmpm.api.controller;

import java.util.List;
import com.jtjmpm.MessageType;
import org.java_websocket.WebSocket;

public interface MessageController {
    List<MessageType> supportedTypes();
    void handle(WebSocket conn, String rawJson);
}
