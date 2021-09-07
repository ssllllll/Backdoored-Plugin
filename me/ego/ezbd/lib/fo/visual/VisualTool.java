package me.ego.ezbd.lib.fo.visual;

import java.util.Iterator;
import java.util.List;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.menu.tool.BlockTool;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public abstract class VisualTool extends BlockTool {
    protected final void onBlockClick(Player player, ClickType click, Block block) {
        this.stopVisualizing(player);
        this.handleBlockClick(player, click, block);
        this.visualize(player);
    }

    protected abstract void handleBlockClick(Player var1, ClickType var2, Block var3);

    protected final void onAirClick(Player player, ClickType click) {
        this.stopVisualizing(player);
        this.handleAirClick(player, click);
        this.visualize(player);
    }

    protected void handleAirClick(Player player, ClickType click) {
    }

    protected final void onHotbarFocused(Player player) {
        this.visualize(player);
    }

    protected final void onHotbarDefocused(Player player) {
        this.stopVisualizing(player);
    }

    protected abstract List<Location> getVisualizedPoints(Player var1);

    protected VisualizedRegion getVisualizedRegion(Player player) {
        return null;
    }

    protected abstract String getBlockName(Block var1, Player var2);

    protected abstract CompMaterial getBlockMask(Block var1, Player var2);

    private void visualize(@NonNull Player player) {
        if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
        } else {
            VisualizedRegion region = this.getVisualizedRegion(player);
            if (region != null && region.isWhole() && !region.canSeeParticles(player)) {
                region.showParticles(player);
            }

            Iterator var3 = this.getVisualizedPoints(player).iterator();

            while(var3.hasNext()) {
                Location location = (Location)var3.next();
                if (location != null) {
                    Block block = location.getBlock();
                    if (!BlockVisualizer.isVisualized(block)) {
                        BlockVisualizer.visualize(block, this.getBlockMask(block, player), this.getBlockName(block, player));
                    }
                }
            }

        }
    }

    private void stopVisualizing(@NonNull Player player) {
        if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
        } else {
            VisualizedRegion region = this.getVisualizedRegion(player);
            if (region != null && region.canSeeParticles(player)) {
                region.hideParticles(player);
            }

            Iterator var3 = this.getVisualizedPoints(player).iterator();

            while(var3.hasNext()) {
                Location location = (Location)var3.next();
                if (location != null) {
                    Block block = location.getBlock();
                    if (BlockVisualizer.isVisualized(block)) {
                        BlockVisualizer.stopVisualizing(block);
                    }
                }
            }

        }
    }

    protected VisualTool() {
    }
}