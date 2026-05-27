package com.jtjmpm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public static final int LOBBY_SIZE = 2;

    private String hostId;
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    private boolean isGameStarted = false;

    public GameState() {
    }

    public GameState(String hostId) {
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

    public synchronized Collection<Player> getPlayers() {
        return players.values();
    }

    public synchronized void applyDamage(String targetPlayerId, int damage) {
        Player player = players.get(targetPlayerId);
        if (player != null) {
            player.setHp(player.getHp() - damage);
        }
    }

    //GETTERS
    public boolean isGameOver() {
        return false;
    }

    public String getWinnerId() {
        return null;
    }

    //SIMPLE GETTERS
    public boolean isGameStarted() { return isGameStarted; }
    public String getHostId() { return hostId; }
}
