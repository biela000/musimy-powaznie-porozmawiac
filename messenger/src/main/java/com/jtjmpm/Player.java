package com.jtjmpm;

public class Player {
    public static final double MAX_HP = 100;

    private final String id;
    private double hp;
    private boolean ready;

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

    public String getId() {
        return id;
    }
}
