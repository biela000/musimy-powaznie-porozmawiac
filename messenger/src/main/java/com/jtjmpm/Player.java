package com.jtjmpm;

import java.awt.geom.Point2D;
import java.util.List;

public class Player {
    public static final double MAX_HP = 100;

    private final String id;
    private double hp;
    private boolean ready;
    private List<Point2D.Double> lastMove;

    public Player(String id) {
        this.id = id;
        this.hp = MAX_HP;
        this.ready = false;
    }

    public synchronized double getHp() {
        return hp;
    }

    public synchronized void setHp(double hp) {
        this.hp = Math.max(hp, 0);
    }

    public synchronized boolean isReady() {
        return ready;
    }

    public synchronized void setReady(boolean ready) {
        this.ready = ready;
    }

    public synchronized void toggleReady() {
        ready = !ready;
    }

    public synchronized void setLastMove(List<Point2D.Double> lastMove) {
        this.lastMove = lastMove;
    }

    public synchronized List<Point2D.Double> getLastMove() {
        return lastMove;
    }

    public String getId() {
        return id;
    }
}
