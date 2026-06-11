package com.jtjmpm.api.service;

import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.status.StatusEffect;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.CombatEventType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CombatEngine {

    public void applyDamage(GameState gameState, String casterId, String targetId, double rawDamage, List<CombatEventMessage> outEvents) {
        Player caster = gameState.getPlayer(casterId);
        Player target = gameState.getPlayer(targetId);

        double modifiedDamage = rawDamage;

        for (StatusEffect effect : caster.getActiveEffects()) {
            modifiedDamage = effect.modifyOutgoingDamage(modifiedDamage);
        }

        for (StatusEffect effect : target.getActiveEffects()) {
            modifiedDamage = effect.modifySpellIncomingDamage(modifiedDamage);
        }

        target.modifyHp(-modifiedDamage);
        System.out.println("Player: " + casterId + " did " + modifiedDamage + " damage to: " + targetId);

        CombatEventMessage hitEvent = new CombatEventMessage(targetId, CombatEventType.HIT, modifiedDamage);
        outEvents.add(hitEvent);

        for (StatusEffect effect : caster.getActiveEffects()) {
            effect.onAttackLanded(hitEvent, gameState, casterId, this, outEvents);
        }

        for (StatusEffect effect : target.getActiveEffects()) {
            effect.onDamageTaken(modifiedDamage, gameState, targetId, casterId, this, outEvents);
        }
    }

    public CombatEventMessage applyStatusDamage(GameState gameState, String targetId, double rawDamage) {
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(-rawDamage);
        System.out.println("Player: " + targetId + " took some damage: " + rawDamage);

        return new CombatEventMessage(targetId, CombatEventType.HIT, rawDamage);
    }

    public void applyManaRegen(GameState gameState, String targetId, double rawValue) {
        Player target = gameState.getPlayer(targetId);
        target.modifyMana(rawValue);
        System.out.println("Player: " + targetId + " regenerated some mana: " + rawValue);
    }

    public void applyManaUsage(GameState gameState, String targetId, double rawValue) {
        Player target = gameState.getPlayer(targetId);
        target.modifyMana(-rawValue);
        System.out.println("Player: " + targetId + " used " + rawValue + " mana");
    }

    public CombatEventMessage applyHeal(GameState gameState, String targetId, double rawHeal) {
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(rawHeal);
        System.out.println("Player: " + targetId + " healed for: " + rawHeal);

        return new CombatEventMessage(targetId, CombatEventType.HEAL, rawHeal);
    }
}
