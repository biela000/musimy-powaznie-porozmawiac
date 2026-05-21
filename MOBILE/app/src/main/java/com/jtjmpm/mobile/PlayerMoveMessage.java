package com.jtjmpm.mobile;

import java.util.List;

public class PlayerMoveMessage extends WsMessage {
    public List<PlayerPosition> playerMove;
    public PlayerMoveMessage(List<PlayerPosition> playerMove) {
        super("PLAYER_MOVE");
        this.playerMove = playerMove;
    }
}
