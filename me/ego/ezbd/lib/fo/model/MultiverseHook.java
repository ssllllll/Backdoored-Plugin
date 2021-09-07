package me.ego.ezbd.lib.fo.model;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;

class MultiverseHook {
    private final MultiverseCore multiVerse = (MultiverseCore)Bukkit.getPluginManager().getPlugin("Multiverse-Core");

    MultiverseHook() {
    }

    String getWorldAlias(String world) {
        MultiverseWorld mvWorld = this.multiVerse.getMVWorldManager().getMVWorld(world);
        return mvWorld != null ? mvWorld.getColoredWorldString() : world;
    }
}