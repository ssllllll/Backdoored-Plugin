package me.ego.ezbd.lib.fo.constants;

import me.ego.ezbd.lib.fo.command.annotation.Permission;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;

public class FoPermissions {
    @Permission("Receive plugin update notifications on join.")
    public static final String NOTIFY_UPDATE = SimplePlugin.getNamed().toLowerCase() + ".notify.update";

    public FoPermissions() {
    }
}