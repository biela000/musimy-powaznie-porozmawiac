package com.jtjmpm.api.model;

import com.jtjmpm.messages.CastStatus;

public class SpellCastResult {
    public CastStatus status;
    public int delayMs; // maybe redundant but its ok
    public Runnable impactAction;

    public SpellCastResult(CastStatus status) {
        this.status = status;
        this.delayMs = 0;
        this.impactAction = null;
    }

    public SpellCastResult(CastStatus status, int delayMs, Runnable impactAction) {
        this.status = status;
        this.delayMs = delayMs;
        this.impactAction = impactAction;
    }
}
