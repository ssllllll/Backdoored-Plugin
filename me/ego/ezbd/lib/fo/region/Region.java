package me.ego.ezbd.lib.fo.region;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.BlockUtil;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.model.ConfigSerializable;
import me.ego.ezbd.lib.fo.visual.VisualizedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Region implements ConfigSerializable {
    public static final VisualizedRegion EMPTY = new VisualizedRegion((Location)null, (Location)null);
    @Nullable
    private String name;
    private Location primary;
    private Location secondary;

    public Region(@Nullable Location primary, @Nullable Location secondary) {
        this((String)null, primary, secondary);
    }

    public Region(@Nullable String name, @Nullable Location primary, @Nullable Location secondary) {
        this.name = name;
        if (primary != null) {
            Valid.checkNotNull(primary.getWorld(), "Primary location lacks a world!");
            this.primary = primary;
        }

        if (secondary != null) {
            Valid.checkNotNull(secondary.getWorld(), "Primary location lacks a world!");
            this.secondary = secondary;
        }

    }

    private Location[] getCorrectedPoints() {
        if (this.primary != null && this.secondary != null) {
            Valid.checkBoolean(this.primary.getWorld().getName().equals(this.secondary.getWorld().getName()), "Points must be in one world! Primary: " + this.primary + " != secondary: " + this.secondary, new Object[0]);
            int x1 = this.primary.getBlockX();
            int x2 = this.secondary.getBlockX();
            int y1 = this.primary.getBlockY();
            int y2 = this.secondary.getBlockY();
            int z1 = this.primary.getBlockZ();
            int z2 = this.secondary.getBlockZ();
            Location primary = this.primary.clone();
            Location secondary = this.secondary.clone();
            primary.setX((double)Math.min(x1, x2));
            primary.setY((double)Math.min(y1, y2));
            primary.setZ((double)Math.min(z1, z2));
            secondary.setX((double)Math.max(x1, x2));
            secondary.setY((double)Math.max(y1, y2));
            secondary.setZ((double)Math.max(z1, z2));
            return new Location[]{primary, secondary};
        } else {
            return null;
        }
    }

    public final Location getCenter() {
        Valid.checkBoolean(this.isWhole(), "Cannot perform getCenter on a non-complete region: " + this.toString(), new Object[0]);
        Location[] centered = this.getCorrectedPoints();
        Location primary = centered[0];
        Location secondary = centered[1];
        return new Location(primary.getWorld(), (primary.getX() + secondary.getX()) / 2.0D, (primary.getY() + secondary.getY()) / 2.0D, (primary.getZ() + secondary.getZ()) / 2.0D);
    }

    public final List<Block> getBlocks() {
        Valid.checkBoolean(this.isWhole(), "Cannot perform getBlocks on a non-complete region: " + this.toString(), new Object[0]);
        Location[] centered = this.getCorrectedPoints();
        return BlockUtil.getBlocks(centered[0], centered[1]);
    }

    public final Set<Location> getBoundingBox() {
        Valid.checkBoolean(this.isWhole(), "Cannot perform getBoundingBox on a non-complete region: " + this.toString(), new Object[0]);
        return BlockUtil.getBoundingBox(this.primary, this.secondary);
    }

    public final List<Entity> getEntities() {
        Valid.checkBoolean(this.isWhole(), "Cannot perform getEntities on a non-complete region: " + this.toString(), new Object[0]);
        List<Entity> found = new LinkedList();
        Location[] centered = this.getCorrectedPoints();
        Location primary = centered[0];
        Location secondary = centered[1];
        int xMin = (int)primary.getX() >> 4;
        int xMax = (int)secondary.getX() >> 4;
        int zMin = (int)primary.getZ() >> 4;
        int zMax = (int)secondary.getZ() >> 4;

        for(int cx = xMin; cx <= xMax; ++cx) {
            for(int cz = zMin; cz <= zMax; ++cz) {
                Entity[] var11 = this.getWorld().getChunkAt(cx, cz).getEntities();
                int var12 = var11.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    Entity entity = var11[var13];
                    if (entity.isValid() && entity.getLocation() != null && this.isWithin(entity.getLocation())) {
                        found.add(entity);
                    }
                }
            }
        }

        return found;
    }

    public final World getWorld() {
        if (!this.isWhole()) {
            return null;
        } else if (this.primary != null && this.secondary == null) {
            return Bukkit.getWorld(this.primary.getWorld().getName());
        } else if (this.secondary != null && this.primary == null) {
            return Bukkit.getWorld(this.secondary.getWorld().getName());
        } else {
            Valid.checkBoolean(this.primary.getWorld().getName().equals(this.secondary.getWorld().getName()), "Worlds of this region not the same: " + this.primary.getWorld() + " != " + this.secondary.getWorld(), new Object[0]);
            return Bukkit.getWorld(this.primary.getWorld().getName());
        }
    }

    public final boolean isWithin(@NonNull Location location) {
        if (location == null) {
            throw new NullPointerException("location is marked non-null but is null");
        } else {
            Valid.checkBoolean(this.isWhole(), "Cannot perform isWithin on a non-complete region: " + this.toString(), new Object[0]);
            if (!location.getWorld().getName().equals(this.primary.getWorld().getName())) {
                return false;
            } else {
                Location[] centered = this.getCorrectedPoints();
                Location primary = centered[0];
                Location secondary = centered[1];
                int x = (int)location.getX();
                int y = (int)location.getY();
                int z = (int)location.getZ();
                return (double)x >= primary.getX() && (double)x <= secondary.getX() && (double)y >= primary.getY() && (double)y <= secondary.getY() && (double)z >= primary.getZ() && (double)z <= secondary.getZ();
            }
        }
    }

    public final boolean isWhole() {
        return this.primary != null && this.secondary != null;
    }

    public final void setPrimary(Location primary) {
        this.primary = primary;
    }

    public final void setSecondary(Location secondary) {
        this.secondary = secondary;
    }

    public final void updateLocationsWeak(Location primary, Location secondary) {
        if (primary != null) {
            this.primary = primary;
        }

        if (secondary != null) {
            this.secondary = secondary;
        }

    }

    public final String toString() {
        return this.getClass().getSimpleName() + "{name=" + this.name + ",location=" + Common.shortLocation(this.primary) + " - " + Common.shortLocation(this.secondary) + "}";
    }

    public final SerializedMap serialize() {
        SerializedMap map = new SerializedMap();
        map.putIfExist("Name", this.name);
        map.putIfExist("Primary", this.primary);
        map.putIfExist("Secondary", this.secondary);
        return map;
    }

    public static Region deserialize(SerializedMap map) {
        Valid.checkBoolean(map.containsKey("Primary") && map.containsKey("Secondary"), "The region must have Primary and a Secondary location", new Object[0]);
        String name = map.getString("Name");
        Location prim = map.getLocation("Primary");
        Location sec = map.getLocation("Secondary");
        return new Region(name, prim, sec);
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    public Location getPrimary() {
        return this.primary;
    }

    public Location getSecondary() {
        return this.secondary;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }
}