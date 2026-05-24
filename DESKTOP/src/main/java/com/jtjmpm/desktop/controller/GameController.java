package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class GameController {

    @FXML
    private Canvas gestureCanvas;
    @FXML
    public Label accuracyLabel;

    private GraphicsContext gc;

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        gc = gestureCanvas.getGraphicsContext2D();
        clearCanvas();

        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);
    }

    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case "MOVE_RESULT":
                Platform.runLater(() -> {
                    drawGesture(gson.fromJson(message, MoveResultMessage.class));
                });
                break;
            case "GAME_STATUS_UPDATE":
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }


    private void drawGesture(MoveResultMessage message) {
        if (message == null || message.points == null || message.points.isEmpty()) {
            return;
        }

        double percent = message.accuracy * 100.0;
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", percent));

        clearCanvas();

        List<Point2D> points = message.points;

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        double shapeWidth = maxX - minX;
        double shapeHeight = maxY - minY;

        if (shapeWidth == 0) shapeWidth = 1;
        if (shapeHeight == 0) shapeHeight = 1;

        double padding = 40.0;
        double canvasW = gestureCanvas.getWidth() - 2 * padding;
        double canvasH = gestureCanvas.getHeight() - 2 * padding;

        double scale = Math.min(canvasW / shapeWidth, canvasH / shapeHeight);

        double offsetX = padding + (canvasW - (shapeWidth * scale)) / 2.0;
        double offsetY = padding + (canvasH - (shapeHeight * scale)) / 2.0;

        gc.setStroke(Color.CHARTREUSE);
        gc.setLineWidth(6.0);
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

    private void clearCanvas() {
        gc.clearRect(0, 0, gestureCanvas.getWidth(), gestureCanvas.getHeight());
    }

    @FXML
    private void onBackToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/jtjmpm/desktop/lobby-view.fxml")
            );
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) gestureCanvas.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}