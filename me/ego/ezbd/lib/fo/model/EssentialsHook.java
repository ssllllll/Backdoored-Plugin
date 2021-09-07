package me.ego.ezbd.lib.fo.model;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class EssentialsHook {
    private final Essentials ess = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");

    EssentialsHook() {
    }

    void setGodMode(Player player, boolean godMode) {
        User user = this.getUser(player.getName());
        if (user != null) {
            user.setGodModeEnabled(godMode);
        }

    }

    void setIgnore(UUID player, UUID toIgnore, boolean ignore) {
        try {
            User user = this.ess.getUser(player);
            User toIgnoreUser = this.ess.getUser(toIgnore);
            if (toIgnoreUser != null) {
                user.setIgnoredPlayer(toIgnoreUser, ignore);
            }
        } catch (Throwable var6) {
        }

    }

    boolean isIgnoring(UUID player, UUID ignoringPlayer) {
        try {
            User user = this.ess.getUser(player);
            User ignored = this.ess.getUser(ignoringPlayer);
            return user != null && ignored != null && user.isIgnoredPlayer(ignored);
        } catch (Throwable var5) {
            return false;
        }
    }

    boolean isAfk(String pl) {
        IUser user = this.getUser(pl);
        return user != null ? user.isAfk() : false;
    }

    boolean isVanished(String pl) {
        IUser user = this.getUser(pl);
        return user != null ? user.isVanished() : false;
    }

    boolean isMuted(String pl) {
        User user = this.getUser(pl);
        return user != null ? user.isMuted() : false;
    }

    Player getReplyTo(String recipient) {
        User user = this.getUser(recipient);
        if (user == null) {
            return null;
        } else {
            String replyPlayer = null;

            try {
                replyPlayer = user.getReplyRecipient().getName();
            } catch (Throwable var8) {
                try {
                    Method getReplyTo = ReflectionUtil.getMethod(user.getClass(), "getReplyTo");
                    if (getReplyTo != null) {
                        CommandSource commandSource = (CommandSource)ReflectionUtil.invoke(getReplyTo, user, new Object[0]);
                        replyPlayer = commandSource == null ? null : commandSource.getPlayer().getName();
                    }
                } catch (Throwable var7) {
                    replyPlayer = null;
                }
            }

            Player bukkitPlayer = replyPlayer == null ? null : Bukkit.getPlayer(replyPlayer);
            return bukkitPlayer != null && bukkitPlayer.isOnline() ? bukkitPlayer : null;
        }
    }

    String getNick(String player) {
        User user = this.getUser(player);
        if (user == null) {
            Common.log(new String[]{"&cMalfunction getting Essentials user. Have you reloaded?"});
            return player;
        } else {
            String essNick = Common.getOrEmpty(user.getNickname());
            return "".equals(essNick) ? null : essNick;
        }
    }

    void setNick(UUID uniqueId, String nick) {
        User user = this.getUser(uniqueId);
        if (user != null) {
            user.setNickname(Common.colorize(nick));
        }

    }

    String getNameFromNick(String maybeNick) {
        UserMap users = this.ess.getUserMap();
        if (users != null) {
            Iterator var3 = users.getAllUniqueUsers().iterator();

            while(var3.hasNext()) {
                UUID userId = (UUID)var3.next();
                User user = users.getUser(userId);
                if (user != null && user.getNickname() != null && Valid.colorlessEquals(user.getNickname(), maybeNick)) {
                    return (String)Common.getOrDefault(user.getName(), maybeNick);
                }
            }
        }

        return maybeNick;
    }

    void setBackLocation(String player, Location loc) {
        User user = this.getUser(player);
        if (user != null) {
            try {
                user.setLastLocation(loc);
            } catch (Throwable var5) {
            }
        }

    }

    private User getUser(String name) {
        if (this.ess.getUserMap() == null) {
            return null;
        } else {
            User user = null;

            try {
                user = this.ess.getUserMap().getUser(name);
            } catch (Throwable var5) {
            }

            if (user == null) {
                try {
                    user = this.ess.getUserMap().getUserFromBukkit(name);
                } catch (Throwable var4) {
                    user = this.ess.getUser(name);
                }
            }

            return user;
        }
    }

    private User getUser(UUID uniqueId) {
        if (this.ess.getUserMap() == null) {
            return null;
        } else {
            User user = null;

            try {
                user = this.ess.getUserMap().getUser(uniqueId);
            } catch (Throwable var5) {
            }

            if (user == null) {
                try {
                    user = this.ess.getUser(uniqueId);
                } catch (Throwable var4) {
                }
            }

            return user;
        }
    }
}
