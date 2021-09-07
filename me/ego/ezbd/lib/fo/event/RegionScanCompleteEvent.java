package me.ego.ezbd.lib.fo.event;

import org.bukkit.World;
import org.bukkit.event.HandlerList;

public final class RegionScanCompleteEvent extends SimpleEvent {
    private static final HandlerList handlers = new HandlerList();
    private final World world;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public World getWorld() {
        return this.world;
    }

    public RegionScanCompleteEvent(World world) {
        this.world = world;
    }
}
