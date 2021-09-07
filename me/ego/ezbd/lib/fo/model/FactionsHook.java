package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Location;
import org.bukkit.entity.Player;

abstract class FactionsHook {
    FactionsHook() {
    }

    abstract Collection<String> getFactions();

    abstract String getFaction(Player var1);

    abstract String getFaction(Location var1);

    abstract String getFactionOwner(Location var1);

    final Collection<? extends Player> getSameFactionPlayers(Player pl) {
        List<Player> recipients = new ArrayList();
        String playerFaction = this.getFaction(pl);
        if (playerFaction != null && !playerFaction.isEmpty()) {
            Iterator var4 = Remain.getOnlinePlayers().iterator();

            while(var4.hasNext()) {
                Player online = (Player)var4.next();
                String onlineFaction = this.getFaction(online);
                if (playerFaction.equals(onlineFaction)) {
                    recipients.add(online);
                }
            }
        }

        return recipients;
    }
}