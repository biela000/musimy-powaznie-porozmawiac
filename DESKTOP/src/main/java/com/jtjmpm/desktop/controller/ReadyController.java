package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.GameStateUpdateMessage;
import com.jtjmpm.ReadyMessage;
import com.jtjmpm.WsMessage;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReadyController {
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

        if ("GAME_STATE_UPDATE".equals(base.type)) {
            GameStateUpdateMessage update = gson.fromJson(message, GameStateUpdateMessage.class);
            Platform.runLater(() -> updateReadyStatus(update));
        }
    }

    private void updateReadyStatus(GameStateUpdateMessage update) {
        player1StatusLabel.setText("Player 1: " + (update.gameState.getPlayer1Ready() ? "✅ Ready" : "❌ Not ready"));
        player2StatusLabel.setText("Player 2: " + (update.gameState.getPlayer2Ready() ? "✅ Ready" : "❌ Not ready"));
    }

    public void setLobbyName(String name) {
        lobbyLabel.setText("Lobby: " + name);
    }

    @FXML
    private void onReady() {
        ApiSocketClient.getInstance().send(new ReadyMessage());
        System.out.println("USER_READY sent");
    }
}
