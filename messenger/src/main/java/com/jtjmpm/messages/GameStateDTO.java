package com.jtjmpm.messages;

import java.util.Map;

public record GameStateDTO(
        String name,
        String hostId,
        boolean isGameStarted,
        Map<String, PlayerDTO> players
) {}