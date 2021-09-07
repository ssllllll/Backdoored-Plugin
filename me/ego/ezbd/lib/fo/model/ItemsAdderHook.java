package me.ego.ezbd.lib.fo.model;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.entity.Player;

class ItemsAdderHook {
    private final Class<?> itemsAdder = ReflectionUtil.lookupClass("dev.lone.itemsadder.api.FontImages.FontImageWrapper");
    private final Method replaceFontImagesMethod;
    private final Method replaceFontImagesMethodNoPlayer;

    ItemsAdderHook() {
        this.replaceFontImagesMethod = ReflectionUtil.getDeclaredMethod(this.itemsAdder, "replaceFontImages", new Class[]{Player.class, String.class});
        this.replaceFontImagesMethodNoPlayer = ReflectionUtil.getDeclaredMethod(this.itemsAdder, "replaceFontImages", new Class[]{String.class});
    }

    String replaceFontImages(@Nullable Player player, String message) {
        return player == null ? (String)ReflectionUtil.invokeStatic(this.replaceFontImagesMethodNoPlayer, new Object[]{message}) : (String)ReflectionUtil.invokeStatic(this.replaceFontImagesMethod, new Object[]{player, message});
    }
}
