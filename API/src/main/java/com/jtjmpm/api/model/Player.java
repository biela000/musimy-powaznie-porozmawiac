package com.jtjmpm.api.model;

import com.jtjmpm.api.game.CombatEngine;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.PlayerDTO;
import com.jtjmpm.messages.StatusEffectDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    public static final double MAX_HP = 100;
    public static final double MAX_MANA = 100;

    private final String id;
    private double hp;
    private double mana;
    private boolean ready;

    private final List<String> spellLoadout = new ArrayList<>();
    private final List<StatusEffect> activeEffects = new ArrayList<>();

    public Player(String id) {
        this.id = id;
        this.hp = MAX_HP;
        this.mana = MAX_MANA;
        this.ready = false;
        this.activeEffects.add(new BaseManaRegenEffect(10.0)); //..
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

    public synchronized double getMana() {
        return mana;
    }

    public synchronized void modifyMana(double amount) {
        this.mana += amount;
        if (this.mana > MAX_MANA) {
            this.mana = MAX_MANA;
        } else if (this.mana < 0) {
            this.mana = 0;
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

    public synchronized List<StatusEffect> getActiveEffects() {
        return activeEffects;
    }

    public void processEffects(GameState gameState, CombatEngine combatEngine, List<CombatEventMessage> outEvents) {
        for (StatusEffect effect : activeEffects) {
            effect.onTick(gameState, this.getId(), combatEngine, outEvents);

            effect.decreaseDuration();
        }

        activeEffects.removeIf(StatusEffect::isExpired);
    }

    public synchronized List<String> getSpellLoadout() {
        return Collections.unmodifiableList(spellLoadout);
    }

    public synchronized void setSpellLoadout(List<String> spellLoadout) {
        if (spellLoadout != null) {
            this.spellLoadout.addAll(spellLoadout);
        }
    }

    public String getId() {
        return id;
    }

    public synchronized PlayerDTO toDTO() {
        return new PlayerDTO(
                this.id, this.hp, this.mana, this.ready,
                this.activeEffects.stream()
                        .filter(StatusEffect::isVisibleOnUI)
                        .map(effect -> new StatusEffectDTO(effect.getName(), effect.getRemainingDuration()))
                        .collect(Collectors.toList())
        );
    }
}
