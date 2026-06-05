package com.jtjmpm.api.model.PatternEngine;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class PatternGenerator {

    private PatternGenerator() {}

    public enum Difficulty { EASY, MEDIUM, HARD }

    public record NamedShape(String name, List<Point2D.Double> points, boolean randomizeStart) {
        public NamedShape(String name, List<Point2D.Double> points) {
            this(name, points, false);
        }
    }

    public static List<NamedShape> shuffledPool(Difficulty difficulty, int pointsCount) {
        List<NamedShape> base = new ArrayList<>(poolFor(difficulty, pointsCount));
        Collections.shuffle(base);

        Random rng = new Random();
        List<NamedShape> result = new ArrayList<>(base.size());
        for (NamedShape s : base) {
            List<Point2D.Double> pts = applyDistortion(s.points(), difficulty, rng);
            if (s.randomizeStart()) {
                pts = withStartOffset(pts, rng.nextInt(pointsCount));
            }
            result.add(new NamedShape(s.name(), pts, s.randomizeStart()));
        }
        return result;
    }

    public static List<Point2D.Double> createCircle(int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>();
        if (pointsCount <= 1) return raw;
        for (int i = 0; i < pointsCount; i++) {
            double angle = (2.0 * Math.PI * i) / (pointsCount - 1);
            raw.add(new Point2D.Double(Math.cos(angle), Math.sin(angle)));
        }
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createFigureEight(int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>();
        if (pointsCount <= 1) return raw;
        for (int i = 0; i < pointsCount; i++) {
            double angle = (2.0 * Math.PI * i) / (pointsCount - 1);
            raw.add(new Point2D.Double(Math.sin(2 * angle), Math.cos(angle)));
        }
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createTriangle(int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>();
        raw.add(new Point2D.Double(0, 1));
        raw.add(new Point2D.Double(-0.866, -0.5));
        raw.add(new Point2D.Double(0.866, -0.5));
        raw.add(new Point2D.Double(0, 1));
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createPolygon(int sides, int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>();
        double startAngle = -Math.PI / 2;
        for (int s = 0; s < sides; s++) {
            double angle     = startAngle + (2.0 * Math.PI * s) / sides;
            double nextAngle = startAngle + (2.0 * Math.PI * (s + 1)) / sides;
            Point2D.Double from = new Point2D.Double(Math.cos(angle), Math.sin(angle));
            Point2D.Double to   = new Point2D.Double(Math.cos(nextAngle), Math.sin(nextAngle));
            int seg = Math.max(1, pointsCount / sides);
            for (int i = 0; i < seg; i++) {
                double t = (double) i / seg;
                raw.add(new Point2D.Double(from.x + t * (to.x - from.x), from.y + t * (to.y - from.y)));
            }
        }
        raw.add(raw.get(0));
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createStar(int points, double innerRadius, int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>();
        double startAngle = -Math.PI / 2;
        for (int i = 0; i <= 2 * points; i++) {
            double angle = startAngle + (Math.PI * i) / points;
            double r     = (i % 2 == 0) ? 1.0 : innerRadius;
            raw.add(new Point2D.Double(r * Math.cos(angle), r * Math.sin(angle)));
        }
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createArrow(int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>(List.of(
            new Point2D.Double(-1.0,  0.30),
            new Point2D.Double( 0.10, 0.30),
            new Point2D.Double( 0.10, 0.75),
            new Point2D.Double( 1.00, 0.00),
            new Point2D.Double( 0.10,-0.75),
            new Point2D.Double( 0.10,-0.30),
            new Point2D.Double(-1.0, -0.30),
            new Point2D.Double(-1.0,  0.30)
        ));
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createCross(double armWidth, int pointsCount) {
        double w = armWidth;
        List<Point2D.Double> raw = new ArrayList<>(List.of(
            new Point2D.Double(-w, -1), new Point2D.Double( w, -1),
            new Point2D.Double( w, -w), new Point2D.Double( 1, -w),
            new Point2D.Double( 1,  w), new Point2D.Double( w,  w),
            new Point2D.Double( w,  1), new Point2D.Double(-w,  1),
            new Point2D.Double(-w,  w), new Point2D.Double(-1,  w),
            new Point2D.Double(-1, -w), new Point2D.Double(-w, -w),
            new Point2D.Double(-w, -1)
        ));
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createHeart(int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>();
        if (pointsCount <= 1) return raw;
        for (int i = 0; i < pointsCount; i++) {
            double t = (2.0 * Math.PI * i) / (pointsCount - 1);
            double x =  16 * Math.pow(Math.sin(t), 3);
            double y = -(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t));
            raw.add(new Point2D.Double(x, y));
        }
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> createSpiral(double turns, int pointsCount) {
        List<Point2D.Double> raw = new ArrayList<>();
        if (pointsCount <= 1) return raw;
        for (int i = 0; i < pointsCount; i++) {
            double t = (double) i / (pointsCount - 1);
            double angle = turns * 2.0 * Math.PI * t;
            raw.add(new Point2D.Double(t * Math.cos(angle), t * Math.sin(angle)));
        }
        return ShapeNormalizer.preProcess(raw, pointsCount, 0);
    }

    public static List<Point2D.Double> withAxisScale(List<Point2D.Double> shape, double scaleX, double scaleY) {
        List<Point2D.Double> out = new ArrayList<>();
        for (Point2D.Double p : shape) out.add(new Point2D.Double(p.x * scaleX, p.y * scaleY));
        return ShapeNormalizer.preProcess(out, shape.size(), 0);
    }

    public static List<Point2D.Double> withRotation(List<Point2D.Double> shape, double angleRadians) {
        double cos = Math.cos(angleRadians), sin = Math.sin(angleRadians);
        List<Point2D.Double> out = new ArrayList<>();
        for (Point2D.Double p : shape)
            out.add(new Point2D.Double(p.x * cos - p.y * sin, p.x * sin + p.y * cos));
        return ShapeNormalizer.preProcess(out, shape.size(), 0);
    }

    public static List<Point2D.Double> withHorizontalFlip(List<Point2D.Double> shape) {
        List<Point2D.Double> out = new ArrayList<>();
        for (Point2D.Double p : shape) out.add(new Point2D.Double(-p.x, p.y));
        return ShapeNormalizer.preProcess(out, shape.size(), 0);
    }

    public static List<Point2D.Double> withStartOffset(List<Point2D.Double> shape, int offsetSteps) {
        int n = shape.size();
        if (n == 0 || offsetSteps == 0) return shape;
        int offset = ((offsetSteps % n) + n) % n;
        List<Point2D.Double> out = new ArrayList<>();
        for (int i = 0; i < n; i++) out.add(shape.get((i + offset) % n));
        return ShapeNormalizer.preProcess(out, n, 0);
    }

    private static List<NamedShape> poolFor(Difficulty difficulty, int n) {
        return switch (difficulty) {
            case EASY   -> easyPool(n);
            case MEDIUM -> mediumPool(n);
            case HARD   -> hardPool(n);
        };
    }

    private static List<NamedShape> easyPool(int n) {
        return List.of(
            new NamedShape("Koło",           createCircle(n),                             true),
            new NamedShape("Elipsa pozioma", withAxisScale(createCircle(n), 2.2, 1.0),    true),
            new NamedShape("Elipsa pionowa", withAxisScale(createCircle(n), 1.0, 2.2),    true),
            new NamedShape("Trójkąt",        createTriangle(n),                           true),
            new NamedShape("Kwadrat",        createPolygon(4, n),                         true),
            new NamedShape("Ósemka",         createFigureEight(n),                        true)
        );
    }

    private static List<NamedShape> mediumPool(int n) {
        return List.of(
            new NamedShape("Elipsa ukośna 45°",    withRotation(withAxisScale(createCircle(n), 2.2, 1.0), Math.toRadians(45)),   true),
            new NamedShape("Elipsa ukośna 120°",   withRotation(withAxisScale(createCircle(n), 2.2, 1.0), Math.toRadians(120)),  true),
            new NamedShape("Trójkąt odwrócony",    withRotation(createTriangle(n), Math.toRadians(180)),                         true),
            new NamedShape("Trójkąt obrócony 60°", withRotation(createTriangle(n), Math.toRadians(60)),                          true),
            new NamedShape("Romb",                 withRotation(createPolygon(4, n), Math.toRadians(45)),                        true),
            new NamedShape("Pięciokąt",            createPolygon(5, n),                                                          true),
            new NamedShape("Ósemka pionowa",       withRotation(createFigureEight(n), Math.toRadians(90)),                       true),
            new NamedShape("Ósemka ukośna",        withRotation(createFigureEight(n), Math.toRadians(45)),                       true),
            new NamedShape("Gwiazda 4",            createStar(4, 0.45, n),                                                       true),
            new NamedShape("Strzałka",             createArrow(n),                                                               true),
            new NamedShape("Serce",                createHeart(n),                                                               false),
            new NamedShape("Spirala 1.5 obrotu",   createSpiral(1.5, n),                                                         false)
        );
    }

    private static List<NamedShape> hardPool(int n) {
        return List.of(
            new NamedShape("Sześciokąt",               createPolygon(6, n),                                                         true),
            new NamedShape("Sześciokąt obrócony 30°",  withRotation(createPolygon(6, n), Math.toRadians(30)),                       true),
            new NamedShape("Pięciokąt obrócony 36°",   withRotation(createPolygon(5, n), Math.toRadians(36)),                       true),
            new NamedShape("Elipsa bardzo ukośna 70°", withRotation(withAxisScale(createCircle(n), 2.5, 1.0), Math.toRadians(70)),  true),
            new NamedShape("Ósemka ukośna lustro",     withHorizontalFlip(withRotation(createFigureEight(n), Math.toRadians(45))),  true),
            new NamedShape("Gwiazda 5",                createStar(5, 0.38, n),                                                      true),
            new NamedShape("Gwiazda 5 obrócona 36°",   withRotation(createStar(5, 0.38, n), Math.toRadians(36)),                    true),
            new NamedShape("Gwiazda 6",                createStar(6, 0.50, n),                                                      true),
            new NamedShape("Gwiazda 8",                createStar(8, 0.60, n),                                                      true),
            new NamedShape("Krzyż",                    createCross(0.28, n),                                                        true),
            new NamedShape("Krzyż obrócony 45°",       withRotation(createCross(0.28, n), Math.toRadians(45)),                      true),
            new NamedShape("Strzałka obrócona 90°",    withRotation(createArrow(n), Math.toRadians(90)),                            true),
            new NamedShape("Strzałka obrócona 180°",   withRotation(createArrow(n), Math.toRadians(180)),                           true),
            new NamedShape("Serce odwrócone",          withRotation(createHeart(n), Math.toRadians(180)),                           false),
            new NamedShape("Spirala 2 obroty",         createSpiral(2.0, n),                                                        false),
            new NamedShape("Spirala 2.5 obrotu",       createSpiral(2.5, n),                                                        false)
        );
    }

    private static List<Point2D.Double> applyDistortion(List<Point2D.Double> shape, Difficulty d, Random rng) {
        double maxAngle = switch (d) { case EASY -> 10.0; case MEDIUM -> 20.0; case HARD -> 30.0; };
        double maxScale = switch (d) { case EASY -> 0.08; case MEDIUM -> 0.15; case HARD -> 0.22; };

        double angle = Math.toRadians((rng.nextDouble() * 2 - 1) * maxAngle);
        double sx    = 1.0 + (rng.nextDouble() * 2 - 1) * maxScale;
        double sy    = 1.0 + (rng.nextDouble() * 2 - 1) * maxScale;

        return withAxisScale(withRotation(shape, angle), sx, sy);
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
