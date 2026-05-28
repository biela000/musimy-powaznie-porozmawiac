package com.jtjmpm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public static final int LOBBY_SIZE = 2;

    private String hostId;
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    private boolean isGameStarted = false;

    private final String name;

    public GameState() {
        this.name = "unknown";
    }

    public GameState(String name, String hostId) {
        this.name = name;
        this.hostId = hostId;
        players.put(hostId, new Player(hostId));
    }

    // SYNCHRONIZED LOGIC
    public synchronized boolean addPlayer(String guestId) {
        if (players.size() < LOBBY_SIZE) {
            players.put(guestId, new Player(guestId));
            return true;
        }
        return false;
    }

    public synchronized Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public synchronized void applyDamage(String targetPlayerId, int damage) {
        Player player = players.get(targetPlayerId);
        if (player != null) {
            player.setHp(player.getHp() - damage);
        }
    }

    public synchronized boolean isReady() {
        for (Player player : players.values()) {
            if (!player.isReady()) return false;
        }

        return true;
    }

    //GETTERS
    public boolean isGameOver() {
        return false;
    }

    public String getWinnerId() {
        return null;
    }

    public String getName() {
        return name;
    }

    //SIMPLE GETTERS
    public boolean isGameStarted() { return isGameStarted; }
    public String getHostId() { return hostId; }
}
