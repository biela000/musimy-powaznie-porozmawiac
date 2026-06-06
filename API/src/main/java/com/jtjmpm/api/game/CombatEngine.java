package com.jtjmpm.api.game;

import com.jtjmpm.api.model.GameState;
import com.jtjmpm.api.model.Player;
import com.jtjmpm.api.model.Spell;
import com.jtjmpm.messages.CombatEventMessage;
import com.jtjmpm.messages.CombatEventType;
import org.springframework.stereotype.Component;

@Component
public class CombatEngine {

    // processing status buffs, debuffs, blocks etc. will go in these methods

    public CombatEventMessage applyDamage(GameState gameState, String casterId, String targetId, double rawDamage){
        //some potential damage calculations here
        double finalDamage = rawDamage;
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(-finalDamage);
        System.out.println("Player: " + casterId + " did " + finalDamage + " damage to: " + targetId);

        //type will be different after we implement processing effects here
        return new CombatEventMessage(targetId, CombatEventType.HIT, finalDamage);
    }

    public CombatEventMessage applyStatusDamage(GameState gameState, String targetId, double rawDamage) {
        //some potential damage calculations here
        double finalDamage = rawDamage;
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(-finalDamage);
        System.out.println("Player: " + targetId + " took some damage: " + finalDamage);

        return new CombatEventMessage(targetId, CombatEventType.HIT, finalDamage);
    }

    public void applyManaRegen(GameState gameState, String targetId, double rawValue) {
        //some potential damage calculations here
        double finalValue = rawValue;
        Player target = gameState.getPlayer(targetId);
        target.modifyMana(finalValue);
        System.out.println("Player: " + targetId + " regenerated some mana: " + finalValue);
    }

    public void applyManaUsage(GameState gameState, String targetId, double rawValue){
        //some potential damage calculations here
        double finalValue = rawValue;
        Player target = gameState.getPlayer(targetId);
        target.modifyMana(-finalValue);
        System.out.println("Player: " + targetId + " used " + finalValue + " mana");
    }

    public CombatEventMessage applyHeal(GameState gameState, String targetId, double rawHeal) {
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(rawHeal);
        System.out.println("Player: " + targetId + " healed for: " + rawHeal);

        return new CombatEventMessage(targetId, CombatEventType.HEAL, rawHeal);
    }
}