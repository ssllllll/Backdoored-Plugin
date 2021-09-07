package me.ego.ezbd.lib.fo.bungee;

import me.ego.ezbd.lib.fo.plugin.SimplePlugin;

public interface BungeeAction {
    Class<?>[] getContent();

    String name();

    static BungeeAction getByName(String name) {
        BungeeAction[] actions = SimplePlugin.getInstance().getBungeeCord().getActions();
        BungeeAction[] var2 = actions;
        int var3 = actions.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            BungeeAction action = var2[var4];
            if (action.name().equals(name)) {
                return action;
            }
        }

        return null;
    }
}
