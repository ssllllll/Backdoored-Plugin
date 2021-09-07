package me.ego.ezbd.lib.fo.model;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.object.WorldCoord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class TownyHook {
    TownyHook() {
    }

    Collection<? extends Player> getTownResidentsOnline(Player pl) {
        List<Player> recipients = new ArrayList();
        String playersTown = this.getTownName(pl);
        if (!playersTown.isEmpty()) {
            Iterator var4 = Remain.getOnlinePlayers().iterator();

            while(var4.hasNext()) {
                Player online = (Player)var4.next();
                if (playersTown.equals(this.getTownName(online))) {
                    recipients.add(online);
                }
            }
        }

        return recipients;
    }

    Collection<? extends Player> getNationPlayersOnline(Player pl) {
        List<Player> recipients = new ArrayList();
        String playerNation = this.getNationName(pl);
        if (!playerNation.isEmpty()) {
            Iterator var4 = Remain.getOnlinePlayers().iterator();

            while(var4.hasNext()) {
                Player online = (Player)var4.next();
                if (playerNation.equals(this.getNationName(online))) {
                    recipients.add(online);
                }
            }
        }

        return recipients;
    }

    String getTownName(Player pl) {
        Town t = this.getTown(pl);
        return t != null ? t.getName() : "";
    }

    String getNationName(Player pl) {
        Nation n = this.getNation(pl);
        return n != null ? n.getName() : "";
    }

    List<String> getTowns() {
        try {
            return Common.convert(TownyUniverse.getInstance().getTowns(), TownyObject::getName);
        } catch (Throwable var2) {
            return new ArrayList();
        }
    }

    String getTownName(Location loc) {
        Town town = this.getTown(loc);
        return town != null ? town.getName() : null;
    }

    private Town getTown(Location loc) {
        try {
            WorldCoord worldCoord = WorldCoord.parseWorldCoord(loc);
            TownBlock townBlock = TownyUniverse.getInstance().getTownBlock(worldCoord);
            return townBlock != null ? townBlock.getTown() : null;
        } catch (Throwable var4) {
            return null;
        }
    }

    String getTownOwner(Location loc) {
        try {
            Town town = this.getTown(loc);
            return town != null ? town.getMayor().getName() : null;
        } catch (Throwable var3) {
            return null;
        }
    }

    private Nation getNation(Player pl) {
        Town town = this.getTown(pl);

        try {
            return town.getNation();
        } catch (Throwable var4) {
            return null;
        }
    }

    private Town getTown(Player pl) {
        Resident res = this.getResident(pl);

        try {
            return res.getTown();
        } catch (Throwable var4) {
            return null;
        }
    }

    private Resident getResident(Player player) {
        try {
            return TownyUniverse.getInstance().getResident(player.getName());
        } catch (Throwable var3) {
            return null;
        }
    }
}