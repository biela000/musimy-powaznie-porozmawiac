package com.jtjmpm.api.controller;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.api.game.MatchSupervisor;
import com.jtjmpm.api.model.*;
import com.jtjmpm.api.model.PatternEngine.GestureToScore;
import com.jtjmpm.api.model.PatternEngine.PatternGenerator;
import com.jtjmpm.api.model.PatternEngine.RotationVectorParser;
import com.jtjmpm.api.model.PatternEngine.ShapeNormalizer;
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
    private final SpellRegistry spellRegistry;
    private final MatchSupervisor matchSupervisor;
    private final CombatEngine combatEngine;

    public GameController(GameStateStore store, SessionRegistry registry, Gson gson, SpellRegistry spellRegistry, MatchSupervisor matchSupervisor, CombatEngine combatEngine) {
        this.store = store;
        this.registry = registry;
        this.gson = gson;
        this.spellRegistry = spellRegistry;
        this.matchSupervisor = matchSupervisor;
        this.combatEngine = combatEngine;
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
            GameState gameState = store.getPlayersLobby(sessionId);
            if (gameState == null) {
                System.err.println("Game not found for player: " + sessionId);
                return;
            }
            Player player = gameState.getPlayer(sessionId);
            if (player == null) {
                System.err.println("Player not found in game state, playerId: " + sessionId);
                return;
            }

            List<String> loadout = player.getSpellLoadout();
            int spellIndex = playerMoveMessage.spellIndex;
            if (spellIndex < 0 || spellIndex >= loadout.size()) {
                System.err.println("SECURITY ALERT: Player " + sessionId + " sent invalid spell index: " + spellIndex);
                return;
            }
            String requestedSpellId = loadout.get(spellIndex);

            Spell spell = spellRegistry.getSpell(requestedSpellId);

            System.out.println("Validation passed. Player " + sessionId + " is casting: " + requestedSpellId);

            RotationVectorParser parser = new RotationVectorParser();

            List<Point2D.Double> normalPoints = parser.processBatch(playerMoveMessage.move);
            List<Point2D.Double> normalizedPoints = ShapeNormalizer.preProcess(
                    normalPoints, NORMALIZED_SHAPE_POINT_COUNT, NORMALIZED_SHAPE_TRIM_COUNT
            );
            List<Point2D.Double> circlePattern = PatternGenerator.createCircle(64);

            double accuracyScore = GestureToScore.getScore(circlePattern, playerMoveMessage.move);
            System.out.println("Acurracy for session: " + sessionId + " equals: " + Math.round(accuracyScore * 100));

            List<String> playerIds = store.getPlayerIdsFromLobby(store.getLobbyIdForPlayer(sessionId));

            matchSupervisor.handlePlayerSpellCast(gameState, sessionId, gameState.getEnemy(sessionId).getId(),
                    spell, accuracyScore, new PlayerMoveResult(normalizedPoints, accuracyScore), playerIds);

        } catch (Exception e) {
            System.err.println("Error while calcultaing score from session: " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
