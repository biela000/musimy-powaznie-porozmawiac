package com.jtjmpm.api.model.core;

import com.jtjmpm.api.service.CombatEngine;
import com.jtjmpm.api.model.status.BaseManaRegenEffect;
import com.jtjmpm.api.model.status.StatusEffect;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.PlayerDTO;
import com.jtjmpm.messages.StatusEffectDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    private final String id;
    private final double maxHp;
    private final double maxMana;
    private double hp;
    private double mana;
    private boolean ready;

    private final List<String> spellLoadout = new ArrayList<>();
    private final List<StatusEffect> activeEffects = new ArrayList<>();

    public Player(String id, double maxHp, double maxMana) {
        this.id = id;
        this.maxHp = maxHp;
        this.maxMana = maxMana;
        this.hp = maxHp;
        this.mana = 20;
        this.ready = false;
        this.activeEffects.add(new BaseManaRegenEffect(5.0, 0.5)); //..
    }

    public synchronized double getHp() {
        return hp;
    }

    public synchronized void modifyHp(double amount) {
        this.hp += amount;
        if (this.hp > maxHp) {
            this.hp = maxHp;
        } else if (this.hp < 0) {
            this.hp = 0;
        }
    }

    public synchronized double getMana() {
        return mana;
    }

    public synchronized void modifyMana(double amount) {
        this.mana += amount;
        if (this.mana > maxMana) {
            this.mana = maxMana;
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
                this.id, this.hp, this.mana, this.maxHp, this.maxMana, this.ready,
                this.activeEffects.stream()
                        .filter(StatusEffect::isVisibleOnUI)
                        .map(effect -> new StatusEffectDTO(effect.getName(), effect.getRemainingDuration()))
                        .collect(Collectors.toList())
        );
    }
}
