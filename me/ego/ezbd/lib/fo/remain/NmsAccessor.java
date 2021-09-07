package me.ego.ezbd.lib.fo.remain;

import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

final class NmsAccessor {
    static final Method createEntity;
    static final Method getBukkitEntity;
    static final Method addEntity;
    private static volatile boolean hasEntityConsumer = false;
    private static volatile boolean olderThan18;

    NmsAccessor() {
    }

    static void call() {
    }

    static Object addEntity(World bukkitWorld, Object nmsEntity, SpawnReason reason) throws ReflectiveOperationException {
        if (olderThan18) {
            addEntity.invoke(Remain.getHandleWorld(bukkitWorld), nmsEntity, reason);
            return getBukkitEntity.invoke(nmsEntity);
        } else {
            return hasEntityConsumer ? addEntity.invoke(bukkitWorld, nmsEntity, reason, null) : addEntity.invoke(bukkitWorld, nmsEntity, reason);
        }
    }

    static {
        try {
            Class<?> nmsEntity = ReflectionUtil.getNMSClass("Entity");
            Class<?> ofcWorld = ReflectionUtil.getOBCClass("CraftWorld");
            olderThan18 = MinecraftVersion.olderThan(V.v1_8);
            createEntity = MinecraftVersion.newerThan(V.v1_7) ? ofcWorld.getDeclaredMethod("createEntity", Location.class, Class.class) : null;
            getBukkitEntity = nmsEntity.getMethod("getBukkitEntity");
            if (MinecraftVersion.newerThan(V.v1_10)) {
                hasEntityConsumer = true;
                addEntity = ofcWorld.getDeclaredMethod("addEntity", nmsEntity, SpawnReason.class, Class.forName("org.bukkit.util.Consumer"));
            } else if (MinecraftVersion.newerThan(V.v1_7)) {
                addEntity = ofcWorld.getDeclaredMethod("addEntity", nmsEntity, SpawnReason.class);
            } else {
                addEntity = ReflectionUtil.getNMSClass("World").getDeclaredMethod("addEntity", nmsEntity, SpawnReason.class);
            }

        } catch (ReflectiveOperationException var2) {
            throw new FoException(var2, "Error setting up nms entity accessor!");
        }
    }
}