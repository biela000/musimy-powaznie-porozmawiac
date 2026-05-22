package com.jtjmpm.api.game.game_logic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import static com.jtjmpm.api.game.game_logic.GestureEvaluator.evaluateScoreBiDirectional;

public class MouseSimulatorWithGuides extends JFrame {

    private List<Point2D> currentDrawing = new ArrayList<>();
    private List<Point2D> pattern;
    private String lastResult = "Wybierz kształt i narysuj go zaczynając od kropki!";

    private final int POINTS = 64;
    private final int TRIM = 3;
    private final double K = 3.5;
    private final double WINDOW = 0.15;

    private JPanel canvas;

    public MouseSimulatorWithGuides() {
        setTitle("Symulator Gestów (DTW Tester) z Przewodnikiem");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        String[] shapes = {"Okrąg", "Ósemka", "Trójkąt"};
        JComboBox<String> shapeSelector = new JComboBox<>(shapes);
        controlPanel.add(new JLabel("Wybierz kształt: "));
        controlPanel.add(shapeSelector);
        add(controlPanel, BorderLayout.NORTH);

        pattern = PatternGenerator.createCircle(POINTS);

        shapeSelector.addActionListener(e -> {
            String selected = (String) shapeSelector.getSelectedItem();
            if ("Okrąg".equals(selected)) {
                pattern = PatternGenerator.createCircle(POINTS);
            } else if ("Ósemka".equals(selected)) {
                pattern = PatternGenerator.createFigureEight(POINTS);
            } else if ("Trójkąt".equals(selected)) {
                pattern = PatternGenerator.createTriangle(POINTS);
            }
            lastResult = "Zmieniono na: " + selected + ". Spróbuj!";
            currentDrawing.clear();
            canvas.repaint();
        });

        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = Math.min(getWidth(), getHeight()) / 3;

                g2d.setColor(Color.LIGHT_GRAY);
                float[] dashPattern = { 5.0f, 5.0f };
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashPattern, 0.0f));

                if (pattern != null && pattern.size() > 1) {
                    for (int i = 1; i < pattern.size(); i++) {
                        Point2D p1 = pattern.get(i - 1);
                        Point2D p2 = pattern.get(i);
                        int x1 = centerX + (int)(p1.x * radius);
                        int y1 = centerY - (int)(p1.y * radius);
                        int x2 = centerX + (int)(p2.x * radius);
                        int y2 = centerY - (int)(p2.y * radius);
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                }

                g2d.setColor(Color.ORANGE);
                g2d.setStroke(new BasicStroke(2));
                if (pattern != null && pattern.size() > 3) {
                    Point2D p0 = pattern.get(0);
                    int sx = centerX + (int)(p0.x * radius);
                    int sy = centerY - (int)(p0.y * radius);

                    g2d.fillOval(sx - 8, sy - 8, 16, 16);
                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(sx - 8, sy - 8, 16, 16);

                    Point2D p3 = pattern.get(3);
                    int targetX = centerX + (int)(p3.x * radius);
                    int targetY = centerY - (int)(p3.y * radius);

                    double arrowAngleRad = Math.atan2(targetY - sy, targetX - sx);

                    g2d.setColor(Color.LIGHT_GRAY);
                    int arrowSize = 12;
                    Path2D arrowHead = new Path2D.Double();
                    arrowHead.moveTo(0, 0);
                    arrowHead.lineTo(-arrowSize, -arrowSize / 2.0);
                    arrowHead.lineTo(-arrowSize, arrowSize / 2.0);
                    arrowHead.closePath();

                    double offset = 15;
                    g2d.translate(sx + Math.cos(arrowAngleRad) * offset, sy + Math.sin(arrowAngleRad) * offset);
                    g2d.rotate(arrowAngleRad);
                    g2d.fill(arrowHead);
                    g2d.rotate(-arrowAngleRad);
                    g2d.translate(-(sx + Math.cos(arrowAngleRad) * offset), -(sy + Math.sin(arrowAngleRad) * offset));
                }

                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                g2d.drawString(lastResult, 20, 30);

                if (currentDrawing.size() > 1) {
                    g2d.setColor(Color.BLUE);
                    g2d.setStroke(new BasicStroke(3));
                    for (int i = 1; i < currentDrawing.size(); i++) {
                        Point2D p1 = currentDrawing.get(i - 1);
                        Point2D p2 = currentDrawing.get(i);
                        g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
                    }
                }
            }
        };

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentDrawing.add(new Point2D(e.getX(), e.getY()));
                canvas.repaint();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentDrawing.clear();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentDrawing.size() > 10) {
                    List<Point2D> mathReadyDrawing = new ArrayList<>();
                    for (Point2D p : currentDrawing) {
                        mathReadyDrawing.add(new Point2D(p.x, -p.y));
                    }

                    List<Point2D> normalized = ShapeNormalizer.preProcess(mathReadyDrawing, POINTS, TRIM);

                    double score = evaluateScoreBiDirectional(pattern, normalized, K, WINDOW);

                    int percent = (int) Math.round(score * 100);
                    lastResult = "Wynik: " + percent + "%";
                } else {
                    lastResult = "Za krótki ruch!";
                }
                canvas.repaint();
            }
        });

        add(canvas, BorderLayout.CENTER);
    }

//    public static double evaluateScoreBiDirectional(List<Point2D> pattern, List<Point2D> gesture, double k, double windowPercent) {
//        double scoreForward = GestureEvaluator.evaluateScore(pattern, gesture, k, windowPercent);
//
//        List<Point2D> reversedGesture = new ArrayList<>(gesture);
//        Collections.reverse(reversedGesture);
//        double scoreBackward = GestureEvaluator.evaluateScore(pattern, reversedGesture, k, windowPercent);
//
//        return Math.max(scoreForward, scoreBackward);
//    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MouseSimulatorWithGuides().setVisible(true);
        });
    }
}