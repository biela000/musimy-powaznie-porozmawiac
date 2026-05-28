package com.jtjmpm.api.utils;

import org.java_websocket.WebSocket;

public class SocketUtils {
    public static String getSessionId(WebSocket conn) {
        return conn.getRemoteSocketAddress().toString();
    }
}
