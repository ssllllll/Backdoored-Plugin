package me.ego.ezbd.lib.fo.model;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import java.util.Collection;
import me.ego.ezbd.lib.fo.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class FactionsMassive extends FactionsHook {
    FactionsMassive() {
    }

    public Collection<String> getFactions() {
        return Common.convert(FactionColl.get().getAll(), (object) -> {
            return Common.stripColors(object.getName());
        });
    }

    public String getFaction(Player pl) {
        try {
            return MPlayer.get(pl.getUniqueId()).getFactionName();
        } catch (Exception var3) {
            return null;
        }
    }

    public String getFaction(Location loc) {
        Faction f = BoardColl.get().getFactionAt(PS.valueOf(loc));
        return f != null ? f.getName() : null;
    }

    public String getFactionOwner(Location loc) {
        Faction f = BoardColl.get().getFactionAt(PS.valueOf(loc));
        if (f != null) {
            return f.getLeader() != null ? f.getLeader().getName() : null;
        } else {
            return null;
        }
    }
}