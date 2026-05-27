package com.jtjmpm;

public class Player {
    public static final int MAX_HP = 100;

    private final String id;
    private int hp;
    private boolean ready;

    public Player(String id) {
        this.id = id;
        this.hp = MAX_HP;
        this.ready = false;
    }

    public synchronized int getHp() {
        return hp;
    }

    public synchronized void setHp(int hp) {
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
