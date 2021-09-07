package me.ego.ezbd.lib.fo.event;

import me.ego.ezbd.lib.fo.menu.tool.Rocket;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class RocketExplosionEvent extends SimpleEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Rocket rocket;
    private final Projectile projectile;
    private float power;
    private boolean breakBlocks;
    private boolean cancelled;

    public RocketExplosionEvent(Rocket rocket, Projectile projectile, float power, boolean breakBlocks) {
        this.rocket = rocket;
        this.projectile = projectile;
        this.power = power;
        this.breakBlocks = breakBlocks;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Rocket getRocket() {
        return this.rocket;
    }

    public Projectile getProjectile() {
        return this.projectile;
    }

    public float getPower() {
        return this.power;
    }

    public boolean isBreakBlocks() {
        return this.breakBlocks;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public void setBreakBlocks(boolean breakBlocks) {
        this.breakBlocks = breakBlocks;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
