package me.ego.ezbd.lib.fo.remain;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum CompItemFlag {
    HIDE_ENCHANTS,
    HIDE_ATTRIBUTES,
    HIDE_UNBREAKABLE,
    HIDE_DESTROYS,
    HIDE_PLACED_ON,
    HIDE_POTION_EFFECTS;

    private CompItemFlag() {
    }

    public final void applyTo(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            ItemFlag bukkitFlag = ItemFlag.valueOf(this.toString());
            meta.addItemFlags(new ItemFlag[]{bukkitFlag});
            item.setItemMeta(meta);
        } catch (Throwable var4) {
        }

    }
}