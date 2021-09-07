package me.ego.ezbd.lib.fo.model;

import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

class LocketteProHook {
    LocketteProHook() {
    }

    boolean isOwner(Block block, Player player) {
        Class<?> locketteProAPI = ReflectionUtil.lookupClass("me.crafter.mc.lockettepro.LocketteProAPI");
        Method isProtected = ReflectionUtil.getMethod(locketteProAPI, "isProtected", new Class[]{Block.class});
        Method isOwner = ReflectionUtil.getMethod(locketteProAPI, "isOwner", new Class[]{Block.class, Player.class});
        return (Boolean)ReflectionUtil.invoke(isProtected, (Object)null, new Object[]{block}) ? (Boolean)ReflectionUtil.invoke(isOwner, (Object)null, new Object[]{block, player}) : false;
    }
}