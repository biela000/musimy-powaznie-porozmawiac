package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.desktop.utils.GameStateUtils;
import com.jtjmpm.messages.*;
import com.jtjmpm.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GameController {
    public static final String LOBBY_VIEW = "/com/jtjmpm/desktop/lobby-view.fxml";

    @FXML private PlayerPanelController hostPanelController;
    @FXML private PlayerPanelController enemyPanelController;

    @FXML private Label lobbyInfoLabel;

    private final Gson gson = new Gson();

    private GameState gameState = new GameState();

    @FXML
    public void initialize() {
        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);
    }

    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case MessageType.MOVE_RESULT:
                Platform.runLater(() -> {
                    handleMoveResult(gson.fromJson(message, MoveResultMessage.class));
                });
                break;
            case MessageType.GAME_STATE_UPDATE:
                Platform.runLater(() -> {
                    handleGameStateUpdate(gson.fromJson(message, GameStateUpdateMessage.class));
                });
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }

    private void handleMoveResult(MoveResultMessage message){
        String hostId = ApiSocketClient.getInstance().getPlayerId();
        if (hostId == null) {
            System.err.println("Local player id is not set");
            return;
        }

        if (message.playerId.equals(hostId)) {
            hostPanelController.moveUpdate(message.result);
        } else {
            enemyPanelController.moveUpdate(message.result);
        }
    }

    private void handleGameStateUpdate(GameStateUpdateMessage message) {
        gameState = message.gameState;

        String hostId = ApiSocketClient.getInstance().getPlayerId();

        hostPanelController.playerStateUpdate(gameState.getPlayer(hostId));
        enemyPanelController.playerStateUpdate(
                Objects.requireNonNull(GameStateUtils.getEnemy(gameState.getPlayers(), hostId))
        );
    }

    @FXML
    private void onBackToMenu() {
        try {
            ApiSocketClient.getInstance().setOnMessageCallback(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOBBY_VIEW));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lobbyInfoLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}