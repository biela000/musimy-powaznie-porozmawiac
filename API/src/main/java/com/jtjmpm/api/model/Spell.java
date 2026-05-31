package com.jtjmpm.api.model;

import com.jtjmpm.messages.SpellDTO;
import com.jtjmpm.messages.SpellType;

public record Spell(
        String name,
        String description,
        SpellType type,
        int basePower, //TODO change to double
        double manaCost,
        SpellEffect effect
) {
    public SpellDTO toDTO() {
        return new SpellDTO(name, description, type, basePower);
    }
}
