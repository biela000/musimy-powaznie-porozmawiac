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

    private String hostId;
    private final Map<String, Player> players = new ConcurrentHashMap<>();


    private volatile MatchStatus status = MatchStatus.LOBBY;

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

    //GETTERS

    /** True when at least one player's HP has reached 0, ending the current round. */
    public boolean isRoundOver() {
        return players.values().stream().anyMatch(player -> player.getHp() <= 0);
    }

    /** True when all players died simultaneously (draw round). */
    public boolean isRoundDraw() {
        return players.values().stream().allMatch(player -> player.getHp() <= 0);
    }

    /** ID of the surviving player when the round ends, or null on a draw. */
    public String getRoundWinnerId() {
        if (isRoundDraw()) return null;
        return players.values().stream()
                .filter(player -> player.getHp() > 0)
                .findFirst()
                .map(Player::getId)
                .orElse(null);
    }

    /** Resets all players to full HP/mana for the next round. Wins are preserved. */
    public synchronized void resetRound() {
        for (Player player : players.values()) {
            player.resetForNewRound();
        }
    }

    // Legacy helpers kept for compatibility — semantics now refer to rounds, not the match.
    /** @deprecated Use {@link #isRoundOver()} instead. */
    public boolean isGameOver() { return isRoundOver(); }

    /** @deprecated Use {@link #isRoundDraw()} instead. */
    public boolean isDraw() { return isRoundDraw(); }

    /** @deprecated Use {@link #getRoundWinnerId()} instead. */
    public String getWinnerId() { return getRoundWinnerId(); }

    public synchronized MatchStatus getStatus() {
        return status;
    }

    // SETTER
    public synchronized void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    //SIMPLE GETTERS
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
