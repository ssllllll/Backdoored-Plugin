package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Collection;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.entity.Player;

class LandsHook {
    private final LandsIntegration lands = new LandsIntegration(SimplePlugin.getInstance());

    LandsHook() {
    }

    Collection<Player> getLandPlayers(Player player) {
        Land land = this.lands.getLand(player.getLocation());
        return (Collection)(land != null ? land.getOnlinePlayers() : new ArrayList());
    }
}