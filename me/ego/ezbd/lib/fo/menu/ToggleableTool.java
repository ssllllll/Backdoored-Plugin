package me.ego.ezbd.lib.fo.menu;

import java.util.Arrays;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.menu.tool.Tool;
import me.ego.ezbd.lib.fo.model.SimpleEnchant;
import me.ego.ezbd.lib.fo.remain.CompItemFlag;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

final class ToggleableTool {
    private final ItemStack item;
    private boolean playerHasTool = false;

    ToggleableTool(Object unparsed) {
        if (unparsed != null) {
            if (unparsed instanceof ItemStack) {
                this.item = (ItemStack)unparsed;
            } else if (unparsed instanceof Tool) {
                this.item = ((Tool)unparsed).getItem();
            } else {
                if (!(unparsed instanceof Number) || ((Number)unparsed).intValue() != 0) {
                    throw new FoException("Unknown tool: " + unparsed + " (we only accept ItemStack, Tool's instance or 0 for air)");
                }

                this.item = new ItemStack(Material.AIR);
            }
        } else {
            this.item = new ItemStack(Material.AIR);
        }

    }

    ItemStack get(Player player) {
        this.update(player);
        return this.playerHasTool ? this.getToolWhenHas() : this.getToolWhenHasnt();
    }

    private void update(Player pl) {
        this.playerHasTool = pl.getOpenInventory().getBottomInventory().containsAtLeast(this.item, 1);
    }

    private ItemStack getToolWhenHas() {
        return ItemCreator.of(this.item).enchant(new SimpleEnchant(Enchantment.ARROW_INFINITE, 1)).flag(CompItemFlag.HIDE_ENCHANTS).lores(Arrays.asList("", "&cYou already have this item.", "&7Click to take it away.")).build().make();
    }

    private ItemStack getToolWhenHasnt() {
        return this.item;
    }

    void giveOrTake(Player player) {
        PlayerInventory inv = player.getInventory();
        if (this.playerHasTool = !this.playerHasTool) {
            inv.addItem(new ItemStack[]{this.item});
        } else {
            inv.removeItem(new ItemStack[]{this.item});
        }

    }

    boolean equals(ItemStack item) {
        return this.getToolWhenHas().isSimilar(item) || this.getToolWhenHasnt().isSimilar(item);
    }

    public String toString() {
        return "Toggleable{" + this.item.getType() + "}";
    }
}