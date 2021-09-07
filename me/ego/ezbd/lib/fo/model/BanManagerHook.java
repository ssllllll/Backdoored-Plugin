package me.ego.ezbd.lib.fo.model;

import java.lang.reflect.Method;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.entity.Player;

class BanManagerHook {
    BanManagerHook() {
    }

    boolean isMuted(Player player) {
        try {
            Class<?> api = ReflectionUtil.lookupClass("me.confuser.banmanager.common.api.BmAPI");
            Method isMuted = ReflectionUtil.getMethod(api, "isMuted", new Class[]{UUID.class});
            return (Boolean)ReflectionUtil.invoke(isMuted, (Object)null, new Object[]{player.getUniqueId()});
        } catch (Throwable var4) {
            if (!var4.toString().contains("Could not find class")) {
                Common.log(new String[]{"Unable to check if " + player.getName() + " is muted at BanManager. Is the API hook outdated? Got: " + var4});
            }

            return false;
        }
    }
}