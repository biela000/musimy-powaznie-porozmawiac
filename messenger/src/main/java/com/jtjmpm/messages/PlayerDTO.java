package com.jtjmpm.messages;

public record PlayerDTO(
        String id,
        double hp,
        double mana,
        boolean ready
) {}