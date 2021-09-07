package me.ego.ezbd.lib.fo;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.ego.ezbd.lib.fo.EntityUtil.HitListener;
import me.ego.ezbd.lib.fo.collection.expiringmap.ExpiringMap;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

class HitTracking implements Listener {
    private static final ExpiringMap<UUID, HitListener> flyingProjectiles;

    HitTracking() {
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onHit(ProjectileHitEvent event) {
        HitListener hitListener = (HitListener)flyingProjectiles.remove(event.getEntity().getUniqueId());
        if (hitListener != null) {
            hitListener.onHit(event);
        }

    }

    static void addFlyingProjectile(Projectile projectile, HitListener hitTask) {
        flyingProjectiles.put(projectile.getUniqueId(), hitTask);
    }

    static {
        flyingProjectiles = ExpiringMap.builder().expiration(30L, TimeUnit.SECONDS).build();
    }
}