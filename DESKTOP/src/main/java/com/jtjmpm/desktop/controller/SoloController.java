package com.jtjmpm.desktop.controller;

import com.jtjmpm.Point2D;
import com.jtjmpm.ShapeMessage;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SoloController {

    @FXML
    private Canvas gestureCanvas;

    private GraphicsContext gc;

    @FXML
    public void initialize() {
        gc = gestureCanvas.getGraphicsContext2D();
        clearCanvas();

        ApiSocketClient.getInstance().setOnShapeReceived(this::drawGesture);
    }


    private void drawGesture(ShapeMessage message) {
        if (message == null || message.points == null || message.points.isEmpty()) {
            return;
        }

        clearCanvas();

        gc.setStroke(Color.CHARTREUSE);
        gc.setLineWidth(4.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        double canvasWidth = gestureCanvas.getWidth();
        double canvasHeight = gestureCanvas.getHeight();

        List<Point2D> points = message.points;

        gc.beginPath();

        double firstX = points.getFirst().getX() * canvasWidth;
        double firstY = points.getFirst().getY() * canvasHeight;
        gc.moveTo(firstX, firstY);

        for (int i = 1; i < points.size(); i++) {
            double x = points.get(i).getX() * canvasWidth;
            double y = points.get(i).getY() * canvasHeight;
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
            ApiSocketClient.getInstance().setOnShapeReceived(null);

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