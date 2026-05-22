package com.jtjmpm.desktop.controller;

import com.jtjmpm.LobbyMessage;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.awt.*;

public class LobbyController {
    @FXML private TextField lobbyNameInput;
    @FXML private Label statusLabel;

    @FXML
    private void onCreateLobby() {
        sendLobbyMessage("CREATE_LOBBY");
    }

    @FXML
    private void onJoinLobby() {
        sendLobbyMessage("JOIN_LOBBY");
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
        navigateToReady(name);
    }

    private void navigateToReady(String lobbyName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jtjmpm/desktop/ready-view.fxml")
            );

            Scene scene = new Scene(loader.load());
        }
    }
}
