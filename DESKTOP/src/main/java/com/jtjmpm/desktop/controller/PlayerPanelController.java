package com.jtjmpm.desktop.controller;

import com.jtjmpm.messages.PlayerDTO;
import com.jtjmpm.PlayerMoveResult;
import com.jtjmpm.desktop.utils.ShapeDrawer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.util.List;

public class PlayerPanelController {

    @FXML private ProgressBar hpBar;
    @FXML private Label hpLabel;
    @FXML private ProgressBar manaBar;
    @FXML private Label manaLabel;
    @FXML private Canvas gestureCanvas;
    @FXML private Canvas patternCanvas;
    @FXML private Label patternNameLabel;
    @FXML private Label accuracyLabel;

    @FXML
    public void initialize() {
        ShapeDrawer.clearCanvas(gestureCanvas);
        ShapeDrawer.clearCanvas(patternCanvas);
    }

    public void moveUpdate(PlayerMoveResult moveResult, String accuracyRating) {
        ShapeDrawer.drawMove(gestureCanvas, moveResult.points, Color.AQUA);
        String rating = accuracyRating != null ? accuracyRating : moveResult.getAccuracyRating();
        accuracyLabel.setText(rating);
        accuracyLabel.getStyleClass().removeAll("rating-bad", "rating-decent", "rating-good", "rating-amazing");
        accuracyLabel.getStyleClass().add(switch (rating) {
            case "AMAZING" -> "rating-amazing";
            case "GOOD"    -> "rating-good";
            case "DECENT"  -> "rating-decent";
            default        -> "rating-bad";
        });
    }

    public void patternUpdate(List<Point2D.Double> points, String shapeName) {
        ShapeDrawer.drawMove(patternCanvas, points, Color.GOLD);
        patternNameLabel.setText(shapeName != null ? shapeName : "");
    }

    public void playerStateUpdate(PlayerDTO player) {
        updateHealthBar(player.hp(), player.maxHp());
        updateManaBar(player.mana(), player.maxMana());
    }

    private void updateHealthBar(double myHp, double maxHp) {
        hpBar.setProgress(myHp / maxHp);
        hpLabel.setText("HP: " + (int)myHp + "/" + (int)maxHp);
    }

    private void updateManaBar(double myMana, double maxMana) {
        manaBar.setProgress(myMana / maxMana);
        manaLabel.setText("Mana: " + (int)myMana + "/" + (int)maxMana);
    }
}
