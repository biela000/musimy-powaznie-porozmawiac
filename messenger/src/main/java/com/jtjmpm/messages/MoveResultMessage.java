package com.jtjmpm.messages;

import com.jtjmpm.MessageType;
import com.jtjmpm.PlayerMoveResult;

import java.awt.geom.Point2D;
import java.util.List;

public class MoveResultMessage extends WsMessage {
    public PlayerMoveResult result;
    public String playerId;
    public String spellId;
    public int castDurationMs;
    public CastStatus status;
    public String accuracyRating;

    public MoveResultMessage(PlayerMoveResult result, String playerId, String spellId,
                             int castDurationMs, CastStatus status) {
        super(MessageType.MOVE_RESULT);
        this.result = result;
        this.playerId = playerId;
        this.spellId = spellId;
        this.castDurationMs = castDurationMs;
        this.status = status;
    }
}
