package com.jtjmpm.desktop.controller;

import com.jtjmpm.messages.PlayerDTO;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.desktop.utils.ShapeDrawer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

public class PlayerPanelController {

    private static final double MAX_HP = 100.0;
    private static final double MAX_MANA = 100.0;

    @FXML private ProgressBar hpBar;
    @FXML private Label hpLabel;
    @FXML private ProgressBar manaBar;
    @FXML private Label manaLabel;
    @FXML private Canvas canvas;
    @FXML private Label accuracyLabel;

    @FXML
    public void initialize() {
        ShapeDrawer.clearCanvas(canvas);
    }

    public void moveUpdate(PlayerMoveResult moveResult) {
        ShapeDrawer.drawMove(canvas, moveResult.points, Color.AQUA);
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", moveResult.accuracy * 100.0));
    }

    public void playerStateUpdate(PlayerDTO player) {
        updateHealthBar(player.hp());
        updateManaBar(player.mana());
    }

    private void updateHealthBar(double myHp) {
        hpBar.setProgress(myHp / MAX_HP);
        hpLabel.setText("HP: " + (int)myHp + "/" + (int)MAX_HP);
    }

    private void updateManaBar(double myMana) {
        hpBar.setProgress(myMana / MAX_MANA);
        hpLabel.setText("HP: " + (int)myMana + "/" + (int)MAX_MANA);
    }
}
