package com.jtjmpm;

public class StartGameMessage extends WsMessage {
    public StartGameMessage() {
        super("GAME_START");
    }
}
