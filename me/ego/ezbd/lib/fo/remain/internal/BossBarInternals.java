package me.ego.ezbd.lib.fo.remain.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompBarColor;
import me.ego.ezbd.lib.fo.remain.CompBarStyle;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;

/** @deprecated */
@Deprecated
public class BossBarInternals implements Listener {
    private static Class<?> entityClass;
    private static boolean isBelowGround = true;
    private static HashMap<UUID, EnderDragonEntity> players = new HashMap();
    private static HashMap<UUID, Integer> timers = new HashMap();
    private static BossBarInternals singleton = null;

    private BossBarInternals() {
    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    public void onPluginDisable(PluginDisableEvent e) {
        if (!MinecraftVersion.olderThan(V.v1_6) && e.getPlugin().equals(SimplePlugin.getInstance()) && singleton != null) {
            singleton.stop();
        }

    }

    private void stop() {
        if (!MinecraftVersion.olderThan(V.v1_6)) {
            Iterator var1 = Remain.getOnlinePlayers().iterator();

            while(var1.hasNext()) {
                Player player = (Player)var1.next();
                removeBar(player);
            }

            players.clear();
            var1 = timers.values().iterator();

            while(var1.hasNext()) {
                int timerID = (Integer)var1.next();
                Bukkit.getScheduler().cancelTask(timerID);
            }

            timers.clear();
        }

    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeBar(event.getPlayer());
    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    public void onPlayerKick(PlayerKickEvent event) {
        removeBar(event.getPlayer());
    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.handleTeleport(event.getPlayer(), event.getTo().clone());
    }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    public void onPlayerTeleport(PlayerRespawnEvent event) {
        this.handleTeleport(event.getPlayer(), event.getRespawnLocation().clone());
    }

    private void handleTeleport(Player player, Location loc) {
        if (!MinecraftVersion.olderThan(V.v1_6) && hasBar(player)) {
            EnderDragonEntity oldDragon = getDragon(player, "");
            if (!(oldDragon instanceof v1_9Native)) {
                Common.runLater(2, () -> {
                    if (hasBar(player)) {
                        float health = oldDragon.health;
                        String message = oldDragon.name;
                        Remain.sendPacket(player, getDragon(player, "").getDestroyPacket());
                        players.remove(player.getUniqueId());
                        EnderDragonEntity dragon = addDragon(player, loc, message);
                        dragon.health = health;
                        sendDragon(dragon, player);
                    }
                });
            }
        }
    }

    public static void setMessage(Player player, String message, float percent, CompBarColor color, CompBarStyle style) {
        Valid.checkBoolean(0.0F <= percent && percent <= 100.0F, "Percent must be between 0F and 100F, but was: " + percent, new Object[0]);
        if (!MinecraftVersion.olderThan(V.v1_6)) {
            if (hasBar(player)) {
                removeBar(player);
            }

            EnderDragonEntity dragon = getDragon(player, message);
            dragon.name = cleanMessage(message);
            dragon.health = percent / 100.0F * dragon.getMaxHealth();
            if (color != null) {
                dragon.barColor = color;
            }

            if (style != null) {
                dragon.barStyle = style;
            }

            cancelTimer(player);
            sendDragon(dragon, player);
        }
    }

    public static void setMessage(Player player, String message, int seconds, CompBarColor color, CompBarStyle style) {
        Valid.checkBoolean(seconds > 0, "Seconds must be > 1 ", new Object[0]);
        if (!MinecraftVersion.olderThan(V.v1_6)) {
            if (hasBar(player)) {
                removeBar(player);
            }

            EnderDragonEntity dragon = getDragon(player, message);
            dragon.name = cleanMessage(message);
            dragon.health = dragon.getMaxHealth();
            if (color != null) {
                dragon.barColor = color;
            }

            if (style != null) {
                dragon.barStyle = style;
            }

            float dragonHealthMinus = dragon.getMaxHealth() / (float)seconds;
            cancelTimer(player);
            timers.put(player.getUniqueId(), Common.runTimer(20, 20, () -> {
                EnderDragonEntity drag = getDragon(player, "");
                drag.health -= dragonHealthMinus;
                if (drag.health <= 1.0F) {
                    removeBar(player);
                    cancelTimer(player);
                } else {
                    sendDragon(drag, player);
                }

            }).getTaskId());
            sendDragon(dragon, player);
        }
    }

    public static void removeBar(Player player) {
        if (hasBar(player)) {
            if (!MinecraftVersion.olderThan(V.v1_6)) {
                EnderDragonEntity dragon = getDragon(player, "");
                if (dragon instanceof v1_9Native) {
                    ((v1_9Native)dragon).removePlayer(player);
                } else {
                    Remain.sendPacket(player, getDragon(player, "").getDestroyPacket());
                }

                players.remove(player.getUniqueId());
                cancelTimer(player);
            }
        }
    }

    private static boolean hasBar(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    private static String cleanMessage(String message) {
        if (message.length() > 64) {
            message = message.substring(0, 63);
        }

        return message;
    }

    private static void cancelTimer(Player player) {
        Integer timerID = (Integer)timers.remove(player.getUniqueId());
        if (timerID != null) {
            Bukkit.getScheduler().cancelTask(timerID);
        }

    }

    private static void sendDragon(EnderDragonEntity dragon, Player player) {
        if (dragon instanceof v1_9Native) {
            v1_9Native bar = (v1_9Native)dragon;
            bar.addPlayer(player);
            bar.setProgress((double)(dragon.health / dragon.getMaxHealth()));
        } else {
            Remain.sendPacket(player, dragon.getMetaPacket(dragon.getWatcher()));
            Remain.sendPacket(player, dragon.getTeleportPacket(getDragonLocation(player.getLocation())));
        }

    }

    private static EnderDragonEntity getDragon(Player player, String message) {
        return hasBar(player) ? (EnderDragonEntity)players.get(player.getUniqueId()) : addDragon(player, cleanMessage(message));
    }

    private static EnderDragonEntity addDragon(Player player, String message) {
        EnderDragonEntity dragon = newDragon(message, getDragonLocation(player.getLocation()));
        if (dragon instanceof v1_9Native) {
            ((v1_9Native)dragon).addPlayer(player);
        } else {
            Remain.sendPacket(player, dragon.getSpawnPacket());
        }

        players.put(player.getUniqueId(), dragon);
        return dragon;
    }

    private static EnderDragonEntity addDragon(Player player, Location loc, String message) {
        EnderDragonEntity dragon = newDragon(message, getDragonLocation(loc));
        if (dragon instanceof v1_9Native) {
            ((v1_9Native)dragon).addPlayer(player);
        } else {
            Remain.sendPacket(player, dragon.getSpawnPacket());
        }

        players.put(player.getUniqueId(), dragon);
        return dragon;
    }

    private static Location getDragonLocation(Location loc) {
        if (isBelowGround) {
            loc.subtract(0.0D, 300.0D, 0.0D);
            return loc;
        } else {
            float pitch = loc.getPitch();
            if (pitch >= 55.0F) {
                loc.add(0.0D, -300.0D, 0.0D);
            } else if (pitch <= -55.0F) {
                loc.add(0.0D, 300.0D, 0.0D);
            } else {
                loc = loc.getBlock().getRelative(getDirection(loc), Bukkit.getViewDistance() * 16).getLocation();
            }

            return loc;
        }
    }

    private static BlockFace getDirection(Location loc) {
        float dir = (float)Math.round(loc.getYaw() / 90.0F);
        if (dir != -4.0F && dir != 0.0F && dir != 4.0F) {
            if (dir != -1.0F && dir != 3.0F) {
                if (dir != -2.0F && dir != 2.0F) {
                    return dir != -3.0F && dir != 1.0F ? null : BlockFace.WEST;
                } else {
                    return BlockFace.NORTH;
                }
            } else {
                return BlockFace.EAST;
            }
        } else {
            return BlockFace.SOUTH;
        }
    }

    private static EnderDragonEntity newDragon(String message, Location loc) {
        EnderDragonEntity fakeDragon = null;

        try {
            fakeDragon = (EnderDragonEntity)entityClass.getConstructor(String.class, Location.class).newInstance(message, loc);
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
        }

        return fakeDragon;
    }

    public static void callStatic() {
    }

    static {
        if (Remain.isProtocol18Hack()) {
            entityClass = v1_8Hack.class;
            isBelowGround = false;
        } else if (MinecraftVersion.equals(V.v1_6)) {
            entityClass = v1_6.class;
        } else if (MinecraftVersion.equals(V.v1_7)) {
            entityClass = v1_7.class;
        } else if (MinecraftVersion.equals(V.v1_8)) {
            entityClass = v1_8.class;
            isBelowGround = false;
        } else if (MinecraftVersion.newerThan(V.v1_8)) {
            entityClass = v1_9Native.class;
        }

        if (!MinecraftVersion.olderThan(V.v1_6)) {
            Valid.checkNotNull(entityClass, "Compatible does not support Boss bar on MC version " + MinecraftVersion.getServerVersion() + "!");
            if (singleton == null && SimplePlugin.getInstance().isEnabled()) {
                singleton = new BossBarInternals();
                Bukkit.getPluginManager().registerEvents(singleton, SimplePlugin.getInstance());
                if (Remain.isProtocol18Hack()) {
                    Common.runTimer(5, () -> {
                        Iterator var0 = players.keySet().iterator();

                        while(var0.hasNext()) {
                            UUID uuid = (UUID)var0.next();
                            Player player = Remain.getPlayerByUUID(uuid);
                            Remain.sendPacket(player, ((EnderDragonEntity)players.get(uuid)).getTeleportPacket(getDragonLocation(player.getLocation())));
                        }

                    });
                }
            }
        }

    }
}