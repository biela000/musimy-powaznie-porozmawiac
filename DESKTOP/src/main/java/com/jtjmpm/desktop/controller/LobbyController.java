package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.messages.LobbyMessage;
import com.jtjmpm.messages.WelcomeMessage;
import com.jtjmpm.messages.WsMessage;
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
    private final static String READY_VIEW = "/com/jtjmpm/desktop/ready-view.fxml";

    @FXML private TextField lobbyNameInput;
    @FXML private Label statusLabel;

    private final ApiSocketClient client = ApiSocketClient.getInstance();

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        client.setOnMessageCallback(this::handleApiMessage);
    }

    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);
        System.out.println("HANDLING API MESSAGE");

        switch (base.type) {
            case MessageType.LOBBY_JOINED:
                handleLobbyJoined();
                break;
            default:
                ApiSocketClient.handleUnknownMessage(base);
        }
    }

    private void handleLobbyJoined() {
        Stage stage = (Stage) lobbyNameInput.getScene().getWindow(); // Needs to be here because in Platform.runLater might be too late
        Platform.runLater(() -> {
            navigateToReady(lobbyNameInput.getText().trim(), stage);
        });
    }

    @FXML
    private void onCreateLobby() {
        sendLobbyMessage(MessageType.CREATE_LOBBY);
    }

    @FXML
    private void onJoinLobby() {
        sendLobbyMessage(MessageType.JOIN_LOBBY);
    }

    private void sendLobbyMessage(MessageType type) {
        String name = lobbyNameInput.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Please enter a lobby name.");
            return;
        }

        client.send(new LobbyMessage(type, name));
    }

    private void navigateToReady(String lobbyName, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(READY_VIEW));

            Scene scene = com.jtjmpm.desktop.utils.ViewLoader.loadScaledScene(loader);

            ReadyController controller = loader.getController();
            controller.setLobbyName(lobbyName);

            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load next screen");
        }
    }
}
