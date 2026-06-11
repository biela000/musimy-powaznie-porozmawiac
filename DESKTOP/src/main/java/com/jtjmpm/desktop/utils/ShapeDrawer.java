package com.jtjmpm.desktop.utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.util.List;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class ShapeDrawer {
    public static final int PADDING = 30;

    public static void clearCanvas(Canvas canvas) {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void drawMove(Canvas canvas, List<Point2D.Double> points, Color paintColor) {
        if (points.isEmpty()) {
            return;
        }

        clearCanvas(canvas);

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

        double canvasW = canvas.getWidth() - 2 * PADDING;
        double canvasH = canvas.getHeight() - 2 * PADDING;

        double scale = Math.min(canvasW / shapeWidth, canvasH / shapeHeight);
        double offsetX = PADDING + (canvasW - (shapeWidth * scale)) / 2.0;
        double offsetY = PADDING + (canvasH - (shapeHeight * scale)) / 2.0;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(paintColor);
        gc.setLineWidth(5.0);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        gc.beginPath();
        double firstX = (points.getFirst().getX() - minX) * scale + offsetX;
        double firstY = (maxY - points.getFirst().getY()) * scale + offsetY;
        gc.moveTo(firstX, firstY);

        for (int i = 1; i < points.size(); i++) {
            double x = (points.get(i).getX() - minX) * scale + offsetX;
            double y = (maxY - points.get(i).getY()) * scale + offsetY;
            gc.lineTo(x, y);
        }
        gc.stroke();

        final double DOT_RADIUS = 7.0;
        gc.setFill(Color.LIMEGREEN);
        gc.fillOval(firstX - DOT_RADIUS, firstY - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(firstX - DOT_RADIUS, firstY - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
    }
}
