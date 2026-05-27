package com.jtjmpm.desktop.utils;

import com.jtjmpm.Player;

import java.util.Collection;

public class GameStateUtils {
    public static Player getEnemy(Collection<Player> players, String hostId) {
        for (Player player : players) {
            if (!player.getId().equals(hostId)) {
                return player;
            }
        }
        return null;
    }
}
