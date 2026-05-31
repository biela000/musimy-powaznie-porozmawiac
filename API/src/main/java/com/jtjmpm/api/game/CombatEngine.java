package com.jtjmpm.api.game;

import com.jtjmpm.api.model.GameState;
import com.jtjmpm.api.model.Player;
import com.jtjmpm.api.model.Spell;
import org.springframework.stereotype.Component;

@Component
public class CombatEngine {
    public void applyDamage(Player caster, Player target, double rawDamage){
        //some potential damage calculations here
        double finalDamage = rawDamage;
        target.modifyHp(rawDamage);
        System.out.println("Player: " + caster.getId() + " did " + rawDamage + " damage to: " + target.getId());
    }
}