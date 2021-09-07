package me.ego.ezbd.lib.fo.model;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import java.util.Collection;
import java.util.Set;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class FactionsUUID extends FactionsHook {
    FactionsUUID() {
    }

    public Collection<String> getFactions() {
        try {
            Object i = this.instance();
            Set<String> tags = (Set)i.getClass().getMethod("getFactionTags").invoke(i);
            return tags;
        } catch (Throwable var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public String getFaction(Player pl) {
        try {
            Object fplayers = this.fplayers();
            Object fpl = fplayers.getClass().getMethod("getByPlayer", Player.class).invoke(fplayers, pl);
            Object f = fpl != null ? fpl.getClass().getMethod("getFaction").invoke(fpl) : null;
            Object name = f != null ? f.getClass().getMethod("getTag").invoke(f) : null;
            return name != null ? name.toString() : null;
        } catch (ReflectiveOperationException var6) {
            var6.printStackTrace();
            return null;
        }
    }

    public String getFaction(Location loc) {
        Object f = this.findFaction(loc);

        try {
            return f != null ? f.getClass().getMethod("getTag").invoke(f).toString() : null;
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public String getFactionOwner(Location loc) {
        Object faction = this.findFaction(loc);

        try {
            return faction != null ? ((FPlayer)faction.getClass().getMethod("getFPlayerAdmin").invoke(faction)).getName() : null;
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    private Object findFaction(Location loc) {
        Class b = Board.class;

        try {
            return b.getMethod("getFactionAt", FLocation.class).invoke(b.getMethod("getInstance").invoke((Object)null), new FLocation(loc));
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    private Object instance() {
        try {
            return Class.forName("com.massivecraft.factions.Factions").getDeclaredMethod("getInstance").invoke((Object)null);
        } catch (ReflectiveOperationException var2) {
            var2.printStackTrace();
            throw new FoException(var2);
        }
    }

    private Object fplayers() {
        try {
            return Class.forName("com.massivecraft.factions.FPlayers").getDeclaredMethod("getInstance").invoke((Object)null);
        } catch (ReflectiveOperationException var2) {
            var2.printStackTrace();
            throw new FoException(var2);
        }
    }
}