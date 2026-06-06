package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.messages.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
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

    @FXML private TilePane spellsContainer;

    @FXML private Button readyButton;

    private final List<String> myLoadout = new ArrayList<>();

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);

        ApiSocketClient.getInstance().send(new GetSpellsListMessage());
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
            case MessageType.AVAILABLE_SPELLS:
                AvailableSpellsMessage spellsMsg = gson.fromJson(message, AvailableSpellsMessage.class);
                Platform.runLater(() -> {
                    renderSpells(spellsMsg);
                });
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }

    private void renderSpells(AvailableSpellsMessage spellsMsg) {
        List<SpellDTO> spells = spellsMsg.spells;
        spellsContainer.getChildren().clear();

        String defaultStyle = "-fx-background-color: #ffffff; " +
                "-fx-border-color: #bdc3c7; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 10px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";

        String selectedStyle = "-fx-background-color: #eafaf1; " +
                "-fx-border-color: #2ecc71; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 10px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(46,204,113,0.4), 8, 0, 0, 0);";


        for (SpellDTO spell : spells) {
            VBox card = new VBox(5);
            card.setPrefWidth(140);
            card.setPrefHeight(100);
            card.setStyle(defaultStyle);

            Label nameLabel = new Label(spell.name());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            String typeStr = spell.type() != null ? spell.type().toString() : "UNKNOWN";
            Label typeLabel = new Label(typeStr);
            typeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px;");

            Label dmgLabel = new Label("DMG: " + spell.displayPower());
            dmgLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            card.getChildren().addAll(nameLabel, typeLabel, dmgLabel);

            card.setOnMouseClicked(event -> {
                String spellId = spell.name();

                if (myLoadout.contains(spellId)) {
                    myLoadout.remove(spellId);
                    card.setStyle(defaultStyle);
                } else if (myLoadout.size() < 4) {
                    myLoadout.add(spellId);
                    card.setStyle(selectedStyle);
                }

                readyButton.setDisable(myLoadout.size() != 4);
            });

            spellsContainer.getChildren().add(card);
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
        ApiSocketClient.getInstance().setCurrentLoadout(new ArrayList<>(myLoadout));
        ApiSocketClient.getInstance().send(new ReadyMessage(myLoadout));
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
