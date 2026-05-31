package com.jtjmpm.api.game;

import com.jtjmpm.api.model.GameState;
import com.jtjmpm.api.model.Player;
import com.jtjmpm.api.model.Spell;
import org.springframework.stereotype.Component;

@Component
public class CombatEngine {
    public void applyDamage(GameState gameState, String casterId, String targetId, double rawDamage){
        //some potential damage calculations here
        double finalDamage = rawDamage;
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(-finalDamage);
        System.out.println("Player: " + casterId + " did " + finalDamage + " damage to: " + targetId);
    }

    public void applyStatusDamage(GameState gameState, String targetId, double rawDamage) {
        //some potential damage calculations here
        double finalDamage = rawDamage;
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(-finalDamage);
        System.out.println("Player: " + targetId + " took some damage: " + finalDamage);
    }

    public void applyManaRegen(GameState gameState, String targetId, double rawValue) {
        //some potential damage calculations here
        double finalValue = rawValue;
        Player target = gameState.getPlayer(targetId);
        target.modifyHp(-finalValue);
        System.out.println("Player: " + targetId + " regenerated some mana: " + finalValue);
    }

    public void applyManaUsage(GameState gameState, String targetId, double rawValue){
        //some potential damage calculations here
        double finalValue = rawValue;
        Player target = gameState.getPlayer(targetId);
        target.modifyMana(-finalValue);
        System.out.println("Player: " + targetId + " used " + finalValue + " mana");
    }
}