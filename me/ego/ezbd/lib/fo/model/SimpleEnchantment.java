package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.ChatUtil;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MathUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class SimpleEnchantment extends Enchantment {
    private static final Pattern VALID_NAMESPACE = Pattern.compile("[a-z0-9._-]+");
    private final String name;
    private final int maxLevel;

    protected SimpleEnchantment(String name, int maxLevel) {
        super(toKey(name));
        this.name = name;
        this.maxLevel = maxLevel;
        Remain.registerEnchantment(this);
    }

    private static NamespacedKey toKey(@NonNull String name) {
        if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
        } else {
            Valid.checkBoolean(MinecraftVersion.atLeast(V.v1_13), "Unfortunately, SimpleEnchantment requires Minecraft 1.13.2 or greater. Cannot make " + name, new Object[0]);
            name = new String(name);
            name = name.toLowerCase().replace(" ", "_");
            name = ChatUtil.replaceDiacritic(name);
            Valid.checkBoolean(name != null && VALID_NAMESPACE.matcher(name).matches(), "Enchant name must only contain English alphabet names: " + name, new Object[0]);
            return new NamespacedKey(SimplePlugin.getInstance(), name);
        }
    }

    protected void onDamage(int level, LivingEntity damager, EntityDamageByEntityEvent event) {
    }

    protected void onInteract(int level, PlayerInteractEvent event) {
    }

    protected void onBreakBlock(int level, BlockBreakEvent event) {
    }

    protected void onShoot(int level, LivingEntity shooter, ProjectileLaunchEvent event) {
    }

    protected void onHit(int level, LivingEntity shooter, ProjectileHitEvent event) {
    }

    public ItemStack applyTo(ItemStack item, int level) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(this, level, true);
        item.setItemMeta(meta);
        return item;
    }

    public String getLore(int level) {
        return this.name + " " + MathUtil.toRoman(level);
    }

    public SimpleEnchantmentTarget getCustomItemTarget() {
        return SimpleEnchantmentTarget.BREAKABLE;
    }

    public Material enchantMaterial() {
        return null;
    }

    public Set<Material> enchantMaterials() {
        return new HashSet();
    }

    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    public int getStartLevel() {
        return 1;
    }

    public boolean isTreasure() {
        return false;
    }

    public boolean isCursed() {
        return false;
    }

    public final int getMaxLevel() {
        return this.maxLevel;
    }

    public final String getName() {
        return this.name;
    }

    public static Map<SimpleEnchantment, Integer> findEnchantments(ItemStack item) {
        Map<SimpleEnchantment, Integer> map = new HashMap();
        if (item == null) {
            return map;
        } else {
            Object vanilla;
            try {
                vanilla = item.hasItemMeta() ? item.getItemMeta().getEnchants() : new HashMap();
            } catch (NoSuchMethodError var7) {
                if (Remain.hasItemMeta()) {
                    var7.printStackTrace();
                }

                return map;
            } catch (NullPointerException var8) {
                return map;
            }

            Iterator var3 = ((Map)vanilla).entrySet().iterator();

            while(var3.hasNext()) {
                Entry<Enchantment, Integer> e = (Entry)var3.next();
                Enchantment enchantment = (Enchantment)e.getKey();
                int level = (Integer)e.getValue();
                if (enchantment instanceof SimpleEnchantment) {
                    map.put((SimpleEnchantment)enchantment, level);
                }
            }

            return map;
        }
    }

    /** @deprecated */
    @Deprecated
    public static ItemStack addEnchantmentLores(ItemStack item) {
        ArrayList customEnchants = new ArrayList();

        try {
            Iterator var2 = item.getEnchantments().entrySet().iterator();

            while(var2.hasNext()) {
                Entry<Enchantment, Integer> e = (Entry)var2.next();
                if (e.getKey() instanceof SimpleEnchantment) {
                    String lore = ((SimpleEnchantment)e.getKey()).getLore((Integer)e.getValue());
                    if (lore != null && !lore.isEmpty()) {
                        customEnchants.add(Common.colorize("&r&7" + lore));
                    }
                }
            }
        } catch (NullPointerException var9) {
        }

        if (customEnchants.isEmpty()) {
            return null;
        } else {
            ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
            List<String> originalLore = meta.hasLore() ? meta.getLore() : new ArrayList();
            List<String> finalLore = new ArrayList();
            List<String> colorlessOriginals = new ArrayList();
            Iterator var6 = ((List)originalLore).iterator();

            String customEnchant;
            while(var6.hasNext()) {
                customEnchant = (String)var6.next();
                colorlessOriginals.add(ChatColor.stripColor(Common.colorize(customEnchant)));
            }

            var6 = customEnchants.iterator();

            while(var6.hasNext()) {
                customEnchant = (String)var6.next();
                String colorlessEnchant = ChatColor.stripColor(Common.colorize(customEnchant));
                if (!colorlessOriginals.contains(colorlessEnchant)) {
                    finalLore.add(customEnchant);
                }
            }

            finalLore.addAll((Collection)originalLore);
            meta.setLore(finalLore);
            item.setItemMeta(meta);
            return item;
        }
    }
}