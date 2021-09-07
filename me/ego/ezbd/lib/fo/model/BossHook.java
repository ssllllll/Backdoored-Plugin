package me.ego.ezbd.lib.fo.model;

import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.entity.Entity;

class BossHook {
    BossHook() {
    }

    String getBossName(Entity entity) {
        try {
            Class<?> api = ReflectionUtil.lookupClass("org.mineacademy.boss.api.BossAPI");
            Method getBoss = ReflectionUtil.getMethod(api, "getBoss", new Class[]{Entity.class});
            Object boss = ReflectionUtil.invoke(getBoss, (Object)null, new Object[]{entity});
            if (boss != null) {
                Method getName = ReflectionUtil.getMethod(boss.getClass(), "getName");
                return (String)ReflectionUtil.invoke(getName, boss, new Object[0]);
            }
        } catch (Throwable var6) {
            Common.log(new String[]{"Unable to check if " + entity + " is a BOSS. Is the API hook outdated? Got: " + var6});
        }

        return null;
    }
}