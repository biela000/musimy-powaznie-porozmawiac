package com.jtjmpm.messages;

import java.util.Map;

public record GameStateDTO(
        String name,
        String hostId,
        MatchStatus status,
        Map<String, PlayerDTO> players
) {}