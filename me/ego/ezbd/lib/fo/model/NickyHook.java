package me.ego.ezbd.lib.fo.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.entity.Player;

class NickyHook {
    NickyHook() {
    }

    String getNick(Player player) {
        Constructor<?> nickConstructor = ReflectionUtil.getConstructor("io.loyloy.nicky.Nick", new Class[]{Player.class});
        Object nick = ReflectionUtil.instantiate(nickConstructor, new Object[]{player});
        String nickname = (String)ReflectionUtil.invoke("get", nick, new Object[0]);
        if (nickname != null) {
            Method formatMethod = ReflectionUtil.getMethod(nick.getClass(), "format", new Class[]{String.class});
            if (formatMethod != null) {
                nickname = (String)ReflectionUtil.invoke(formatMethod, nick, new Object[]{nickname});
            }
        }

        return nickname != null && !nickname.isEmpty() ? nickname : null;
    }
}