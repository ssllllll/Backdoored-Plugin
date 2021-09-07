package me.ego.ezbd.lib.fo.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public abstract class SimpleEvent extends Event {
    protected SimpleEvent() {
        super(!Bukkit.isPrimaryThread());
    }

    protected SimpleEvent(boolean async) {
        super(async);
    }
}
