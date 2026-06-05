package com.jtjmpm.messages;

import com.jtjmpm.ControllerRotation;
import com.jtjmpm.MessageType;

import java.util.List;

public class PlayerMoveMessage extends WsMessage {
    public List<ControllerRotation> move;
    public int spellIndex;

    public PlayerMoveMessage(List<ControllerRotation> move, int spellIndex) {
        super(MessageType.PLAYER_MOVE);
        this.move = move;
        this.spellIndex = spellIndex;
    }
}
