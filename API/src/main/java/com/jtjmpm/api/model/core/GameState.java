package com.jtjmpm.api.model.core;

import com.jtjmpm.messages.GameStateDTO;
import com.jtjmpm.messages.PlayerDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public static final int LOBBY_SIZE = 2;

    private String hostId;
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    //TODO implement game start
    private boolean isGameStarted = false;

    private MatchStatus status = MatchStatus.LOBBY;

    private final String name;

    public GameState() {
        this.name = "unknown";
    }

    public GameState(String name, String hostId) {
        this.name = name;
        this.hostId = hostId;
        players.put(hostId, new Player(hostId, 200, 100));
    }

    // SYNCHRONIZED LOGIC
    public synchronized boolean addPlayer(String guestId) {
        if (players.size() < LOBBY_SIZE) {
            players.put(guestId, new Player(guestId, 200, 100));
            return true;
        }
        return false;
    }

    public synchronized boolean removePlayer(String playerId) {
        Player removed = players.remove(playerId);
        return removed != null;
    }

    public synchronized Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public synchronized Player getEnemy(String myId) {
        for (String id : players.keySet()) {
            if (!id.equals(myId)) {
                return players.get(id);
            }
        }
        return null;
    }

    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public synchronized boolean isReady() {
        //TODO
        //check if both players are in the lobby
        for (Player player : players.values()) {
            if (!player.isReady()) return false;
        }

        return true;
    }

    //GETTERS
    public boolean isGameOver() {
        return players.values().stream().anyMatch(player -> player.getHp() <= 0);
    }

    public boolean isDraw() {
        return players.values().stream().allMatch(player -> player.getHp() <= 0);
    }

    public String getWinnerId() {
        return players.values().stream()
                .filter(player -> player.getHp() > 0)
                .findFirst()
                .map(Player::getId)
                .orElse(null);
    }

    public MatchStatus getStatus() {
        return status;
    }

    // SETTER
    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    //SIMPLE GETTERS
    public boolean isGameStarted() { return isGameStarted; }
    public String getHostId() { return hostId; }

    public synchronized GameStateDTO toDTO() {
        Map<String, PlayerDTO> playerDTOs = new HashMap<>();
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            playerDTOs.put(entry.getKey(), entry.getValue().toDTO());
        }

        return new GameStateDTO(
                this.name,
                this.hostId,
                this.isGameStarted,
                playerDTOs
        );
    }
}
