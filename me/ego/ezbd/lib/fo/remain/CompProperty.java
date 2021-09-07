package me.ego.ezbd.lib.fo.remain;

import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.remain.nbt.NBTEntity;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;

public enum CompProperty {
    UNBREAKABLE(ItemMeta.class, Boolean.TYPE),
    GLOWING(Entity.class, Boolean.TYPE),
    AI(Entity.class, Boolean.TYPE),
    GRAVITY(Entity.class, Boolean.TYPE),
    SILENT(Entity.class, Boolean.TYPE),
    INVULNERABLE(Entity.class, Boolean.TYPE);

    private final Class<?> requiredClass;
    private final Class<?> setterMethodType;

    public final void apply(Object instance, Object key) {
        Valid.checkNotNull(instance, "instance is null!");
        Valid.checkBoolean(this.requiredClass.isAssignableFrom(instance.getClass()), this + " accepts " + this.requiredClass.getSimpleName() + ", not " + instance.getClass().getSimpleName(), new Object[0]);

        try {
            Method m = this.getMethod(instance.getClass());
            m.setAccessible(true);
            m.invoke(instance, key);
        } catch (ReflectiveOperationException var9) {
            if (var9 instanceof NoSuchMethodException && MinecraftVersion.olderThan(V.values()[0])) {
                if (instance instanceof Entity) {
                    NBTEntity nbtEntity = new NBTEntity((Entity)instance);
                    boolean has = Boolean.parseBoolean(key.toString());
                    if (this == INVULNERABLE) {
                        nbtEntity.setInteger("Invulnerable", has ? 1 : 0);
                    } else if (this == AI) {
                        nbtEntity.setInteger("NoAI", has ? 0 : 1);
                    } else if (this == GRAVITY) {
                        nbtEntity.setInteger("NoGravity", has ? 0 : 1);
                    }
                }

                if (Remain.hasItemMeta() && instance instanceof ItemMeta && this == UNBREAKABLE) {
                    try {
                        boolean has = Boolean.parseBoolean(key.toString());
                        Method spigotMethod = instance.getClass().getMethod("spigot");
                        spigotMethod.setAccessible(true);
                        Object spigot = spigotMethod.invoke(instance);
                        Method setUnbreakable = spigot.getClass().getMethod("setUnbreakable", Boolean.TYPE);
                        setUnbreakable.setAccessible(true);
                        setUnbreakable.invoke(spigot, has);
                    } catch (Throwable var8) {
                        if (MinecraftVersion.atLeast(V.v1_8)) {
                            var8.printStackTrace();
                        }
                    }
                }
            } else {
                var9.printStackTrace();
            }
        }

    }

    public final boolean isAvailable(Class<?> clazz) {
        try {
            this.getMethod(clazz);
        } catch (ReflectiveOperationException var3) {
            if (var3 instanceof NoSuchMethodException && MinecraftVersion.olderThan(V.values()[0])) {
                return false;
            }
        }

        return true;
    }

    private final Method getMethod(Class<?> clazz) throws ReflectiveOperationException {
        return clazz.getMethod("set" + (this.toString().equals("AI") ? "AI" : WordUtils.capitalize(this.toString().toLowerCase())), this.setterMethodType);
    }

    private CompProperty(Class<?> requiredClass, Class<?> setterMethodType) {
        this.requiredClass = requiredClass;
        this.setterMethodType = setterMethodType;
    }

    public Class<?> getRequiredClass() {
        return this.requiredClass;
    }
}
