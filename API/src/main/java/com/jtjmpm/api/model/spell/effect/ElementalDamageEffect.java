package com.jtjmpm.api.model.spell.effect;
import com.jtjmpm.api.model.core.GameState;
import com.jtjmpm.api.model.core.Player;
import com.jtjmpm.api.model.spell.Element;
import com.jtjmpm.api.model.spell.SpellCastResult;
import com.jtjmpm.api.model.spell.effect.SpellEffect;
import com.jtjmpm.api.service.CombatEngine;

public class ElementalDamageEffect implements SpellEffect {
    private final Element element;
    private final double baseDamage;
    private final int castDuration;

    public ElementalDamageEffect(Element element, double baseDamage, int castDuration) {
        this.element = element;
        this.baseDamage = baseDamage;
        this.castDuration = castDuration;
    }

    @Override
    public SpellCastResult cast(GameState state, String casterId, String targetId, double accuracy, CombatEngine combatEngine) {
        Player caster = state.getPlayer(casterId);
        Element previousElement = caster.getLastCastElement();
        int comboCount = caster.getElementComboCount();
        double currentMultiplier = 1.0;

        if (this.element == Element.FIRE && previousElement == Element.FIRE) {
            currentMultiplier += (0.2 * comboCount);
            System.out.println(casterId + " triggered IGNITE! Multiplier: " + currentMultiplier);
        }

        if (this.element == Element.AIR && previousElement == Element.WATER) {
            currentMultiplier = 1.3;
            System.out.println(casterId + " triggered CONDUCTIVE STORM!");
        }

        final double finalComboMultiplier = currentMultiplier;

        return new SpellCastResult(castDuration, targetId, outEvents -> {
            double finalDamage = baseDamage * accuracy * finalComboMultiplier;
            combatEngine.applyDamage(state, casterId, targetId, finalDamage, outEvents);
            if (this.element == Element.AIR && previousElement == Element.WATER) {
                combatEngine.applyManaUsage(state, targetId, 10.0);
            }
        });
    }
}