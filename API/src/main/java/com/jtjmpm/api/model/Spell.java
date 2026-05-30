package com.jtjmpm.api.model;

import com.jtjmpm.messages.SpellDTO;
import com.jtjmpm.messages.SpellType;

public record Spell(
        String name,
        String description,
        SpellType type,
        int baseDamage
) {
    public SpellDTO toDTO() {
        return new SpellDTO(name, description, type, baseDamage);
    }
}
