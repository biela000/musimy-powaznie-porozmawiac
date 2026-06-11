package com.jtjmpm.api.model.core;

import com.jtjmpm.messages.GameStateDTO;
import com.jtjmpm.messages.MatchStatus;
import com.jtjmpm.messages.PlayerDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    public static final int LOBBY_SIZE = 2;

    private static final double PLAYER_MAX_HP = 200;
    private static final double PLAYER_MAX_MANA = 100;

    private String hostId;
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    private volatile MatchStatus status = MatchStatus.LOBBY;

    private final String name;

    public GameState(String name, String hostId) {
        this.name = name;
        this.hostId = hostId;
        players.put(hostId, new Player(hostId, PLAYER_MAX_HP, PLAYER_MAX_MANA));
    }

    public synchronized boolean addPlayer(String guestId) {
        if (players.size() < LOBBY_SIZE) {
            players.put(guestId, new Player(guestId, PLAYER_MAX_HP, PLAYER_MAX_MANA));
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

    public synchronized void resetAllPlayersReady() {
        for (Player player : players.values()) {
            player.setReady(false);
        }
    }

    public synchronized boolean isReady() {
        if (players.size() < LOBBY_SIZE) {
            return false;
        }

        for (Player player : players.values()) {
            if (!player.isReady()) return false;
        }

        return true;
    }

    public boolean isRoundOver() {
        return players.values().stream().anyMatch(player -> player.getHp() <= 0);
    }

    public boolean isRoundDraw() {
        return players.values().stream().allMatch(player -> player.getHp() <= 0);
    }

    public String getRoundWinnerId() {
        if (isRoundDraw()) return null;
        return players.values().stream()
                .filter(player -> player.getHp() > 0)
                .findFirst()
                .map(Player::getId)
                .orElse(null);
    }

    public synchronized void resetRound() {
        for (Player player : players.values()) {
            player.resetForNewRound();
        }
    }

    public synchronized MatchStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getHostId() { return hostId; }

    public synchronized GameStateDTO toDTO() {
        Map<String, PlayerDTO> playerDTOs = new HashMap<>();
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            playerDTOs.put(entry.getKey(), entry.getValue().toDTO());
        }

        return new GameStateDTO(
                this.name,
                this.hostId,
                this.status,
                playerDTOs
        );
    }
}
