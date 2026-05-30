package com.jtjmpm.messages;

public record SpellDTO(
        String name,
        String description,
        SpellType type,
        int displayDamage
) {}