package me.ego.ezbd.lib.fo.menu.tool;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class BlockTool extends Tool {
    private PlayerInteractEvent event;

    public BlockTool() {
    }

    protected final void onBlockClick(PlayerInteractEvent event) {
        this.event = event;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            this.onBlockClick(player, ClickType.RIGHT, block);
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            this.onBlockClick(player, ClickType.LEFT, block);
        } else if (action == Action.RIGHT_CLICK_AIR) {
            this.onAirClick(player, ClickType.RIGHT);
        } else if (action == Action.LEFT_CLICK_AIR) {
            this.onAirClick(player, ClickType.LEFT);
        }

    }

    protected abstract void onBlockClick(Player var1, ClickType var2, Block var3);

    protected void onAirClick(Player player, ClickType click) {
    }

    protected final boolean ignoreCancelled() {
        return false;
    }

    protected PlayerInteractEvent getEvent() {
        return this.event;
    }
}