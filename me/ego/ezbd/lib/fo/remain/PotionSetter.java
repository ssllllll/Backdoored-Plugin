package me.ego.ezbd.lib.fo.remain;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

class PotionSetter {
    PotionSetter() {
    }

    public static void setPotion(ItemStack item, PotionEffectType type, int level) {
        PotionType wrapped = PotionType.getByEffect(type);
        PotionMeta meta = (PotionMeta)item.getItemMeta();

        try {
            PotionData data = new PotionData(level > 0 && wrapped != null ? wrapped : PotionType.WATER);
            if (level > 0 && wrapped == null) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
            }

            meta.setBasePotionData(data);
        } catch (NoClassDefFoundError | NoSuchMethodError var6) {
            meta.setMainEffect(type);
            meta.addCustomEffect(new PotionEffect(type, 2147483647, level - 1), true);
        }

        item.setItemMeta(meta);
    }
}