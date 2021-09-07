package me.ego.ezbd.lib.fo.model;

import org.bukkit.enchantments.Enchantment;

public final class SimpleEnchant {
    private final Enchantment enchant;
    private final int level;

    public SimpleEnchant(Enchantment enchant) {
        this(enchant, 1);
    }

    public Enchantment getEnchant() {
        return this.enchant;
    }

    public int getLevel() {
        return this.level;
    }

    public SimpleEnchant(Enchantment enchant, int level) {
        this.enchant = enchant;
        this.level = level;
    }
}