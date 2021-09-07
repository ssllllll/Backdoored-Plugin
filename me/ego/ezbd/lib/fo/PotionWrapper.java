package me.ego.ezbd.lib.fo;

import org.apache.commons.lang.WordUtils;

enum PotionWrapper {
    SLOW("SLOW", "Slowness"),
    STRENGTH("INCREASE_DAMAGE"),
    JUMP_BOOST("JUMP"),
    INSTANT_HEAL("INSTANT_HEALTH"),
    REGEN("REGENERATION");

    private final String bukkitName;
    private final String minecraftName;

    private PotionWrapper(String bukkitName) {
        this(bukkitName, (String)null);
    }

    protected static String getLocalizedName(String name) {
        String localizedName = name;
        PotionWrapper[] var2 = values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            PotionWrapper e = var2[var4];
            if (name.toUpperCase().replace(" ", "_").equals(e.bukkitName)) {
                localizedName = e.getMinecraftName();
                break;
            }
        }

        return WordUtils.capitalizeFully(localizedName.replace("_", " "));
    }

    protected static String getBukkitName(String name) {
        name = name.toUpperCase().replace(" ", "_");
        PotionWrapper[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            PotionWrapper e = var1[var3];
            if (e.toString().equalsIgnoreCase(name) || e.minecraftName != null && e.minecraftName.equalsIgnoreCase(name)) {
                return e.bukkitName;
            }
        }

        return name;
    }

    public String getMinecraftName() {
        return (String)Common.getOrDefault(this.minecraftName, this.bukkitName);
    }

    private PotionWrapper(String bukkitName, String minecraftName) {
        this.bukkitName = bukkitName;
        this.minecraftName = minecraftName;
    }
}