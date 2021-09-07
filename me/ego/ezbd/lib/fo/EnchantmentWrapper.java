package me.ego.ezbd.lib.fo;

import org.apache.commons.lang.WordUtils;

enum EnchantmentWrapper {
    PROTECTION("PROTECTION_ENVIRONMENTAL"),
    FIRE_PROTECTION("PROTECTION_FIRE"),
    FEATHER_FALLING("PROTECTION_FALL"),
    BLAST_PROTECTION("PROTECTION_EXPLOSIONS"),
    PROJECTILE_PROTECTION("PROTECTION_PROJECTILE"),
    RESPIRATION("OXYGEN"),
    AQUA_AFFINITY("WATER_WORKER"),
    THORN("THORNS"),
    CURSE_OF_VANISHING("VANISHING_CURSE"),
    CURSE_OF_BINDING("BINDING_CURSE"),
    SHARPNESS("DAMAGE_ALL"),
    SMITE("DAMAGE_UNDEAD"),
    BANE_OF_ARTHROPODS("DAMAGE_ARTHROPODS"),
    LOOTING("LOOT_BONUS_MOBS"),
    SWEEPING_EDGE("SWEEPING"),
    EFFICIENCY("DIG_SPEED"),
    UNBREAKING("DURABILITY"),
    FORTUNE("LOOT_BONUS_BLOCKS"),
    POWER("ARROW_DAMAGE"),
    PUNCH("ARROW_KNOCKBACK"),
    FLAME("ARROW_FIRE"),
    INFINITY("ARROW_INFINITE"),
    LUCK_OF_THE_SEA("LUCK");

    private final String bukkitName;

    protected static String toBukkit(String name) {
        name = name.toUpperCase().replace(" ", "_");
        EnchantmentWrapper[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            EnchantmentWrapper e = var1[var3];
            if (e.toString().equals(name)) {
                return e.bukkitName;
            }
        }

        return name;
    }

    protected static String toMinecraft(String name) {
        name = name.toUpperCase().replace(" ", "_");
        EnchantmentWrapper[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            EnchantmentWrapper e = var1[var3];
            if (name.equals(e.bukkitName)) {
                return ItemUtil.bountifyCapitalized(e);
            }
        }

        return WordUtils.capitalizeFully(name);
    }

    public String getBukkitName() {
        return this.bukkitName != null ? this.bukkitName : this.name();
    }

    private EnchantmentWrapper(String bukkitName) {
        this.bukkitName = bukkitName;
    }
}