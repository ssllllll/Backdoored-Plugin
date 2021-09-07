package me.ego.ezbd.lib.fo.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

class PlotSquaredHook {
    private final boolean legacy;

    PlotSquaredHook() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        Valid.checkNotNull(plugin, "PlotSquared not hooked yet!");
        this.legacy = plugin.getDescription().getVersion().startsWith("3");
    }

    List<Player> getPlotPlayers(Player player) {
        List<Player> players = new ArrayList();
        Class plotPlayerClass = ReflectionUtil.lookupClass((this.legacy ? "com.intellectualcrafters.plot.object" : "com.plotsquared.core.player") + ".PlotPlayer");

        Method wrap;
        try {
            wrap = plotPlayerClass.getMethod("wrap", Player.class);
        } catch (ReflectiveOperationException var12) {
            try {
                wrap = plotPlayerClass.getMethod("wrap", Object.class);
            } catch (ReflectiveOperationException var11) {
                throw new NullPointerException("PlotSquared could not convert " + player.getName() + " into PlotPlayer! Is the integration outdated?");
            }
        }

        Object plotPlayer = ReflectionUtil.invokeStatic(wrap, new Object[]{player});
        Valid.checkNotNull(plotPlayer, "Failed to convert player " + player.getName() + " to PlotPlayer!");
        Object currentPlot = ReflectionUtil.invoke("getCurrentPlot", plotPlayer, new Object[0]);
        if (currentPlot != null) {
            Iterator var7 = ((Iterable)ReflectionUtil.invoke("getPlayersInPlot", currentPlot, new Object[0])).iterator();

            while(var7.hasNext()) {
                Object playerInPlot = var7.next();
                UUID id = (UUID)ReflectionUtil.invoke("getUUID", playerInPlot, new Object[0]);
                Player online = Bukkit.getPlayer(id);
                if (online != null && online.isOnline()) {
                    players.add(online);
                }
            }
        }

        return players;
    }
}