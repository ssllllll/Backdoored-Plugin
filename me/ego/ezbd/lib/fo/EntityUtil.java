package me.ego.ezbd.lib.fo;

import java.util.function.Consumer;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.HookManager;
import me.ego.ezbd.lib.fo.remain.CompRunnable;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public final class EntityUtil {
    public static Player getTargetPlayer(Entity entity) {
        LivingEntity target = getTarget(entity);
        return target instanceof Player && !HookManager.isNPC(target) ? (Player)target : null;
    }

    public static LivingEntity getTarget(Entity entity) {
        return entity instanceof Creature ? ((Creature)entity).getTarget() : null;
    }

    public static boolean isAggressive(Entity entity) {
        if (!(entity instanceof Ghast) && !(entity instanceof Slime)) {
            if (entity instanceof Wolf && ((Wolf)entity).isAngry()) {
                return true;
            } else {
                return entity instanceof Animals ? false : entity instanceof Creature;
            }
        } else {
            return true;
        }
    }

    public static boolean isCreature(Entity entity) {
        return entity instanceof Slime || entity instanceof Wolf || entity instanceof Creature;
    }

    public static boolean canBeCleaned(Entity entity) {
        return entity instanceof FallingBlock || entity instanceof Item || entity instanceof Projectile || entity instanceof ExperienceOrb;
    }

    public static Item dropItem(Location location, ItemStack item, Consumer<Item> modifier) {
        return Remain.spawnItem(location, item, modifier);
    }

    public static void trackFalling(Entity entity, Runnable hitGroundListener) {
        track(entity, 600, (Runnable)null, hitGroundListener);
    }

    public static void trackFlying(Entity entity, Runnable flyListener) {
        track(entity, 600, flyListener, (Runnable)null);
    }

    public static void track(final Entity entity, final int timeoutTicks, final Runnable flyListener, final Runnable hitGroundListener) {
        if (flyListener == null && hitGroundListener == null) {
            throw new FoException("Cannot track entity with fly and hit listeners on null!");
        } else {
            final boolean isProjectile = entity instanceof Projectile;
            if (isProjectile && hitGroundListener != null) {
                HitTracking.addFlyingProjectile((Projectile)entity, (event) -> {
                    hitGroundListener.run();
                });
            }

            Common.runTimer(1, new CompRunnable() {
                private int elapsedTicks = 0;

                public void run() {
                    if (this.elapsedTicks++ > timeoutTicks) {
                        this.cancel();
                    } else if (entity != null && !entity.isDead() && entity.isValid()) {
                        if (entity.isOnGround()) {
                            if (!isProjectile && hitGroundListener != null) {
                                hitGroundListener.run();
                            }

                            this.cancel();
                        } else if (flyListener != null) {
                            flyListener.run();
                        }

                    } else {
                        if (entity instanceof FallingBlock && !isProjectile && hitGroundListener != null) {
                            hitGroundListener.run();
                        }

                        this.cancel();
                    }
                }
            });
        }
    }

    public static void trackHit(Projectile projectile, EntityUtil.HitListener hitTask) {
        HitTracking.addFlyingProjectile(projectile, hitTask);
    }

    private EntityUtil() {
    }

    static {
        Common.registerEvents(new HitTracking());
    }

    public interface HitListener {
        void onHit(ProjectileHitEvent var1);
    }
}