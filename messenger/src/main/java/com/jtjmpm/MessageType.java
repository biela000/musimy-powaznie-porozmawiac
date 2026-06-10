package com.jtjmpm;

public enum MessageType {

    // Connection
    WELCOME,

    // Lobby
    LOBBY_JOINED,
    CREATE_LOBBY,
    JOIN_LOBBY,
    LEAVE_LOBBY,
    DESTROY_LOBBY,
    LOBBY_DESTROYED,

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
    COMBAT_EVENT,
    GAME_OVER,
    RESTART_MATCH,

    // Round system
    ROUND_OVER,
    COUNTDOWN,
    ROUND_START,

    // Errors
    ERROR;
}
