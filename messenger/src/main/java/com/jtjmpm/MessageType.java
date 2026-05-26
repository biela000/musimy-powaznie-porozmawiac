package com.jtjmpm;

public enum MessageType {

    // Connection
    WELCOME,

    // Lobby
    LOBBY_JOINED,

    // Readiness & Game start
    TOGGLE_READY,
    GAME_START,

    // Gameplay
    PLAYER_MOVE,
    MOVE_RESULT,
    SHAPE_DRAWN,
    GAME_STATE_UPDATE,

    // Errors
    ERROR;
}
