package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
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

        switch (base.type) {
            case "GAME_STATE_UPDATE":
                GameStateUpdateMessage update = gson.fromJson(message, GameStateUpdateMessage.class);
                Platform.runLater(() -> updateReadyStatus(update));
                break;
            case "GAME_START":
                Platform.runLater(this::navigateToGame);
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
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

    private void navigateToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jtjmpm/desktop/game-view.fxml")
            );

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lobbyLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
