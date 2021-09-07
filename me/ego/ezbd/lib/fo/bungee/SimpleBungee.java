package me.ego.ezbd.lib.fo.bungee;

import java.lang.reflect.Constructor;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;

public final class SimpleBungee {
    private final String channel;
    private final BungeeListener listener;
    private final BungeeAction[] actions;

    public SimpleBungee(String channel, Class<? extends BungeeListener> listenerClass, Class<? extends BungeeAction> actionEnum) {
        this(channel, toListener(listenerClass), toAction(actionEnum));
    }

    private static BungeeListener toListener(Class<? extends BungeeListener> listenerClass) {
        Valid.checkNotNull(listenerClass);

        try {
            Constructor<?> con = listenerClass.getConstructor();
            con.setAccessible(true);
            return (BungeeListener)con.newInstance();
        } catch (ReflectiveOperationException var2) {
            Common.log(new String[]{"Unable to create new instance of " + listenerClass + ", ensure constructor is public without parameters!"});
            var2.printStackTrace();
            return null;
        }
    }

    private static BungeeAction[] toAction(Class<? extends BungeeAction> actionEnum) {
        Valid.checkNotNull(actionEnum);
        Valid.checkBoolean(actionEnum.isEnum(), "Enum expected, given: " + actionEnum, new Object[0]);

        try {
            return (BungeeAction[])((BungeeAction[])actionEnum.getMethod("values").invoke((Object)null));
        } catch (ReflectiveOperationException var2) {
            Common.log(new String[]{"Unable to get values() of " + actionEnum + ", ensure it is an enum!"});
            var2.printStackTrace();
            return null;
        }
    }

    public SimpleBungee(String channel, BungeeListener listener, BungeeAction... actions) {
        Valid.checkNotNull(channel, "Channel cannot be null!");
        this.channel = channel;
        this.listener = listener;
        Valid.checkNotNull(actions, "Actions cannot be null!");
        this.actions = actions;
    }

    public String getChannel() {
        return this.channel;
    }

    public BungeeListener getListener() {
        return this.listener;
    }

    public BungeeAction[] getActions() {
        return this.actions;
    }
}
