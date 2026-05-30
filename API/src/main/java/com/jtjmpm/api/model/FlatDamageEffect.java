package com.jtjmpm.api.model;

public class FlatDamageEffect implements SpellEffect {
    private final double baseDamage;

    public FlatDamageEffect(double baseDamage){
        this.baseDamage = baseDamage;
    }
    @Override
    public void apply(GameState state, String casterId, String targetId, double accuracy){
        Player target = state.getPlayer(targetId);
        if (target != null) {
            double actualDamage = Math.round(baseDamage * accuracy);

        }
    }
}
