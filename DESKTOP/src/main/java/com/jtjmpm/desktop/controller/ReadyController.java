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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReadyController {
    private final static String GAME_VIEW = "/com/jtjmpm/desktop/game-view.fxml";

    private final static Map<Boolean, String> playerStatusText = new HashMap<>();
    {
        playerStatusText.put(true, "READY");
        playerStatusText.put(false, "NOT READY");
    }

    @FXML private Label lobbyLabel;
    @FXML private Label hostStatusLabel;
    @FXML private Label enemyStatusLabel;

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

        Optional<Player> enemy = players.stream()
                .filter(player -> !player.getId().equals(ApiSocketClient.getInstance().getHostId()))
                .findFirst();

        enemy.ifPresent(player -> ApiSocketClient.getInstance().setEnemyId(player.getId()));
    }

    private void updateReadyStatus(GameStateUpdateMessage update) {
        ApiSocketClient client = ApiSocketClient.getInstance();
        System.out.println(client.getHostId());
        System.out.println(client.getEnemyId());

        boolean isHostReady = update.gameState.getPlayer(client.getHostId()).isReady();
        boolean isEnemyReady = false;

        if (client.getEnemyId() != null) {
            isEnemyReady = update.gameState.getPlayer(client.getEnemyId()).isReady();
        }

        hostStatusLabel.setText(playerStatusText.get(isHostReady));
        enemyStatusLabel.setText(playerStatusText.get(isEnemyReady));
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
