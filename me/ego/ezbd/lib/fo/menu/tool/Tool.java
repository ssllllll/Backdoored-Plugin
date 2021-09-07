package me.ego.ezbd.lib.fo.menu.tool;

import me.ego.ezbd.lib.fo.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class Tool {
    protected Tool() {
        (new Thread(() -> {
            try {
                Thread.sleep(3L);
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }

            if (!ToolRegistry.isRegistered(this)) {
                ToolRegistry.register(this);
            }

        })).start();
    }

    public boolean isTool(ItemStack item) {
        return ItemUtil.isSimilar(this.getItem(), item);
    }

    public boolean hasToolInHand(Player player) {
        return this.isTool(player.getItemInHand());
    }

    public abstract ItemStack getItem();

    protected void onBlockClick(PlayerInteractEvent event) {
    }

    protected void onBlockPlace(BlockPlaceEvent event) {
    }

    protected void onHotbarFocused(Player player) {
    }

    protected void onHotbarDefocused(Player player) {
    }

    protected boolean ignoreCancelled() {
        return true;
    }

    protected boolean autoCancel() {
        return false;
    }

    public final void give(Player player, int slot) {
        player.getInventory().setItem(slot, this.getItem());
    }

    public final void give(Player player) {
        player.getInventory().addItem(new ItemStack[]{this.getItem()});
    }

    public final boolean equals(Object obj) {
        return obj instanceof Tool && ((Tool)obj).getItem().equals(this.getItem());
    }
}