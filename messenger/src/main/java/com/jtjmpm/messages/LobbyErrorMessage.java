package com.jtjmpm.messages;

import com.jtjmpm.WsErrorName;

public class LobbyErrorMessage extends WsErrorMessage{
    public LobbyErrorMessage(String description) {
        super(WsErrorName.LOBBY_ERROR, description);
    }
}
