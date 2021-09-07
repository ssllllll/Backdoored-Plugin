package me.ego.ezbd.lib.fo.remain.nbt;

import me.ego.ezbd.lib.fo.Common;
import org.bukkit.Bukkit;

enum WrapperVersion {
    UNKNOWN(2147483647),
    MC1_7_R4(174),
    MC1_8_R3(183),
    MC1_9_R1(191),
    MC1_9_R2(192),
    MC1_10_R1(1101),
    MC1_11_R1(1111),
    MC1_12_R1(1121),
    MC1_13_R1(1131),
    MC1_13_R2(1132),
    MC1_14_R1(1141),
    MC1_15_R1(1151),
    MC1_16_R1(1161),
    MC1_16_R2(1162),
    MC1_16_R3(1163);

    private static WrapperVersion version;
    private final int versionId;

    private WrapperVersion(int versionId) {
        this.versionId = versionId;
    }

    public int getVersionId() {
        return this.versionId;
    }

    public static boolean isAtLeastVersion(WrapperVersion version) {
        return getVersion().getVersionId() >= version.getVersionId();
    }

    public static boolean isNewerThan(WrapperVersion version) {
        return getVersion().getVersionId() > version.getVersionId();
    }

    public static WrapperVersion getVersion() {
        if (version != null) {
            return version;
        } else {
            String ver = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            try {
                version = valueOf(ver.replace("v", "MC"));
            } catch (IllegalArgumentException var2) {
                version = UNKNOWN;
            }

            if (version == UNKNOWN) {
                Common.log(new String[]{"[NBTAPI] Wasn't able to find NMS Support! Some functions may not work!"});
            }

            return version;
        }
    }
}