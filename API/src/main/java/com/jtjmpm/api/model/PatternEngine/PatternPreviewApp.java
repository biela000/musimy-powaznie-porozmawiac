package com.jtjmpm.api.model.PatternEngine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jtjmpm.api.model.core.GameConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class PatternPreviewApp extends JFrame {

    private static final JsonObject CONFIG = GameConstants.PATTERN_PREVIEW;

    private static final int POINTS      = CONFIG.get("points").getAsInt();
    private static final int CANVAS_SIZE = CONFIG.get("canvasSize").getAsInt();
    private static final int PADDING     = CONFIG.get("padding").getAsInt();

    private static final Color EASY_COLOR   = colorFromJson(CONFIG.getAsJsonArray("easyColor"));
    private static final Color MEDIUM_COLOR = colorFromJson(CONFIG.getAsJsonArray("mediumColor"));
    private static final Color HARD_COLOR   = colorFromJson(CONFIG.getAsJsonArray("hardColor"));

    private static Color colorFromJson(JsonArray rgb) {
        return new Color(rgb.get(0).getAsInt(), rgb.get(1).getAsInt(), rgb.get(2).getAsInt());
    }

    private PatternGenerator.Difficulty currentDifficulty = PatternGenerator.Difficulty.EASY;
    private List<PatternGenerator.NamedShape> pool        = List.of();
    private int poolIndex = 0;

    private List<Point2D.Double> currentPoints = List.of();
    private String currentName = "";

    private final ShapeCanvas canvas    = new ShapeCanvas();
    private final JLabel nameLabel      = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel diffLabel      = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel progressLabel  = new JLabel(" ", SwingConstants.CENTER);

    public PatternPreviewApp() {
        super("Pattern Preview");
        buildUI();
        resetPool();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel diffPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        diffPanel.add(new JLabel("Poziom:"));
        for (PatternGenerator.Difficulty d : PatternGenerator.Difficulty.values()) {
            JButton btn = new JButton(labelFor(d)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color base = colorFor(d);
                    Color fill = getModel().isPressed()  ? base.darker()
                               : getModel().isRollover() ? base.brighter()
                               : base;
                    g2.setColor(fill);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(base.darker());
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setForeground(Color.WHITE);
            btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
            btn.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> { currentDifficulty = d; resetPool(); });
            diffPanel.add(btn);
        }

        canvas.setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        canvas.setBackground(Color.WHITE);
        canvas.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 15f));
        diffLabel.setFont(diffLabel.getFont().deriveFont(Font.PLAIN, 11f));
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.PLAIN, 11f));
        progressLabel.setForeground(Color.GRAY);

        Color nextColor = new Color(70, 110, 200);
        JButton nextBtn = new JButton("Następny  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed()  ? nextColor.darker()
                           : getModel().isRollover() ? nextColor.brighter()
                           : nextColor;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(nextColor.darker());
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        nextBtn.setContentAreaFilled(false);
        nextBtn.setBorderPainted(false);
        nextBtn.setFocusPainted(false);
        nextBtn.setOpaque(false);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFont(nextBtn.getFont().deriveFont(Font.BOLD, 13f));
        nextBtn.setBorder(BorderFactory.createEmptyBorder(7, 22, 7, 22));
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.addActionListener(e -> advance());

        JLabel hint = new JLabel("● start   gradient niebieski→czerwony = kierunek rysowania", SwingConstants.CENTER);
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 10f));
        hint.setForeground(Color.GRAY);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{ nameLabel, diffLabel, progressLabel }) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
            bottomPanel.add(c);
        }
        bottomPanel.add(Box.createVerticalStrut(6));
        nextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(nextBtn);
        bottomPanel.add(Box.createVerticalStrut(6));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(hint);
        bottomPanel.add(Box.createVerticalStrut(6));

        add(diffPanel,   BorderLayout.NORTH);
        add(canvas,      BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void resetPool() {
        pool      = PatternGenerator.shuffledPool(currentDifficulty, POINTS);
        poolIndex = 0;
        showCurrent();
    }

    private void advance() {
        poolIndex++;
        if (poolIndex >= pool.size()) {
            pool      = PatternGenerator.shuffledPool(currentDifficulty, POINTS);
            poolIndex = 0;
        }
        showCurrent();
    }

    private void showCurrent() {
        PatternGenerator.NamedShape shape = pool.get(poolIndex);
        currentPoints = shape.points();
        currentName   = shape.name();

        nameLabel.setText(currentName);
        diffLabel.setText(labelFor(currentDifficulty));
        diffLabel.setForeground(colorFor(currentDifficulty));
        progressLabel.setText((poolIndex + 1) + " / " + pool.size());
        canvas.repaint();
    }

    private class ShapeCanvas extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            List<Point2D.Double> pts = currentPoints;
            if (pts == null || pts.size() < 2) return;

            int w = getWidth(), h = getHeight();
            int drawSize = Math.min(w, h) - 2 * PADDING;

            double minX = pts.stream().mapToDouble(p -> p.x).min().orElse(-1);
            double maxX = pts.stream().mapToDouble(p -> p.x).max().orElse(1);
            double minY = pts.stream().mapToDouble(p -> p.y).min().orElse(-1);
            double maxY = pts.stream().mapToDouble(p -> p.y).max().orElse(1);

            double range = Math.max(maxX - minX, maxY - minY);
            if (range == 0) range = 1;

            double midX  = (minX + maxX) / 2.0;
            double midY  = (minY + maxY) / 2.0;
            double scale = drawSize / range;
            double cx    = w / 2.0;
            double cy    = h / 2.0;

            int n = pts.size();
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                float t = (float) i / n;
                g2.setColor(new Color(t, 0.2f, 1f - t));

                Point2D.Double p0 = pts.get(Math.max(i - 1, 0));
                Point2D.Double p1 = pts.get(i);
                Point2D.Double p2 = pts.get(i + 1);
                Point2D.Double p3 = pts.get(Math.min(i + 2, n - 1));

                double cp1x = p1.x + (p2.x - p0.x) / 6.0;
                double cp1y = p1.y + (p2.y - p0.y) / 6.0;
                double cp2x = p2.x - (p3.x - p1.x) / 6.0;
                double cp2y = p2.y - (p3.y - p1.y) / 6.0;

                java.awt.geom.Path2D.Double seg = new java.awt.geom.Path2D.Double();
                seg.moveTo(sx(p1.x, midX, scale, cx), sy(p1.y, midY, scale, cy));
                seg.curveTo(
                    sx(cp1x, midX, scale, cx), sy(cp1y, midY, scale, cy),
                    sx(cp2x, midX, scale, cx), sy(cp2y, midY, scale, cy),
                    sx(p2.x,  midX, scale, cx), sy(p2.y,  midY, scale, cy)
                );
                g2.draw(seg);
            }

            int startX = sx(pts.get(0).x, midX, scale, cx);
            int startY = sy(pts.get(0).y, midY, scale, cy);
            g2.setColor(Color.ORANGE);
            g2.fillOval(startX - 7, startY - 7, 14, 14);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(startX - 7, startY - 7, 14, 14);

            if (n > 4) {
                int ax = sx(pts.get(4).x, midX, scale, cx);
                int ay = sy(pts.get(4).y, midY, scale, cy);
                double dx = ax - startX, dy = ay - startY;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len > 0) {
                    dx /= len; dy /= len;
                    g2.setColor(Color.ORANGE);
                    g2.setStroke(new BasicStroke(2f));
                    drawArrow(g2, startX, startY,
                        startX + (int)(dx * 22), startY + (int)(dy * 22));
                }
            }
        }

        private int sx(double x, double midX, double scale, double cx) {
            return (int)(cx + (x - midX) * scale);
        }

        private int sy(double y, double midY, double scale, double cy) {
            return (int)(cy - (y - midY) * scale);
        }

        private void drawArrow(Graphics2D g2, int x0, int y0, int x1, int y1) {
            double angle  = Math.atan2(y1 - y0, x1 - x0);
            double spread = Math.PI / 6;
            int    len    = 11;
            g2.fillPolygon(
                new int[]{ x1,
                    x1 - (int)(len * Math.cos(angle - spread)),
                    x1 - (int)(len * Math.cos(angle + spread)) },
                new int[]{ y1,
                    y1 - (int)(len * Math.sin(angle - spread)),
                    y1 - (int)(len * Math.sin(angle + spread)) },
                3
            );
        }
    }

    private static Color colorFor(PatternGenerator.Difficulty d) {
        return switch (d) {
            case EASY   -> EASY_COLOR;
            case MEDIUM -> MEDIUM_COLOR;
            case HARD   -> HARD_COLOR;
        };
    }

    private static String labelFor(PatternGenerator.Difficulty d) {
        return switch (d) {
            case EASY   -> "Łatwy";
            case MEDIUM -> "Średni";
            case HARD   -> "Trudny";
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatternPreviewApp().setVisible(true));
    }
}
