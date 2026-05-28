package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.Player;
import com.jtjmpm.messages.GameStateUpdateMessage;
import com.jtjmpm.messages.ReadyMessage;
import com.jtjmpm.messages.WsMessage;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ReadyController {
    private final static String GAME_VIEW = "/com/jtjmpm/desktop/game-view.fxml";

    @FXML private Label lobbyLabel;
    @FXML private Label player1StatusLabel;
    @FXML private Label player2StatusLabel;

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);
    }

    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case MessageType.GAME_STATE_UPDATE:
                GameStateUpdateMessage update = gson.fromJson(message, GameStateUpdateMessage.class);
                Platform.runLater(() -> {
                    updateEnemyId(update);
                    updateReadyStatus(update);
                });
                break;
            case MessageType.GAME_START:
                Platform.runLater(this::navigateToGame);
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }

    private void updateEnemyId(GameStateUpdateMessage update) {
        List<Player> players = update.gameState.getPlayers();

        if (players.size() == 1) {
            ApiSocketClient.getInstance().setEnemyId(null);
        }

        String enemyId = players.stream()
                .filter(player -> !player.getId().equals(ApiSocketClient.getInstance().getHostId()))
                .findFirst()
                .toString();

        ApiSocketClient.getInstance().setEnemyId(enemyId);
    }

    private void updateReadyStatus(GameStateUpdateMessage update) {

    }

    public void setLobbyName(String name) {
        lobbyLabel.setText("Lobby: " + name);
    }

    @FXML
    private void onReady() {
        ApiSocketClient.getInstance().send(new ReadyMessage());
    }

    private void navigateToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(GAME_VIEW));

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lobbyLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
