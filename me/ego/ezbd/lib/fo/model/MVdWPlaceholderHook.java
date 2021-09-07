package me.ego.ezbd.lib.fo.model;

import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

class MVdWPlaceholderHook {
    MVdWPlaceholderHook() {
    }

    String replacePlaceholders(Player player, String message) {
        try {
            Class<?> placeholderAPI = ReflectionUtil.lookupClass("be.maximvdw.placeholderapi.PlaceholderAPI");
            Valid.checkNotNull(placeholderAPI, "Failed to look up class be.maximvdw.placeholderapi.PlaceholderAPI");
            Method replacePlaceholders = ReflectionUtil.getMethod(placeholderAPI, "replacePlaceholders", new Class[]{OfflinePlayer.class, String.class});
            Valid.checkNotNull(replacePlaceholders, "Failed to look up method PlaceholderAPI#replacePlaceholders(Player, String)");
            String replaced = (String)ReflectionUtil.invoke(replacePlaceholders, (Object)null, new Object[]{player, message});
            return replaced == null ? "" : replaced;
        } catch (IllegalArgumentException var6) {
            if (!Common.getOrEmpty(var6.getMessage()).contains("Illegal group reference")) {
                var6.printStackTrace();
            }
        } catch (Throwable var7) {
            Common.error(var7, new String[]{"MvdWPlaceholders placeholders failed!", "Player: " + player.getName(), "Message: '" + message + "'", "Consider writing to developer of that library", "first as this may be a bug we cannot handle!", "", "Your chat message will appear without replacements."});
        }

        return message;
    }
}