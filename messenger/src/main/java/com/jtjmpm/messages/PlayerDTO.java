package com.jtjmpm.messages;

import java.util.List;

public record PlayerDTO(
        String id,
        double hp,
        double mana,
        double maxHp,
        double maxMana,
        boolean ready,
        List<StatusEffectDTO> activeEffects,
        int wins
) {}
