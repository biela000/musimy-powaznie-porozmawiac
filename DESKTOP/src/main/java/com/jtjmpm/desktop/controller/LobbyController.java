package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.LobbyMessage;
import com.jtjmpm.WsMessage;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;

public class LobbyController {
    @FXML private TextField lobbyNameInput;
    @FXML private Label statusLabel;

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);
    }

    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        if ("LOBBY_JOINED".equals(base.type)) {
            Platform.runLater(() -> {
                navigateToReady(lobbyNameInput.getText().trim());
            });
        }
    }

    @FXML
    private void onCreateLobby() {
        sendLobbyMessage("CREATE_LOBBY");
    }

    @FXML
    private void onJoinLobby() {
        sendLobbyMessage("JOIN_LOBBY");
    }

    @FXML
    private void onPlaySolo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jtjmpm/desktop/game-view.fxml")
            );

            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lobbyNameInput.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load Solo Mode screen");
        }
    }

    private void sendLobbyMessage(String type) {
        String name = lobbyNameInput.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Please enter a lobby name.");
            return;
        }

        ApiSocketClient.getInstance().send(
                new LobbyMessage(type, name)
        );
    }

    private void navigateToReady(String lobbyName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jtjmpm/desktop/ready-view.fxml")
            );

            Scene scene = new Scene(loader.load());

            ReadyController controller = loader.getController();
            controller.setLobbyName(lobbyName);

            Stage stage = (Stage) lobbyNameInput.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load next screen");
        }
    }
}
