package com.jtjmpm.desktop.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class DesktopConstants {

    public static final double SCREEN_WIDTH;
    public static final double SCREEN_HEIGHT;
    public static final double WIZARD_SIZE;
    public static final double WIZARD_OFFSET_X;
    public static final double WIZARD_OFFSET_Y;
    public static final double PROJECTILE_IMAGE_SIZE;
    public static final double PROJECTILE_SPAWN_OFFSET_X;
    public static final double PROJECTILE_SPAWN_OFFSET_Y;
    public static final double TEXT_SPAWN_OFFSET_X;
    public static final double TEXT_SPAWN_OFFSET_Y;
    public static final double TEXT_FLOAT_DISTANCE;
    public static final int SHAPE_DRAWER_PADDING;
    public static final int RECONNECT_DELAY_SECONDS;
    public static final String LOBBY_VIEW;
    public static final double TARGET_WIDTH;
    public static final double TARGET_HEIGHT;

    static {
        JsonObject json = load();
        SCREEN_WIDTH = json.get("screenWidth").getAsDouble();
        SCREEN_HEIGHT = json.get("screenHeight").getAsDouble();
        WIZARD_SIZE = json.get("wizardSize").getAsDouble();
        WIZARD_OFFSET_X = json.get("wizardOffsetX").getAsDouble();
        WIZARD_OFFSET_Y = json.get("wizardOffsetY").getAsDouble();
        PROJECTILE_IMAGE_SIZE = json.get("projectileImageSize").getAsDouble();
        PROJECTILE_SPAWN_OFFSET_X = json.get("projectileSpawnOffsetX").getAsDouble();
        PROJECTILE_SPAWN_OFFSET_Y = json.get("projectileSpawnOffsetY").getAsDouble();
        TEXT_SPAWN_OFFSET_X = json.get("textSpawnOffsetX").getAsDouble();
        TEXT_SPAWN_OFFSET_Y = json.get("textSpawnOffsetY").getAsDouble();
        TEXT_FLOAT_DISTANCE = json.get("textFloatDistance").getAsDouble();
        SHAPE_DRAWER_PADDING = json.get("shapeDrawerPadding").getAsInt();
        RECONNECT_DELAY_SECONDS = json.get("reconnectDelaySeconds").getAsInt();
        LOBBY_VIEW = json.get("lobbyView").getAsString();
        TARGET_WIDTH = json.get("targetWidth").getAsDouble();
        TARGET_HEIGHT = json.get("targetHeight").getAsDouble();
    }

    private static JsonObject load() {
        InputStream in = DesktopConstants.class.getResourceAsStream("/desktop-constants.json");
        if (in == null) {
            throw new IllegalStateException("desktop-constants.json not found on classpath");
        }
        return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private DesktopConstants() {
    }
}
