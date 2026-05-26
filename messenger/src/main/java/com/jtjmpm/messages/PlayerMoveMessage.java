package com.jtjmpm.messages;

import com.jtjmpm.ControllerRotation;
import com.jtjmpm.MessageType;

import java.util.List;

public class PlayerMoveMessage extends WsMessage {
    public List<ControllerRotation> move;

    public PlayerMoveMessage(List<ControllerRotation> move) {
        super(MessageType.PLAYER_MOVE);
        this.move = move;
    }
}
