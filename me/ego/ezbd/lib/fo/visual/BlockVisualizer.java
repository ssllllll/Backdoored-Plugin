package me.ego.ezbd.lib.fo.visual;

import java.util.HashSet;
import java.util.Iterator;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.CompProperty;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class BlockVisualizer {
    private static final StrictMap<Location, Object> visualizedBlocks = new StrictMap();

    public static void visualize(@NonNull Block block, CompMaterial mask, String blockName) {
        if (block == null) {
            throw new NullPointerException("block is marked non-null but is null");
        } else {
            Valid.checkBoolean(!isVisualized(block), "Block at " + block.getLocation() + " already visualized", new Object[0]);
            Location location = block.getLocation();
            FallingBlock falling = spawnFallingBlock(location, mask, blockName);
            Iterator var5 = block.getWorld().getPlayers().iterator();

            while(var5.hasNext()) {
                Player player = (Player)var5.next();
                Remain.sendBlockChange(2, player, location, MinecraftVersion.olderThan(V.v1_9) ? mask : CompMaterial.BARRIER);
            }

            visualizedBlocks.put(location, falling == null ? false : falling);
        }
    }

    private static FallingBlock spawnFallingBlock(Location location, CompMaterial mask, String blockName) {
        if (MinecraftVersion.olderThan(V.v1_9)) {
            return null;
        } else {
            FallingBlock falling = Remain.spawnFallingBlock(location.clone().add(0.5D, 0.0D, 0.5D), mask.getMaterial());
            falling.setDropItem(false);
            falling.setVelocity(new Vector(0, 0, 0));
            Remain.setCustomName(falling, blockName);
            CompProperty.GLOWING.apply(falling, true);
            CompProperty.GRAVITY.apply(falling, false);
            return falling;
        }
    }

    public static void stopVisualizing(@NonNull Block block) {
        if (block == null) {
            throw new NullPointerException("block is marked non-null but is null");
        } else {
            Valid.checkBoolean(isVisualized(block), "Block at " + block.getLocation() + " not visualized", new Object[0]);
            Object fallingBlock = visualizedBlocks.remove(block.getLocation());
            if (fallingBlock instanceof FallingBlock) {
                ((FallingBlock)fallingBlock).remove();
            }

            Iterator var2 = block.getWorld().getPlayers().iterator();

            while(var2.hasNext()) {
                Player player = (Player)var2.next();
                Remain.sendBlockChange(1, player, block);
            }

        }
    }

    public static void stopAll() {
        Iterator var0 = (new HashSet(visualizedBlocks.keySet())).iterator();

        while(var0.hasNext()) {
            Location location = (Location)var0.next();
            Block block = location.getBlock();
            if (isVisualized(block)) {
                stopVisualizing(block);
            }
        }

    }

    public static boolean isVisualized(@NonNull Block block) {
        if (block == null) {
            throw new NullPointerException("block is marked non-null but is null");
        } else {
            return visualizedBlocks.contains(block.getLocation());
        }
    }

    private BlockVisualizer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}