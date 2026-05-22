package com.jtjmpm.desktop.controller;

import com.jtjmpm.ReadyMessage;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReadyController {
    @FXML private Label lobbyLabel;

    public void setLobbyName(String name) {
        lobbyLabel.setText("Lobby: " + name);
    }

    @FXML
    private void onReady() {
        ApiSocketClient.getInstance().send(new ReadyMessage(true));
        System.out.println("USER_READY sent");
    }
}
