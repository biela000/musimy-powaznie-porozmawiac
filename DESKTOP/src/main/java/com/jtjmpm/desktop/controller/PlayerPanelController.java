package com.jtjmpm.desktop.controller;

import com.jtjmpm.Player;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.desktop.utils.ShapeDrawer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

public class PlayerPanelController {
    @FXML private ProgressBar hpBar;
    @FXML private Label hpLabel;
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

    public void playerStateUpdate(Player player) {
        updateHealthBar(player.getHp());
    }

    private void updateHealthBar(double myHp) {
        hpBar.setProgress(myHp / Player.MAX_HP);
        hpLabel.setText("HP: " + (int)myHp + "/" + (int)Player.MAX_HP);
    }
}
