package com.jtjmpm.messages;

public class StatusEffectDTO {
    public String effectName;
    public int durationTicks;
    public boolean isPositive;

    public StatusEffectDTO(String effectName, int durationTicks, boolean isPositive) {
        this.effectName = effectName;
        this.durationTicks = durationTicks;
        this.isPositive = isPositive;
    }
}
