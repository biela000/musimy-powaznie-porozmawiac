package com.jtjmpm.api.model.spell;

import com.jtjmpm.api.model.spell.effect.SpellEffect;
import com.jtjmpm.messages.SpellDTO;
import com.jtjmpm.messages.SpellType;

public record Spell(
        String name,
        String description,
        SpellType type,
        int displayPower,
        int manaCost,
        int castDurationMs,
        SpellEffect effect
) {
    public SpellDTO toDTO() {
        return new SpellDTO(name, description, type, displayPower, manaCost);
    }
}
