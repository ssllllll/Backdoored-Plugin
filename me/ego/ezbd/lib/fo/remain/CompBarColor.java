package me.ego.ezbd.lib.fo.remain;

import me.ego.ezbd.lib.fo.Common;

public enum CompBarColor {
    PINK("PINK"),
    BLUE("BLUE"),
    RED("RED"),
    GREEN("GREEN"),
    YELLOW("YELLOW"),
    PURPLE("PURPLE"),
    WHITE("WHITE");

    private final String key;

    public static CompBarColor fromKey(String key) {
        CompBarColor[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            CompBarColor mode = var1[var3];
            if (mode.key.equalsIgnoreCase(key)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("No such CompBarColor: " + key + ". Available: " + Common.join(values()));
    }

    public String toString() {
        return this.key;
    }

    private CompBarColor(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}