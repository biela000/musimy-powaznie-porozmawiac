package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.messages.PlayerDTO;
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
import java.util.*;

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
    @FXML private Label startingSoonLabel;

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
        Collection<PlayerDTO> players = update.gameState.players().values();

        if (players.size() == 1) {
            ApiSocketClient.getInstance().setEnemyId(null);
        }

        Optional<PlayerDTO> enemy = players.stream()
                .filter(player -> !player.id().equals(ApiSocketClient.getInstance().getHostId()))
                .findFirst();

        enemy.ifPresent(player -> ApiSocketClient.getInstance().setEnemyId(player.id()));
    }

    private void updateReadyStatus(GameStateUpdateMessage update) {
        ApiSocketClient client = ApiSocketClient.getInstance();

        PlayerDTO host = update.gameState.players().get(client.getHostId());
        boolean isHostReady = host.ready();

        boolean isEnemyReady = false;

        if (client.getEnemyId() != null) {
            PlayerDTO enemy = update.gameState.players().get(client.getEnemyId());
            isEnemyReady = enemy.ready();
        }

        hostStatusLabel.setText("Host: " + playerStatusText.get(isHostReady));
        enemyStatusLabel.setText("Enemy: " + playerStatusText.get(isEnemyReady));

        if (isHostReady && isEnemyReady) {
            startingSoonLabel.setText("STARTING SOON...");
        } else {
            startingSoonLabel.setText("");
        }
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
