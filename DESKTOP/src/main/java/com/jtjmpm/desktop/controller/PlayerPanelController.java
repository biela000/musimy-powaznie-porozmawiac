package com.jtjmpm.desktop.controller;

import com.jtjmpm.messages.PlayerDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class PlayerPanelController {

    @FXML private ProgressBar hpBar;
    @FXML private Label hpLabel;
    @FXML private ProgressBar manaBar;
    @FXML private Label manaLabel;

    public void playerStateUpdate(PlayerDTO player) {
        hpBar.setProgress(player.hp() / player.maxHp());
        hpLabel.setText("HP: " + (int) player.hp() + "/" + (int) player.maxHp());
        manaBar.setProgress(player.mana() / player.maxMana());
        manaLabel.setText("Mana: " + (int) player.mana() + "/" + (int) player.maxMana());
    }
}
