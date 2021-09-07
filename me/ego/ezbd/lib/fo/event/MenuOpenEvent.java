package me.ego.ezbd.lib.fo.event;

import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.menu.model.InventoryDrawer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class MenuOpenEvent extends SimpleEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Menu menu;
    private final InventoryDrawer drawer;
    private final Player player;
    private boolean cancelled;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Menu getMenu() {
        return this.menu;
    }

    public InventoryDrawer getDrawer() {
        return this.drawer;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public MenuOpenEvent(Menu menu, InventoryDrawer drawer, Player player) {
        this.menu = menu;
        this.drawer = drawer;
        this.player = player;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
