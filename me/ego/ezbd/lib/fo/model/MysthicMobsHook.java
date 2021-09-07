package me.ego.ezbd.lib.fo.model;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.entity.Entity;

class MythicMobsHook {
    MythicMobsHook() {
    }

    final String getBossName(Entity entity) {
        try {
            Class<?> mythicMobs = ReflectionUtil.lookupClass("io.lumine.xikage.mythicmobs.MythicMobs");
            Object instance = ReflectionUtil.invokeStatic(mythicMobs, "inst", new Object[0]);
            Object mobManager = ReflectionUtil.invoke("getMobManager", instance, new Object[0]);
            Optional<Object> activeMob = (Optional)ReflectionUtil.invoke(ReflectionUtil.getMethod(mobManager.getClass(), "getActiveMob", new Class[]{UUID.class}), mobManager, new Object[]{entity.getUniqueId()});
            Object mob = activeMob != null && activeMob.isPresent() ? activeMob.get() : null;
            if (mob != null) {
                Object mythicEntity = ReflectionUtil.invoke("getEntity", mob, new Object[0]);
                if (mythicEntity != null) {
                    return (String)ReflectionUtil.invoke("getName", mythicEntity, new Object[0]);
                }
            }
        } catch (NoSuchElementException var8) {
        }

        return null;
    }
}