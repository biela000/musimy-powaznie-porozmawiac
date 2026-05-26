package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.messages.*;
import com.jtjmpm.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class GameController {

    // PLAYER ELEMENTS
    @FXML private ProgressBar myHpBar;
    @FXML private Label myHpLabel;
    @FXML private Canvas myCanvas;
    @FXML private Label myAccuracyLabel;

    // ENEMY ELEMENTS
    @FXML private ProgressBar enemyHpBar;
    @FXML private Label enemyHpLabel;
    @FXML private Canvas enemyCanvas;
    @FXML private Label enemyAccuracyLabel;

    //Might have to make new classes for these panels but idk

    @FXML private Label lobbyInfoLabel;

    private final Gson gson = new Gson();

    //local game state
    private GameState gameState = new GameState();

    @FXML
    public void initialize() {
        clearCanvas(myCanvas.getGraphicsContext2D(), myCanvas);
        clearCanvas(enemyCanvas.getGraphicsContext2D(), enemyCanvas);

        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);
    }

    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case "MOVE_RESULT":
                Platform.runLater(() -> {
                    handleMoveResult(gson.fromJson(message, MoveResultMessage.class));
                });
                break;
            case "GAME_STATE_UPDATE":
                Platform.runLater(() -> {
                    handleGameStateUpdate(gson.fromJson(message, GameStateUpdateMessage.class));
                });
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }

    private void handleMoveResult(MoveResultMessage msg){
        String myPlayerId = ApiSocketClient.getInstance().getMyPlayerId();
        if (myPlayerId == null) {
            System.err.println("Local player ID is not set");
            return;
        }

        if(msg.playerID.equals(myPlayerId)){
            drawGesture(msg, myCanvas, myAccuracyLabel, Color.CHARTREUSE);
        }
        else{
            drawGesture(msg, enemyCanvas, enemyAccuracyLabel, Color.RED);
        }
    }

    private void handleGameStateUpdate(GameStateUpdateMessage msg){
        //might have to do this smarter later
        gameState = msg.gameState;

        double myHp;
        double enemyHp;

        if (ApiSocketClient.getInstance().getMyPlayerId().equals(gameState.getPlayer1Id())) {
            myHp = gameState.getPlayer1Hp();
            enemyHp = gameState.getPlayer2Hp();
        } else {
            myHp = gameState.getPlayer2Hp();
            enemyHp = gameState.getPlayer1Hp();
        }

        updateHealthBars(myHp, 100, enemyHp);
    }

    private void drawGesture(MoveResultMessage message, Canvas canvas, Label accLbl, Color paintColor) {
        if (message == null || message.points == null || message.points.isEmpty()) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc, canvas);

        accLbl.setText(String.format("Accuracy: %.1f%%", message.accuracy * 100.0));

        List<Point2D> points = message.points;
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double shapeWidth = Math.max(maxX - minX, 1);
        double shapeHeight = Math.max(maxY - minY, 1);

        double padding = 30.0;
        double canvasW = canvas.getWidth() - 2 * padding;
        double canvasH = canvas.getHeight() - 2 * padding;

        double scale = Math.min(canvasW / shapeWidth, canvasH / shapeHeight);
        double offsetX = padding + (canvasW - (shapeWidth * scale)) / 2.0;
        double offsetY = padding + (canvasH - (shapeHeight * scale)) / 2.0;

        gc.setStroke(paintColor);
        gc.setLineWidth(5.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        gc.beginPath();
        double firstX = (points.get(0).getX() - minX) * scale + offsetX;
        double firstY = (points.get(0).getY() - minY) * scale + offsetY;
        gc.moveTo(firstX, firstY);

        for (int i = 1; i < points.size(); i++) {
            double x = (points.get(i).getX() - minX) * scale + offsetX;
            double y = (points.get(i).getY() - minY) * scale + offsetY;
            gc.lineTo(x, y);
        }

        gc.stroke();
    }

    private void clearCanvas(GraphicsContext gc, Canvas canvas) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updateHealthBars(double myHp, double maxHp, double enemyHp) {
        myHpBar.setProgress(myHp / maxHp);
        myHpLabel.setText("HP: " + (int)myHp + "/" + (int)maxHp);

        enemyHpBar.setProgress(enemyHp / maxHp);
        enemyHpLabel.setText("HP: " + (int)enemyHp + "/" + (int)maxHp);
    }

    @FXML
    private void onBackToMenu() {
        try {

            ApiSocketClient.getInstance().setOnMessageCallback(null);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jtjmpm/desktop/lobby-view.fxml")
            );
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) myCanvas.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}