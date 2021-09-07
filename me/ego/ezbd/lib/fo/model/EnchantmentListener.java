package me.ego.ezbd.lib.fo.model;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import me.ego.ezbd.lib.fo.EntityUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public final class EnchantmentListener implements Listener {
    public EnchantmentListener() {
    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof LivingEntity) {
            this.execute((LivingEntity)damager, (enchant, level) -> {
                enchant.onDamage(level, (LivingEntity)damager, event);
            });
        }

    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = false
    )
    public void onInteract(PlayerInteractEvent event) {
        this.execute(event.getPlayer(), (enchant, level) -> {
            enchant.onInteract(level, event);
        });
    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    public void onBreakBlock(BlockBreakEvent event) {
        this.execute(event.getPlayer(), (enchant, level) -> {
            enchant.onBreakBlock(level, event);
        });
    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    public void onShoot(ProjectileLaunchEvent event) {
        try {
            ProjectileSource projectileSource = event.getEntity().getShooter();
            if (projectileSource instanceof LivingEntity) {
                LivingEntity shooter = (LivingEntity)projectileSource;
                this.execute(shooter, (enchant, level) -> {
                    enchant.onShoot(level, shooter, event);
                });
                EntityUtil.trackHit(event.getEntity(), (hitEvent) -> {
                    this.execute(shooter, (enchant, level) -> {
                        enchant.onHit(level, shooter, hitEvent);
                    });
                });
            }
        } catch (NoSuchMethodError var4) {
            if (MinecraftVersion.atLeast(V.v1_4)) {
                var4.printStackTrace();
            }
        }

    }

    private void execute(LivingEntity source, BiConsumer<SimpleEnchantment, Integer> executer) {
        try {
            ItemStack hand = source instanceof Player ? ((Player)source).getItemInHand() : source.getEquipment().getItemInHand();
            if (hand != null) {
                Iterator var4 = SimpleEnchantment.findEnchantments(hand).entrySet().iterator();

                while(var4.hasNext()) {
                    Entry<SimpleEnchantment, Integer> e = (Entry)var4.next();
                    executer.accept(e.getKey(), e.getValue());
                }
            }
        } catch (NoSuchMethodError var6) {
            if (Remain.hasItemMeta()) {
                var6.printStackTrace();
            }
        }

    }
}