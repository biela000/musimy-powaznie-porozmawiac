package com.jtjmpm.desktop.utils;

import com.google.gson.Gson;
import javafx.scene.image.Image;

import java.io.InputStreamReader;
import java.io.Reader;
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

            // Load effects dynamically from JSON
            try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/effects_index.json"))) {
                Gson gson = new Gson();
                EffectIndexEntry[] entries = gson.fromJson(reader, EffectIndexEntry[].class);
                if (entries != null) {
                    for (EffectIndexEntry entry : entries) {
                        loadedEffects.put(entry.name(), loadFrames(entry.pathPattern(), entry.frameCount()));
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load effects_index.json: " + e.getMessage());
            }

            spellEffectBindings.put("Fireball", "epic_explosion_001_small_orange");
            spellEffectBindings.put("Ice Shard", "directional_impact_001_small_blue");
            spellEffectBindings.put("Tornado", "symmetrical_smoke_burst_001_small_brown");
            spellEffectBindings.put("Poison", "spell_poison_001_small_green");
            spellEffectBindings.put("Water Beam", "burst_splatter_001_small_red");
            spellEffectBindings.put("Air Slash", "directional_impact_001_small_blue");
            // New Spells Effects
            spellEffectBindings.put("Gamble", "scifi_spark_burst_001_small_yellow");
            spellEffectBindings.put("Heal", "spell_heal_001_small_red");
            spellEffectBindings.put("Healing Aura", "round_heart_burst_001_small_red");
            spellEffectBindings.put("Weakness", "stylized_skull_smoke_burst_001_small_white");
            spellEffectBindings.put("Shield", "spell_defense_up_001_small_blue");
            spellEffectBindings.put("Damage Up", "spell_attack_up_001_small_red");
            spellEffectBindings.put("Vampirism", "spell_absorb_001_small_violet");
            spellEffectBindings.put("Blindness", "symmetrical_smoke_burst_001_small_brown");
            spellEffectBindings.put("Mana Steal", "scifi_warp_003_small_blue");
            spellEffectBindings.put("Thorns", "spell_haste_001_small_green");
            spellEffectBindings.put("Dark Magic", "spell_death_001_small_red");
            spellEffectBindings.put("Lightning Bolt", "lightning_strike_001_small_violet");
            spellEffectBindings.put("Laser Beam", "scifi_warp_003_small_blue");
            spellEffectBindings.put("Meteor", "epic_explosion_001_small_orange");
            spellEffectBindings.put("Poison Dart", "spell_poison_001_small_green");

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
            // New Spells Fireball Colors
            spellColors.put("Gamble", 3); // Purple
            spellColors.put("Heal", 1); // Green
            spellColors.put("Healing Aura", 1); // Green
            spellColors.put("Weakness", 3); // Purple
            spellColors.put("Shield", 0); // Blue
            spellColors.put("Damage Up", 4); // Red
            spellColors.put("Vampirism", 4); // Red
            spellColors.put("Blindness", 3); // Purple
            spellColors.put("Mana Steal", 0); // Blue
            spellColors.put("Thorns", 1); // Green
            spellColors.put("Dark Magic", 3); // Purple
            spellColors.put("Lightning Bolt", 3); // Purple
            spellColors.put("Laser Beam", 0); // Blue
            spellColors.put("Meteor", 2); // Orange
            spellColors.put("Poison Dart", 1); // Green
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
            effectName = "epic_explosion_001_small_orange"; // default effect
        }
        List<Image> frames = loadedEffects.get(effectName);
        if (frames == null || frames.isEmpty()) {
            return loadedEffects.get("epic_explosion_001_small_orange");
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
