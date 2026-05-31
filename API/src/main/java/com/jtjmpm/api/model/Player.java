package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.PlayerDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    public static final double MAX_HP = 100;

    private final String id;
    private double hp;
    private boolean ready;

    private List<String> spellLoadout;
    private List<StatusEffect> activeEffects = new ArrayList<>();

    public Player(String id) {
        this.id = id;
        this.hp = MAX_HP;
        this.ready = false;
        this.spellLoadout = new ArrayList<>();
    }

    public synchronized double getHp() {
        return hp;
    }

    public synchronized void modifyHp(double amount) {
        this.hp += amount;
        if (this.hp > MAX_HP) {
            this.hp = MAX_HP;
        } else if (this.hp < 0) {
            this.hp = 0;
        }
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

    public synchronized void addEffect(StatusEffect effect) {
        activeEffects.add(effect);
    }

    public synchronized void processEffects(GameState gameState, CombatEngine combatEngine) {
        activeEffects.removeIf(effect -> effect.tick(gameState, id, combatEngine));
    }

    public synchronized List<String> getSpellLoadout() {
        return Collections.unmodifiableList(spellLoadout);
    }

    public synchronized void setSpellLoadout(List<String> spellLoadout) {
        if (spellLoadout == null) {
            this.spellLoadout = new ArrayList<>();
        } else {
            this.spellLoadout = new ArrayList<>(spellLoadout);
        }
    }

    public String getId() {
        return id;
    }

    public synchronized PlayerDTO toDTO() {
        return new PlayerDTO(this.id, this.hp, this.ready);
    }
}
