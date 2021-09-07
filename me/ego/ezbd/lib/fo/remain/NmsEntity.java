package me.ego.ezbd.lib.fo.remain;

import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public final class NmsEntity {
    private final World bukkitWorld;
    private final Object nmsEntity;

    public NmsEntity(Class<?> entityClass) {
        this(new Location((World)Bukkit.getWorlds().get(0), 0.0D, 0.0D, 0.0D), entityClass);
    }

    public NmsEntity(Location location, Class<?> entityClass) {
        try {
            NmsAccessor.call();
        } catch (Throwable var4) {
            throw new FoException(var4, "Failed to setup entity reflection! MC version: " + MinecraftVersion.getCurrent());
        }

        this.bukkitWorld = location.getWorld();
        this.nmsEntity = MinecraftVersion.equals(V.v1_7) ? getHandle(location, entityClass) : this.createEntity(location, entityClass);
    }

    private static Object getHandle(Location location, Class<?> entityClass) {
        Entity entity = (new Location(location.getWorld(), -1.0D, 0.0D, -1.0D)).getWorld().spawn(location, entityClass);

        try {
            return entity.getClass().getMethod("getHandle").invoke(entity);
        } catch (ReflectiveOperationException var4) {
            throw new Error(var4);
        }
    }

    private Object createEntity(Location location, Class<?> entityClass) {
        try {
            return NmsAccessor.createEntity.invoke(this.bukkitWorld, location, entityClass);
        } catch (ReflectiveOperationException var4) {
            throw new FoException(var4, "Error creating entity " + entityClass + " at " + location);
        }
    }

    public <T extends Entity> T addEntity(SpawnReason reason) {
        try {
            return (Entity)NmsAccessor.addEntity(this.bukkitWorld, this.nmsEntity, reason);
        } catch (ReflectiveOperationException var3) {
            throw new FoException(var3, "Error creating entity " + this.nmsEntity + " for " + reason);
        }
    }

    public Entity getBukkitEntity() {
        try {
            return (Entity)NmsAccessor.getBukkitEntity.invoke(this.nmsEntity);
        } catch (ReflectiveOperationException var2) {
            throw new FoException(var2, "Error getting bukkit entity from " + this.nmsEntity);
        }
    }

    public Object getNmsEntity() {
        return this.nmsEntity;
    }
}