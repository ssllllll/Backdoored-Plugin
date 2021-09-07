package me.ego.ezbd;

import me.ego.ezbd.commands.EZBDCommandGroup;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;

public class EZBDPlugin extends SimplePlugin {
    public EZBDPlugin() {
    }

    protected void onPluginStart() {
        Filter.inject();
        this.registerCommands("ezbd", new EZBDCommandGroup());
    }
}
