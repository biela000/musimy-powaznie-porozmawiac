package com.jtjmpm.messages;

public class StatusEffectDTO {
    public String effectName;
    public int durationTicks;

    public StatusEffectDTO(String effectName, int durationTicks) {
        this.effectName = effectName;
        this.durationTicks = durationTicks;
    }
}
