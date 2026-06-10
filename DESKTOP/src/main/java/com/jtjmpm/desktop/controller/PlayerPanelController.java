package com.jtjmpm.desktop.controller;

import com.jtjmpm.messages.PlayerDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class PlayerPanelController {

    @FXML private ProgressBar hpBar;
    @FXML private Label hpLabel;
    @FXML private ProgressBar manaBar;
    @FXML private Label manaLabel;
    @FXML private Label winsLabel;

    @FXML private VBox activeEffectsContainer;

    public void playerStateUpdate(PlayerDTO player) {
        hpBar.setProgress(player.hp() / player.maxHp());
        hpLabel.setText("HP: " + (int) player.hp() + "/" + (int) player.maxHp());
        manaBar.setProgress(player.mana() / player.maxMana());
        manaLabel.setText("Mana: " + (int) player.mana() + "/" + (int) player.maxMana());
        winsLabel.setText(winsIndicator(player.wins()));
        
        javafx.application.Platform.runLater(() -> {
            activeEffectsContainer.getChildren().clear();
            for (com.jtjmpm.messages.StatusEffectDTO effect : player.activeEffects()) {
                String durationText = effect.durationTicks >= 0 ? " (" + effect.durationTicks + "s)" : "";
                String formattedName = effect.effectName.replace("_", " ");
                Label label = new Label(formattedName + durationText);
                String color = effect.isPositive ? "limegreen" : "red";
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + color + ";");
                activeEffectsContainer.getChildren().add(label);
            }
        });
    }

    private String winsIndicator(int wins) {
        return switch (wins) {
            case 1 -> "●  ○";
            case 2 -> "●  ●";
            default -> "○  ○";
        };
    }
}
