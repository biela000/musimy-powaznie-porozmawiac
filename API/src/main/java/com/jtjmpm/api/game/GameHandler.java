package com.jtjmpm.api.game;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>(); //maps session id to session object
    private final GameStateStore store;
    private final ObjectMapper objectMapper;

    @Autowired
    public GameHandler(GameStateStore store, ObjectMapper objectMapper) { //Constructor so its possible to create a new GameHandler for testing
        this.store=store;
        this.objectMapper=objectMapper;
    }

    //CONNECTIONS

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connected, session ID: " + session.getId());
        activeSessions.put(session.getId(), session);
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

    //HANDLING MESSAGES

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String rawJson = message.getPayload();

        try {
            //Parsing JSON file (might need to be changed)
            JsonNode rootNode = objectMapper.readTree(rawJson);

            String messageType = rootNode.get("type").asText();
            JsonNode payloadNode = rootNode.get("payload");

            switch (messageType) {
                case "CREATE_LOBBY":
                    handleCreateLobby(session);
                    break;
                case "JOIN_LOBBY":
                    handleJoinLobby(session, payloadNode);
                    break;
                case "SENSOR_DATA":
                    handleSensorData(session, payloadNode);
                    break;
                case "LEAVE_LOBBY":
                    handleLeaveLobby(session);
                    break;
                default:
                    System.out.println("Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            System.err.println("Parsing error, session ID: " + session.getId() + ": " + e.getMessage());
        }
    }

    //HELPER FUNCTIONS FOR HANDLING MESSAGES

    private void handleCreateLobby(WebSocketSession session){
        // TODO
        System.out.println("Player with session ID: " + session.getId() + "is creating a lobby");
    }

    private void handleJoinLobby(WebSocketSession session, JsonNode payloadNode){
        // TODO
        System.out.println("Player with session ID: " + session.getId() + "is joining a lobby");
    }

    private void handleSensorData(WebSocketSession session, JsonNode payloadNode) {
        // TODO
        System.out.println("Player with session ID: " + session.getId() + "is sending sensor data");
    }

    private void handleLeaveLobby(WebSocketSession session){
        // TODO
        System.out.println("Player with session ID: " + session.getId() + "is leaving his lobby");
    }
}
