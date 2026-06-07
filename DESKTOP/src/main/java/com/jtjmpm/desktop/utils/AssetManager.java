package com.jtjmpm.desktop.utils;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetManager {

    private static AssetManager instance;

    private final List<Image> idleFrames = new ArrayList<>();
    private final List<Image> attackFrames = new ArrayList<>();
    private final List<Image> attack2Frames = new ArrayList<>();
    private final List<Image> hitFrames = new ArrayList<>();
    private final List<Image> deathFrames = new ArrayList<>();
    private final Map<String, List<Image>> loadedEffects = new HashMap<>();
    private final Map<String, String> spellEffectBindings = new HashMap<>();
    
    private final List<List<Image>> fireballFramesList = new ArrayList<>();
    private final Map<String, Integer> spellColors = new HashMap<>();

    private AssetManager() {
        loadAnimations();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    private void loadAnimations() {
        try {
            for (int i = 1; i <= 6; i++) {
                idleFrames.add(new Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Idle_animation/Idle" + i + ".png")));
            }
            for (int i = 1; i <= 8; i++) {
                attackFrames.add(new Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Attack1_animation/Attack1_" + i + ".png")));
            }
            for (int i = 0; i < 8; i++) {
                attack2Frames.add(new Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Attack2_animation/Attack2_" + i + ".png")));
            }
            for (int i = 0; i < 4; i++) {
                hitFrames.add(new Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Hit_animation/Hit_" + i + ".png")));
            }
            for (int i = 0; i < 7; i++) {
                deathFrames.add(new Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Death_animation/Death_" + i + ".png")));
            }

            // need to extract later

            loadedEffects.put("epic_explosion", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Explosions/epic_explosion_001/epic_explosion_001_small_orange/frame%04d.png", 13));
            loadedEffects.put("directional_impact", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Impacts/directional_impact_001/directional_impact_001_small_blue/frame%04d.png", 7));
            loadedEffects.put("smoke_burst", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Smoke Bursts/symmetrical_smoke_burst_001/symmetrical_smoke_burst_001_small_brown/frame%04d.png", 10));
            loadedEffects.put("spell_poison", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Fantasy Spells/spell_poison_001/spell_poison_001_small_green/frame%04d.png", 17));
            loadedEffects.put("burst_splatter", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Splatters/burst_splatter_001/burst_splatter_001_small_red/frame%04d.png", 10));

            spellEffectBindings.put("Fireball", "epic_explosion");
            spellEffectBindings.put("Ice Shard", "directional_impact");
            spellEffectBindings.put("Tornado", "smoke_burst");
            spellEffectBindings.put("Poison", "spell_poison");
            spellEffectBindings.put("Water Beam", "burst_splatter");
            spellEffectBindings.put("Air Slash", "directional_impact");

            String[] colors = {"Blue", "Green", "Orange", "Purple", "Red"};
            for (int i = 0; i < colors.length; i++) {
                String color = colors[i];
                List<Image> fFrames = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    fFrames.add(new Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/Fireballs/Fireball" + color + "0" + j + ".png")));
                }
                fireballFramesList.add(fFrames);
            }
            
            spellColors.put("Fireball", 4); // Red
            spellColors.put("Ice Shard", 0); // Blue
            spellColors.put("Tornado", 3); // Purple
            spellColors.put("Poison", 1); // Green
            spellColors.put("Water Beam", 0); // Blue
            spellColors.put("Air Slash", 2); // Orange
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Image> loadFrames(String pathFormat, int count) {
        List<Image> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new Image(getClass().getResourceAsStream(String.format(pathFormat, i))));
        }
        return list;
    }

    public List<Image> getIdleFrames() { return idleFrames; }
    public List<Image> getAttackFrames() { return attackFrames; }
    public List<Image> getAttack2Frames() { return attack2Frames; }
    public List<Image> getHitFrames() { return hitFrames; }
    public List<Image> getDeathFrames() { return deathFrames; }

    public List<Image> getEffectFrames(String spellId) {
        String effectName = spellEffectBindings.get(spellId);
        if (effectName == null) {
            effectName = "epic_explosion"; // Domyślny efekt
        }
        List<Image> frames = loadedEffects.get(effectName);
        if (frames == null || frames.isEmpty()) {
            return loadedEffects.get("epic_explosion");
        }
        return frames;
    }

    public List<Image> getProjectileFrames(String spellId) {
        Integer colorIndex = spellColors.get(spellId);
        if (colorIndex == null || colorIndex < 0 || colorIndex >= fireballFramesList.size()) {
            colorIndex = Math.abs(spellId.hashCode()) % fireballFramesList.size();
        }
        return fireballFramesList.get(colorIndex);
    }
}
