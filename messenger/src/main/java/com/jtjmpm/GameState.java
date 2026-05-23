package com.jtjmpm;

public class GameState {
    private final String player1Id; // Host
    private String player2Id;       // Gość

    private int player1Hp = 100;
    private int player2Hp = 100;

    private boolean player1Ready = false;
    private boolean player2Ready = false;

    private boolean isGameStarted = false;

    //player 1 is always the host
    public GameState(String hostId) {
        this.player1Id = hostId;
    }

    // SYNCHRONIZED LOGIC

    public synchronized boolean addPlayer2(String guestId) {
        if (this.player2Id == null) {
            this.player2Id = guestId;
            return true;
        }
        return false;
    }

    public synchronized boolean setPlayerReady(String playerId) {
        if (playerId.equals(player1Id)) player1Ready = !player1Ready;
        if (playerId.equals(player2Id)) player2Ready = !player2Ready;

        // Returns true if both players are ready and the game can be started
        if (player1Ready && player2Ready && !isGameStarted) {
            isGameStarted = true;
            return true;
        }
        return false;
    }

    public synchronized void applyDamage(String targetPlayerId, int damage) {
        if (targetPlayerId.equals(player1Id)) {
            player1Hp = Math.max(0, player1Hp - damage);
        } else if (targetPlayerId.equals(player2Id)) {
            player2Hp = Math.max(0, player2Hp - damage);
        }
    }

    //GETTERS

    public boolean isGameOver() {
        return player1Hp <= 0 || player2Hp <= 0;
    }

    public String getWinnerId() {
        if (player1Hp <= 0) return player2Id;
        if (player2Hp <= 0) return player1Id;
        return null;
    }

    //SIMPLE GETTERS

    public String getPlayer1Id() { return player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public int getPlayer1Hp() { return player1Hp; }
    public int getPlayer2Hp() { return player2Hp; }
    public boolean isGameStarted() { return isGameStarted; }
}
