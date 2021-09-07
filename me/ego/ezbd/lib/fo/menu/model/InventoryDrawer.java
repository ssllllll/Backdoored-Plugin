package me.ego.ezbd.lib.fo.menu.model;

import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class InventoryDrawer {
    private final int size;
    private String title;
    private final ItemStack[] content;

    private InventoryDrawer(int size, String title) {
        this.size = size;
        this.title = title;
        this.content = new ItemStack[size];
    }

    public void pushItem(ItemStack item) {
        boolean added = false;

        for(int i = 0; i < this.content.length; ++i) {
            ItemStack currentItem = this.content[i];
            if (currentItem == null) {
                this.content[i] = item;
                added = true;
                break;
            }
        }

        if (!added) {
            this.content[this.size - 1] = item;
        }

    }

    public boolean isSet(int slot) {
        return this.getItem(slot) != null;
    }

    public ItemStack getItem(int slot) {
        return slot < this.content.length ? this.content[slot] : null;
    }

    public void setItem(int slot, ItemStack item) {
        this.content[slot] = item;
    }

    public void setContent(ItemStack[] newContent) {
        for(int i = 0; i < this.content.length; ++i) {
            this.content[i] = i < newContent.length ? newContent[i] : new ItemStack(CompMaterial.AIR.getMaterial());
        }

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void display(Player player) {
        Inventory inv = this.build(player);
        if (player.getOpenInventory() != null) {
            player.closeInventory();
        }

        player.openInventory(inv);
    }

    public Inventory build() {
        return this.build((InventoryHolder)null);
    }

    public Inventory build(@Nullable InventoryHolder holder) {
        Inventory inv = Bukkit.createInventory(holder, this.size, Common.colorize("&0" + this.title));
        inv.setContents(this.content);
        return inv;
    }

    public static InventoryDrawer of(int size, String title) {
        return new InventoryDrawer(size, title);
    }

    public int getSize() {
        return this.size;
    }
}