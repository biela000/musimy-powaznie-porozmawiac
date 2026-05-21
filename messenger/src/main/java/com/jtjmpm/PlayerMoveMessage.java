package com.jtjmpm;

import java.util.List;

public class PlayerMoveMessage extends WsMessage {
    public List<ControllerRotation> move;

    public PlayerMoveMessage(List<ControllerRotation> move) {
        super("PLAYER_MOVE");
        this.move = move;
    }
}
