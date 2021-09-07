package me.ego.ezbd.lib.fo.remain;

import me.ego.ezbd.lib.fo.Common;

public enum CompBarStyle {
    SOLID("SOLID", "SOLID"),
    SEGMENTED_6("SEGMENTED_6", "SEG6"),
    SEGMENTED_10("SEGMENTED_10", "SEG10"),
    SEGMENTED_12("SEGMENTED_12", "SEG12"),
    SEGMENTED_20("SEGMENTED_20", "SEG20");

    private final String key;
    private final String shortKey;

    public static CompBarStyle fromKey(String key) {
        CompBarStyle[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            CompBarStyle mode = var1[var3];
            if (mode.key.equalsIgnoreCase(key) || mode.shortKey.equalsIgnoreCase(key)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("No such CompBarStyle: " + key + ". Available: " + Common.join(values()));
    }

    public String toString() {
        return this.key;
    }

    private CompBarStyle(String key, String shortKey) {
        this.key = key;
        this.shortKey = shortKey;
    }

    public String getKey() {
        return this.key;
    }

    public String getShortKey() {
        return this.shortKey;
    }
}