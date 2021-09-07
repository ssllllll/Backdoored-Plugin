package me.ego.ezbd.lib.fo;

import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.Bukkit;

public final class MinecraftVersion {
    private static String serverVersion;
    private static MinecraftVersion.V current;

    public MinecraftVersion() {
    }

    public static boolean equals(MinecraftVersion.V version) {
        return compareWith(version) == 0;
    }

    public static boolean olderThan(MinecraftVersion.V version) {
        return compareWith(version) < 0;
    }

    public static boolean newerThan(MinecraftVersion.V version) {
        return compareWith(version) > 0;
    }

    public static boolean atLeast(MinecraftVersion.V version) {
        return equals(version) || newerThan(version);
    }

    private static int compareWith(MinecraftVersion.V version) {
        try {
            return getCurrent().minorVersionNumber - version.minorVersionNumber;
        } catch (Throwable var2) {
            var2.printStackTrace();
            return 0;
        }
    }

    public static String getServerVersion() {
        return serverVersion.equals("craftbukkit") ? "" : serverVersion;
    }

    public static MinecraftVersion.V getCurrent() {
        return current;
    }

    static {
        try {
            String packageName = Bukkit.getServer() == null ? "" : Bukkit.getServer().getClass().getPackage().getName();
            String curr = packageName.substring(packageName.lastIndexOf(46) + 1);
            boolean hasGatekeeper = !"craftbukkit".equals(curr) && !"".equals(packageName);
            serverVersion = curr;
            if (hasGatekeeper) {
                int pos = 0;
                char[] var4 = curr.toCharArray();
                int found = var4.length;

                for(int var6 = 0; var6 < found; ++var6) {
                    char ch = var4[var6];
                    ++pos;
                    if (pos > 2 && ch == 'R') {
                        break;
                    }
                }

                String numericVersion = curr.substring(1, pos - 2).replace("_", ".");
                found = 0;
                char[] var12 = numericVersion.toCharArray();
                int var13 = var12.length;

                for(int var8 = 0; var8 < var13; ++var8) {
                    char ch = var12[var8];
                    if (ch == '.') {
                        ++found;
                    }
                }

                Valid.checkBoolean(found == 1, "Minecraft Version checker malfunction. Could not detect your server version. Detected: " + numericVersion + " Current: " + curr, new Object[0]);
                current = MinecraftVersion.V.parse(Integer.parseInt(numericVersion.split("\\.")[1]));
            } else {
                current = MinecraftVersion.V.v1_3_AND_BELOW;
            }
        } catch (Throwable var10) {
            Common.error(var10, new String[]{"Error detecting your Minecraft version. Check your server compatibility."});
        }

    }

    public static enum V {
        v1_17(17, false),
        v1_16(16),
        v1_15(15),
        v1_14(14),
        v1_13(13),
        v1_12(12),
        v1_11(11),
        v1_10(10),
        v1_9(9),
        v1_8(8),
        v1_7(7),
        v1_6(6),
        v1_5(5),
        v1_4(4),
        v1_3_AND_BELOW(3);

        private final int minorVersionNumber;
        private final boolean tested;

        private V(int version) {
            this(version, true);
        }

        private V(int version, boolean tested) {
            this.minorVersionNumber = version;
            this.tested = tested;
        }

        protected static MinecraftVersion.V parse(int number) {
            MinecraftVersion.V[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                MinecraftVersion.V v = var1[var3];
                if (v.minorVersionNumber == number) {
                    return v;
                }
            }

            throw new FoException("Invalid version number: " + number);
        }

        public String toString() {
            return "1." + this.minorVersionNumber;
        }

        public boolean isTested() {
            return this.tested;
        }
    }
}