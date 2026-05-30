package com.jtjmpm;

public enum MessageType {

    // Connection
    WELCOME,

    // Lobby
    LOBBY_JOINED,
    CREATE_LOBBY,
    JOIN_LOBBY,
    LEAVE_LOBBY,

    // Readiness & Game start
    TOGGLE_READY,
    GAME_START,

    // Spells
    GET_SPELLS_LIST,
    AVAILABLE_SPELLS,

    // Gameplay
    PLAYER_MOVE,
    MOVE_RESULT,
    SHAPE_DRAWN,
    GAME_STATE_UPDATE,

    // Errors
    ERROR;
}
