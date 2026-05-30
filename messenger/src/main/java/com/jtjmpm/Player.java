package com.jtjmpm;

import com.jtjmpm.messages.PlayerDTO;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public static final double MAX_HP = 100;

    private final String id;
    private double hp;
    private boolean ready;

    private List<String> spellLoadout;

    public Player(String id) {
        this.id = id;
        this.hp = MAX_HP;
        this.ready = false;
        this.spellLoadout = new ArrayList<>();
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

    public List<String> getSpellLoadout() {
        return spellLoadout;
    }

    public void setSpellLoadout(List<String> spellLoadout) {
        this.spellLoadout = spellLoadout;
    }

    public String getId() {
        return id;
    }

    public synchronized PlayerDTO toDTO() {
        return new PlayerDTO(this.id, this.hp, this.ready);
    }
}
