package com.jtjmpm.messages;

public record PlayerDTO(
        String id,
        int hp,
        boolean ready
) {}