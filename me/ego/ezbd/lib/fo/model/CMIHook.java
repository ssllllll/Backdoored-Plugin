package me.ego.ezbd.lib.fo.model;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Modules.TabList.TabListManager;
import java.util.Iterator;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class CMIHook {
    CMIHook() {
    }

    boolean isVanished(Player player) {
        CMIUser user = this.getUser(player);
        return user != null && user.isVanished();
    }

    boolean isAfk(Player player) {
        CMIUser user = this.getUser(player);
        return user != null && user.isAfk();
    }

    boolean isMuted(Player player) {
        CMIUser user = this.getUser(player);

        try {
            return user != null && user.getMutedUntil() != 0L && user.getMutedUntil() != null && user.getMutedUntil() > System.currentTimeMillis();
        } catch (Exception var4) {
            return false;
        }
    }

    void setGodMode(Player player, boolean godMode) {
        CMIUser user = this.getUser(player);
        user.setGod(godMode);
    }

    void setLastTeleportLocation(Player player, Location location) {
        CMIUser user = this.getUser(player);

        try {
            user.getClass().getMethod("setLastTeleportLocation", Location.class).invoke(user, location);
        } catch (Throwable var5) {
        }

    }

    void setIgnore(UUID player, UUID who, boolean ignore) {
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
        if (ignore) {
            user.addIgnore(who, true);
        } else {
            user.removeIgnore(who);
        }

    }

    boolean isIgnoring(UUID player, UUID who) {
        try {
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            return user.isIgnoring(who);
        } catch (NullPointerException var4) {
            return false;
        }
    }

    String getNick(Player player) {
        CMIUser user = this.getUser(player);
        String nick = user == null ? null : user.getNickName();
        return nick != null && !"".equals(nick) ? nick : null;
    }

    void setNick(UUID uniqueId, String nick) {
        CMIUser user = this.getUser(uniqueId);
        TabListManager tabManager = CMI.getInstance().getTabListManager();
        if (user != null) {
            user.setNickName(Common.colorize(nick), true);
            user.updateDisplayName();
            if (tabManager.isUpdatesOnNickChange()) {
                tabManager.updateTabList(3);
            }
        }

    }

    String getNameFromNick(String nick) {
        Iterator var2 = CMI.getInstance().getPlayerManager().getAllUsers().values().iterator();

        CMIUser user;
        do {
            if (!var2.hasNext()) {
                return nick;
            }

            user = (CMIUser)var2.next();
        } while(user == null || user.getNickName() == null || !Valid.colorlessEquals(user.getNickName(), nick));

        return (String)Common.getOrDefault(user.getName(), nick);
    }

    private CMIUser getUser(Player player) {
        return CMI.getInstance().getPlayerManager().getUser(player);
    }

    private CMIUser getUser(UUID uniqueId) {
        return CMI.getInstance().getPlayerManager().getUser(uniqueId);
    }
}