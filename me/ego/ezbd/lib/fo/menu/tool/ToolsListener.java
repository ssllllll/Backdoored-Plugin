package me.ego.ezbd.lib.fo.menu.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.event.RocketExplosionEvent;
import me.ego.ezbd.lib.fo.remain.CompRunnable;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class ToolsListener implements Listener {
    private final Map<UUID, ToolsListener.ShotRocket> shotRockets = new HashMap();

    public ToolsListener() {
    }

    @EventHandler(
        priority = EventPriority.HIGH
    )
    public void onToolClick(PlayerInteractEvent event) {
        if (Remain.isInteractEventPrimaryHand(event)) {
            Player player = event.getPlayer();
            Tool tool = ToolRegistry.getTool(player.getItemInHand());
            if (tool != null) {
                try {
                    if ((event.isCancelled() || !event.hasBlock()) && tool.ignoreCancelled()) {
                        return;
                    }

                    if (tool instanceof Rocket) {
                        Rocket rocket = (Rocket)tool;
                        if (rocket.canLaunch(player, player.getEyeLocation())) {
                            player.launchProjectile(rocket.getProjectile(), player.getEyeLocation().getDirection().multiply(rocket.getFlightSpeed()));
                        } else {
                            event.setCancelled(true);
                        }
                    } else {
                        tool.onBlockClick(event);
                    }

                    if (tool.autoCancel()) {
                        event.setCancelled(true);
                    }
                } catch (Throwable var5) {
                    event.setCancelled(true);
                    Common.tell(player, new String[]{me.ego.ezbd.lib.fo.settings.SimpleLocalization.Tool.ERROR});
                    Common.error(var5, new String[]{"Failed to handle " + event.getAction() + " using tool: " + tool.getClass()});
                }
            }

        }
    }

    @EventHandler(
        priority = EventPriority.HIGH
    )
    public void onToolPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Tool tool = ToolRegistry.getTool(player.getItemInHand());
        if (tool != null) {
            try {
                if (event.isCancelled() && tool.ignoreCancelled()) {
                    return;
                }

                tool.onBlockPlace(event);
                if (tool.autoCancel()) {
                    event.setCancelled(true);
                }
            } catch (Throwable var5) {
                event.setCancelled(true);
                Common.tell(player, new String[]{me.ego.ezbd.lib.fo.settings.SimpleLocalization.Tool.ERROR});
                Common.error(var5, new String[]{"Failed to handle placing " + event.getBlock() + " using tool: " + tool.getClass()});
            }
        }

    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    public void onHeltItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Tool current = ToolRegistry.getTool(player.getInventory().getItem(event.getNewSlot()));
        Tool previous = ToolRegistry.getTool(player.getInventory().getItem(event.getPreviousSlot()));
        if (current != null) {
            if (previous != null) {
                if (previous.equals(current)) {
                    return;
                }

                previous.onHotbarDefocused(player);
            }

            current.onHotbarFocused(player);
        } else if (previous != null) {
            previous.onHotbarDefocused(player);
        }

    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onRocketShoot(ProjectileLaunchEvent event) {
        Projectile shot = event.getEntity();

        ProjectileSource shooter;
        try {
            shooter = shot.getShooter();
        } catch (NoSuchMethodError var9) {
            if (MinecraftVersion.atLeast(V.v1_4)) {
                var9.printStackTrace();
            }

            return;
        }

        if (shooter instanceof Player) {
            if (!this.shotRockets.containsKey(shot.getUniqueId())) {
                Player player = (Player)shooter;
                Tool tool = ToolRegistry.getTool(player.getItemInHand());
                if (tool != null && tool instanceof Rocket) {
                    try {
                        Rocket rocket = (Rocket)tool;
                        if (event.isCancelled() && tool.ignoreCancelled()) {
                            return;
                        }

                        if (!rocket.canLaunch(player, shot.getLocation())) {
                            event.setCancelled(true);
                            return;
                        }

                        if (!tool.autoCancel() && !(shot instanceof EnderPearl)) {
                            this.shotRockets.put(shot.getUniqueId(), new ToolsListener.ShotRocket(player, rocket));
                            rocket.onLaunch(shot, player);
                        } else {
                            World world = shot.getWorld();
                            Location loc = shot.getLocation();
                            Common.runLater(() -> {
                                shot.remove();
                            });
                            Common.runLater(1, () -> {
                                Valid.checkNotNull(shot, "shot = null");
                                Valid.checkNotNull(world, "shot.world = null");
                                Valid.checkNotNull(loc, "shot.location = null");
                                Location directedLoc = player.getEyeLocation().add(player.getEyeLocation().getDirection().setY(0).normalize().multiply(1.05D)).add(0.0D, 0.2D, 0.0D);
                                final Projectile copy = (Projectile)world.spawn(directedLoc, shot.getClass());
                                copy.setVelocity(shot.getVelocity());
                                this.shotRockets.put(copy.getUniqueId(), new ToolsListener.ShotRocket(player, rocket));
                                rocket.onLaunch(copy, player);
                                Common.runTimer(1, new CompRunnable() {
                                    private long elapsedTicks = 0L;

                                    public void run() {
                                        if (copy.isValid() && !copy.isOnGround() && this.elapsedTicks++ <= 600L) {
                                            rocket.onFlyTick(copy, player);
                                        } else {
                                            this.cancel();
                                        }

                                    }
                                });
                            });
                        }
                    } catch (Throwable var10) {
                        event.setCancelled(true);
                        Common.tell(player, new String[]{me.ego.ezbd.lib.fo.settings.SimpleLocalization.Tool.ERROR});
                        Common.error(var10, new String[]{"Failed to shoot rocket " + tool.getClass()});
                    }
                }

            }
        }
    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    public void onRocketHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ToolsListener.ShotRocket shot = (ToolsListener.ShotRocket)this.shotRockets.remove(projectile.getUniqueId());
        if (shot != null) {
            Rocket rocket = shot.getRocket();
            Player shooter = shot.getShooter();

            try {
                if (rocket.canExplode(projectile, shooter)) {
                    RocketExplosionEvent rocketEvent = new RocketExplosionEvent(rocket, projectile, rocket.getExplosionPower(), rocket.isBreakBlocks());
                    if (Common.callEvent(rocketEvent)) {
                        Location location = projectile.getLocation();
                        shot.getRocket().onExplode(projectile, shot.getShooter());
                        projectile.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), rocketEvent.getPower(), false, rocketEvent.isBreakBlocks());
                    }
                } else {
                    projectile.remove();
                }
            } catch (Throwable var8) {
                Common.tell(shooter, new String[]{me.ego.ezbd.lib.fo.settings.SimpleLocalization.Tool.ERROR});
                Common.error(var8, new String[]{"Failed to handle impact by rocket " + shot.getRocket().getClass()});
            }
        }

    }

    private final class ShotRocket {
        private final Player shooter;
        private final Rocket rocket;

        public ShotRocket(Player shooter, Rocket rocket) {
            this.shooter = shooter;
            this.rocket = rocket;
        }

        public Player getShooter() {
            return this.shooter;
        }

        public Rocket getRocket() {
            return this.rocket;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof ToolsListener.ShotRocket)) {
                return false;
            } else {
                ToolsListener.ShotRocket other = (ToolsListener.ShotRocket)o;
                Object this$shooter = this.getShooter();
                Object other$shooter = other.getShooter();
                if (this$shooter == null) {
                    if (other$shooter != null) {
                        return false;
                    }
                } else if (!this$shooter.equals(other$shooter)) {
                    return false;
                }

                Object this$rocket = this.getRocket();
                Object other$rocket = other.getRocket();
                if (this$rocket == null) {
                    if (other$rocket != null) {
                        return false;
                    }
                } else if (!this$rocket.equals(other$rocket)) {
                    return false;
                }

                return true;
            }
        }

        public int hashCode() {
            int PRIME = true;
            int result = 1;
            Object $shooter = this.getShooter();
            int resultx = result * 59 + ($shooter == null ? 43 : $shooter.hashCode());
            Object $rocket = this.getRocket();
            resultx = resultx * 59 + ($rocket == null ? 43 : $rocket.hashCode());
            return resultx;
        }

        public String toString() {
            return "ToolsListener.ShotRocket(shooter=" + this.getShooter() + ", rocket=" + this.getRocket() + ")";
        }
    }
}