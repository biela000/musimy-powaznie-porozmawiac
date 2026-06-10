package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.MessageType;
import com.jtjmpm.messages.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import com.jtjmpm.desktop.model.GameStateManager;
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
import com.jtjmpm.desktop.utils.ViewLoader;

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
            case MessageType.COUNTDOWN:
                com.jtjmpm.messages.CountdownMessage countdown = gson.fromJson(message, com.jtjmpm.messages.CountdownMessage.class);
                if (countdown.count > 0) {
                    Platform.runLater(() -> startingSoonLabel.setText(">> STARTING IN " + countdown.count + "..."));
                }
                break;
            case MessageType.GAME_START:
                StartGameMessage startMsg = gson.fromJson(message, StartGameMessage.class);
                if (startMsg.initialPattern != null) {
                    GameStateManager.getInstance().setPendingPattern(startMsg.initialPattern, startMsg.initialPatternName);
                }
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

        for (SpellDTO spell : spells) {
            VBox card = new VBox(5);
            card.setPrefWidth(148);
            card.setPrefHeight(100);
            card.getStyleClass().add("pixel-spell-card");

            Label nameLabel = new Label(spell.name());
            nameLabel.getStyleClass().add("pixel-card-title");

            String typeStr = spell.type() != null ? spell.type().toString() : "UNKNOWN";
            Label typeLabel = new Label(typeStr);
            typeLabel.getStyleClass().add("pixel-card-type");

            Label dmgLabel = new Label("DMG: " + spell.displayPower());
            dmgLabel.getStyleClass().add("pixel-card-dmg");

            card.getChildren().addAll(nameLabel, typeLabel, dmgLabel);

            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(spell.description());
            tooltip.setShowDelay(javafx.util.Duration.millis(200));
            tooltip.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-background-color: #2b2b45; -fx-text-fill: #f0e68c; -fx-border-color: #f0e68c; -fx-border-width: 1px;");
            javafx.scene.control.Tooltip.install(card, tooltip);

            card.setOnMouseClicked(event -> {
                String spellId = spell.name();

                if (myLoadout.contains(spellId)) {
                    myLoadout.remove(spellId);
                    card.getStyleClass().remove("pixel-spell-card-selected");
                    if (!card.getStyleClass().contains("pixel-spell-card")) {
                        card.getStyleClass().add("pixel-spell-card");
                    }
                } else if (myLoadout.size() < 4) {
                    myLoadout.add(spellId);
                    card.getStyleClass().remove("pixel-spell-card");
                    if (!card.getStyleClass().contains("pixel-spell-card-selected")) {
                        card.getStyleClass().add("pixel-spell-card-selected");
                    }
                }

                readyButton.setDisable(myLoadout.size() != 4);
            });

            spellsContainer.getChildren().add(card);
        }
    }

    private void updateEnemyId(GameStateUpdateMessage update) {
        Collection<PlayerDTO> players = update.gameState.players().values();

        if (players.size() == 1) {
            GameStateManager.getInstance().setEnemyId(null);
        }

        Optional<PlayerDTO> enemy = players.stream()
                .filter(player -> !player.id().equals(GameStateManager.getInstance().getHostId()))
                .findFirst();

        enemy.ifPresent(player -> GameStateManager.getInstance().setEnemyId(player.id()));
    }

    private void updateReadyStatus(GameStateUpdateMessage update) {
        GameStateManager stateManager = GameStateManager.getInstance();

        PlayerDTO host = update.gameState.players().get(stateManager.getHostId());
        boolean isHostReady = host.ready();

        boolean isEnemyReady = false;

        if (stateManager.getEnemyId() != null) {
            PlayerDTO enemy = update.gameState.players().get(stateManager.getEnemyId());
            isEnemyReady = enemy.ready();
        }

        hostStatusLabel.setText("HOST   :  " + (isHostReady ? "READY" : "NOT READY"));
        hostStatusLabel.getStyleClass().removeAll("pixel-status-ready", "pixel-status-not-ready");
        hostStatusLabel.getStyleClass().add(isHostReady ? "pixel-status-ready" : "pixel-status-not-ready");

        enemyStatusLabel.setText("ENEMY  :  " + (isEnemyReady ? "READY" : "NOT READY"));
        enemyStatusLabel.getStyleClass().removeAll("pixel-status-ready", "pixel-status-not-ready");
        enemyStatusLabel.getStyleClass().add(isEnemyReady ? "pixel-status-ready" : "pixel-status-not-ready");

        if (isHostReady && isEnemyReady) {
            startingSoonLabel.setText(">> STARTING SOON...");
        } else {
            startingSoonLabel.setText("");
        }
    }

    public void setLobbyName(String name) {
        lobbyLabel.setText("[ " + name.toUpperCase() + " ]");
    }

    @FXML
    private void onReady() {
        GameStateManager.getInstance().setCurrentLoadout(new ArrayList<>(myLoadout));
        ApiSocketClient.getInstance().send(new ReadyMessage(myLoadout));
    }

    private void navigateToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(GAME_VIEW));

            Scene scene = ViewLoader.loadScaledScene(loader);
            Stage stage = (Stage) lobbyLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
