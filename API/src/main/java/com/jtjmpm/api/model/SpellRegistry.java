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