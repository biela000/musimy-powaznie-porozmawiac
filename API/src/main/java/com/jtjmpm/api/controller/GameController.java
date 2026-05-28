package com.jtjmpm.api.controller;

import com.google.gson.Gson;
import com.jtjmpm.GameState;
import com.jtjmpm.MessageType;
import com.jtjmpm.Player;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.api.model.GameStateStore;
import com.jtjmpm.api.model.PatternEngine.GestureToScore;
import com.jtjmpm.api.model.PatternEngine.PatternGenerator;
import com.jtjmpm.api.model.PatternEngine.RotationVectorParser;
import com.jtjmpm.api.model.PatternEngine.ShapeNormalizer;
import com.jtjmpm.api.model.SessionRegistry;
import com.jtjmpm.api.utils.SocketUtils;
import com.jtjmpm.messages.*;
import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.util.List;

@Component
public class GameController implements MessageController {
    private final static int NORMALIZED_SHAPE_POINT_COUNT = 64;
    private final static int NORMALIZED_SHAPE_TRIM_COUNT = 3;

    private final GameStateStore store;
    private final SessionRegistry registry;
    private final Gson gson;

    public GameController(GameStateStore store, SessionRegistry registry, Gson gson) {
        this.store = store;
        this.registry = registry;
        this.gson = gson;
    }

    @Override
    public List<MessageType> supportedTypes() {
        return List.of(
                MessageType.PLAYER_MOVE
        );
    }

    @Override
    public void handle(WebSocket conn, String rawJson) {
        WsMessage message = gson.fromJson(rawJson, WsMessage.class);

        switch (message.type) {
            case MessageType.PLAYER_MOVE -> handlePlayerMove(conn, rawJson);
        }
    }

    private void handlePlayerMove(WebSocket conn, String rawJson) {
        String sessionId = SocketUtils.getSessionId(conn);
        PlayerMoveMessage playerMoveMessage = gson.fromJson(rawJson, PlayerMoveMessage.class);
        System.out.println("Receiving a move from: " + sessionId + " (size: " + playerMoveMessage.move.size() + ")");

        try {
            RotationVectorParser parser = new RotationVectorParser();

            List<Point2D.Double> normalPoints = parser.processBatch(playerMoveMessage.move);
            List<Point2D.Double> normalizedPoints = ShapeNormalizer.preProcess(
                    normalPoints, NORMALIZED_SHAPE_POINT_COUNT, NORMALIZED_SHAPE_TRIM_COUNT
            );
            List<Point2D.Double> circlePattern = PatternGenerator.createCircle(64);

            double accuracyScore = GestureToScore.getScore(circlePattern, playerMoveMessage.move);
            System.out.println("Acurracy for session: " + sessionId + " equals: " + Math.round(accuracyScore * 100));

            MoveResultMessage resultMessage = new MoveResultMessage(
                    new PlayerMoveResult(normalizedPoints, accuracyScore), sessionId
            );

            List<String> playerIds = store.getPlayerIdsFromLobby(store.getLobbyIdForPlayer(sessionId));
            registry.broadcast(playerIds, gson.toJson(resultMessage));
        } catch (Exception e) {
            System.err.println("Error while calcultaing score from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
