package com.jtjmpm.desktop.model;

import java.util.List;

public class GameStateManager {
    private static volatile GameStateManager instance;

    private volatile String hostId;
    private volatile String enemyId;
    private volatile List<String> currentLoadout;

    private GameStateManager() {}

    public static GameStateManager getInstance() {
        if (instance == null) {
            synchronized (GameStateManager.class) {
                if (instance == null) {
                    instance = new GameStateManager();
                }
            }
        }
        return instance;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getEnemyId() {
        return enemyId;
    }

    public void setEnemyId(String enemyId) {
        this.enemyId = enemyId;
    }

    public List<String> getCurrentLoadout() {
        return currentLoadout;
    }

    public void setCurrentLoadout(List<String> currentLoadout) {
        this.currentLoadout = currentLoadout;
    }
}
