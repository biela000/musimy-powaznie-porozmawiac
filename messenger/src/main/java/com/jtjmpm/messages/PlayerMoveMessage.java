package com.jtjmpm.messages;

import com.jtjmpm.ControllerRotation;
import com.jtjmpm.MessageType;

import java.util.List;

public class PlayerMoveMessage extends WsMessage {
    public List<ControllerRotation> move;
    public String spellId;

    public PlayerMoveMessage(List<ControllerRotation> move, String spellId) {
        super(MessageType.PLAYER_MOVE);
        this.move = move;
        this.spellId = spellId;
    }
}
