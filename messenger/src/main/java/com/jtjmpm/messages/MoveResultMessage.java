package com.jtjmpm.messages;

import com.jtjmpm.MessageType;
import com.jtjmpm.PlayerMoveResult;

import java.awt.geom.Point2D;
import java.util.List;

public class MoveResultMessage extends WsMessage {
    public PlayerMoveResult result;
    public String playerId;

    public MoveResultMessage(PlayerMoveResult result, String playerId) {
        super(MessageType.MOVE_RESULT);
        this.result = result;
        this.playerId = playerId;
    }
}
