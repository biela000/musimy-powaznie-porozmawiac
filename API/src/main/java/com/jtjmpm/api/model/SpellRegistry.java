package com.jtjmpm.api.model;

import com.jtjmpm.messages.SpellDTO;
import com.jtjmpm.messages.SpellType;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
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
        addSpell(new Spell("Fireball", "fireball", SpellType.OFFENSE,
                40, 60, 1000,
                new FlatDamageEffect(40, 60, 1000)));
        addSpell(new Spell("Ice Shard", "ice shard", SpellType.OFFENSE,
                25, 15, 1000,
                new FlatDamageEffect(25, 15, 1000)));
        addSpell(new Spell("Air Slash", "air slash", SpellType.OFFENSE,
                20, 10, 1000,
                new FlatDamageEffect(20, 10, 1000)));
        addSpell(new Spell("Water Beam", "water beam", SpellType.OFFENSE,
                15, 5, 1000,
                new FlatDamageEffect(15, 5, 1000)));
        addSpell(new Spell("Tornado", "tornado", SpellType.OFFENSE,
                30, 25, 1000,
                new FlatDamageEffect(30, 25, 1000)));
        addSpell(new Spell("Poison", "poison", SpellType.SUPPORT,
                0, 25, 1000,
                new ApplyStatusSpellEffect(25, 5,
                        () -> new PoisonEffect(5, 1, 5))));
        addSpell(new Spell("Gamble", "gamble", SpellType.OFFENSE, 80, 40, 1000,
                new GambleSpellEffect(80, 40, 1000)));

        addSpell(new Spell("Heal", "heal", SpellType.SUPPORT, 40, 30, 1000,
                new HealSpellEffect(40, 30, 1000)));

        addSpell(new Spell("Healing Aura", "healing aura", SpellType.SUPPORT, 0, 50, 1000,
                new ApplyStatusSpellEffect(50, 1000, true,
                        acc -> new HealingAuraEffect(10, 1, 10, acc))));

        addSpell(new Spell("Weakness", "weakness", SpellType.SUPPORT, 0, 30, 1000,
                new ApplyStatusSpellEffect(30, 1000, false,
                        acc -> new WeaknessEffect(10, acc))));

        addSpell(new Spell("Shield", "shield", SpellType.SUPPORT, 0, 20, 1000,
                new ApplyStatusSpellEffect(20, 1000, true,
                        ShieldEffect::new)));

        addSpell(new Spell("Damage Up", "damage up", SpellType.SUPPORT, 0, 25, 1000,
                new ApplyStatusSpellEffect(25, 1000, true,
                        DamageUpEffect::new)));

        addSpell(new Spell("Vampirism", "vampirism", SpellType.SUPPORT, 0, 45, 1000,
                new ApplyStatusSpellEffect(45, 1000, true,
                        acc -> new VampirismEffect(15, acc))));

        addSpell(new Spell("Blindness", "blindness", SpellType.SUPPORT, 0, 35, 1000,
                new ApplyStatusSpellEffect(35, 1000, false,
                        acc -> new BlindnessEffect(5))));
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