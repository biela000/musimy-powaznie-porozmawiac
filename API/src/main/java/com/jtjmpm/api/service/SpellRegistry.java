package com.jtjmpm.api.service;


import com.jtjmpm.api.model.spell.Element;
import com.jtjmpm.api.model.spell.Spell;

import com.jtjmpm.api.model.spell.effect.*;
import com.jtjmpm.api.model.status.*;
import com.jtjmpm.messages.SpellDTO;
import com.jtjmpm.messages.SpellType;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpellRegistry {

    private final Map<String, Spell> spells = new ConcurrentHashMap<>();

    @PostConstruct
    public void initSpells() {
        //TODO
        //export to json
        //make this cleaner with the dmg and mana maybe
        addSpell(new Spell("Fireball", "fireball", SpellType.OFFENSE, Element.FIRE,
                40, 60, 1800,
                new FlatDamageEffect(40, 1800)));
        addSpell(new Spell("Ice Shard", "ice shard", SpellType.OFFENSE, Element.WATER,
                25, 15, 1500,
                new FlatDamageEffect(25, 1500)));
        addSpell(new Spell("Air Slash", "air slash", SpellType.OFFENSE, Element.AIR,
                20, 10, 1500,
                new FlatDamageEffect(20, 1500)));
        addSpell(new Spell("Water Beam", "water beam", SpellType.OFFENSE, Element.WATER,
                15, 5, 1600,
                new FlatDamageEffect(15, 1600)));
        addSpell(new Spell("Tornado", "tornado", SpellType.OFFENSE, Element.AIR,
                30, 25, 2200,
                new FlatDamageEffect(30, 2200)));
        addSpell(new Spell("Poison", "poison", SpellType.SUPPORT, Element.NATURE,
                0, 25, 1500,
                new ApplyStatusSpellEffect(1500, false,
                        acc -> new PoisonEffect(5, 1, 5))));
        addSpell(new Spell("Gamble", "gamble", SpellType.OFFENSE,Element.DARK,  80, 100, 1000,
                new GambleSpellEffect(80, 1000)));

        addSpell(new Spell("Heal", "heal", SpellType.SUPPORT,Element.NATURE , 40, 30, 1000,
                new HealSpellEffect(40, 1000)));

        addSpell(new Spell("Healing Aura", "healing aura", SpellType.SUPPORT, Element.NATURE, 0, 50, 1000,
                new ApplyStatusSpellEffect(1000, true,
                        acc -> new HealingAuraEffect(10, 1, 10, acc))));

        addSpell(new Spell("Weakness", "weakness", SpellType.SUPPORT, Element.ARCANE, 0, 30, 1600,
                new ApplyStatusSpellEffect(1600, false,
                        acc -> new WeaknessEffect(10, acc))));

        addSpell(new Spell("Shield", "shield", SpellType.SUPPORT, Element.ARCANE ,0, 20, 1000,
                new ApplyStatusSpellEffect(1000, true,
                        ShieldEffect::new)));

        addSpell(new Spell("Damage Up", "damage up", SpellType.SUPPORT, Element.ARCANE , 0, 25, 1000,
                new ApplyStatusSpellEffect(1000, true,
                        DamageUpEffect::new)));

        addSpell(new Spell("Vampirism", "vampirism", SpellType.SUPPORT,Element.DARK, 0, 45, 1000,
                new ApplyStatusSpellEffect(1000, true,
                        acc -> new VampirismEffect(15, acc))));

        addSpell(new Spell("Blindness", "blindness", SpellType.SUPPORT, Element.DARK, 0, 35, 1700,
                new ApplyStatusSpellEffect(1700, false,
                        acc -> new BlindnessEffect(5))));
        addSpell(new Spell("Mana Steal", "mana steal", SpellType.OFFENSE, Element.DARK, 0, 20, 1600,
                new ManaStealSpellEffect(60, 1600)));

        addSpell(new Spell("Thorns", "thorns", SpellType.SUPPORT, Element.NATURE, 0, 35, 1000,
                new ApplyStatusSpellEffect(1000, true,
                        acc -> new ThornsEffect(0.4 * acc, 10))));
        addSpell(new Spell("Dark Magic", "dark magic", SpellType.OFFENSE, Element.DARK,
                0, 25, 2500,
                new DarkMagicSpellEffect(3.0, 2500)));

        // NEW SPELLS
        addSpell(new Spell("Lightning Bolt", "lightning bolt", SpellType.OFFENSE, Element.AIR,
                35, 20, 1500,
                new FlatDamageEffect(35, 1500)));
        addSpell(new Spell("Laser Beam", "laser beam", SpellType.OFFENSE, Element.ARCANE,
                50, 45, 1500,
                new FlatDamageEffect(50, 1500)));
        addSpell(new Spell("Meteor", "meteor", SpellType.OFFENSE, Element.FIRE,
                100, 80, 7000,
                new FlatDamageEffect(100, 7000)));
        addSpell(new Spell("Toxic Dart", "toxic dart", SpellType.OFFENSE, Element.NATURE,
                25, 5, 1500,
                new FlatDamageEffect(25, 1500)));
        System.out.println("Loaded " + spells.size() + " spells to the registry");
    }

    private void addSpell(Spell spell) {
        spells.put(spell.name(), spell);
    }

    public Spell getSpell(String name) {
        return spells.get(name);
    }

    public List<SpellDTO> getAllSpellsAsDTO() {
        return spells.values().stream()
                .map(Spell::toDTO)
                .toList();
    }
}