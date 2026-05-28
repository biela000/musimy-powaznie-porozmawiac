package com.jtjmpm.messages;

import com.jtjmpm.MessageType;

public class LobbyLeftMessage extends WsMessage {
    public String playerId;
    //public String lobbyId; ???

    public LobbyLeftMessage(String playerId) {
        super(MessageType.LEAVE_LOBBY);
        this.playerId = playerId;
    }

}