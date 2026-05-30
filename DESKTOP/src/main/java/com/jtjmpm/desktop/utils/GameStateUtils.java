package com.jtjmpm.desktop.utils;

import com.jtjmpm.messages.PlayerDTO;

import java.util.Collection;

public class GameStateUtils {
    public static PlayerDTO getEnemy(Collection<PlayerDTO> players, String hostId) {
        for (PlayerDTO player : players) {
            if (!player.id().equals(hostId)) {
                return player;
            }
        }
        return null;
    }
}
