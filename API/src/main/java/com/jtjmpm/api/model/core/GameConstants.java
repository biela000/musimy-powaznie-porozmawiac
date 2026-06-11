package com.jtjmpm.api.model.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class GameConstants {

    public static final int TICKS_PER_SECOND;
    public static final int LOBBY_SIZE;
    public static final double PLAYER_MAX_HP;
    public static final double PLAYER_MAX_MANA;
    public static final double STARTING_MANA;
    public static final int WINS_TO_WIN;
    public static final int START_GAME_DELAY_SECONDS;
    public static final int NORMALIZED_SHAPE_POINT_COUNT;
    public static final int NORMALIZED_SHAPE_TRIM_COUNT;

    public static final JsonObject PATTERN_PREVIEW;

    static {
        JsonObject json = load();
        TICKS_PER_SECOND = json.get("ticksPerSecond").getAsInt();
        LOBBY_SIZE = json.get("lobbySize").getAsInt();
        PLAYER_MAX_HP = json.get("playerMaxHp").getAsDouble();
        PLAYER_MAX_MANA = json.get("playerMaxMana").getAsDouble();
        STARTING_MANA = json.get("startingMana").getAsDouble();
        WINS_TO_WIN = json.get("winsToWin").getAsInt();
        START_GAME_DELAY_SECONDS = json.get("startGameDelaySeconds").getAsInt();
        NORMALIZED_SHAPE_POINT_COUNT = json.get("normalizedShapePointCount").getAsInt();
        NORMALIZED_SHAPE_TRIM_COUNT = json.get("normalizedShapeTrimCount").getAsInt();
        PATTERN_PREVIEW = json.getAsJsonObject("patternPreview");
    }

    private static JsonObject load() {
        InputStream in = GameConstants.class.getResourceAsStream("/game-constants.json");
        if (in == null) {
            throw new IllegalStateException("game-constants.json not found on classpath");
        }
        return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private GameConstants() {
    }
}
