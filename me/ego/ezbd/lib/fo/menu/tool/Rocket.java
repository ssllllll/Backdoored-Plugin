package me.ego.ezbd.lib.fo.menu.tool;

import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class Rocket extends Tool {
    private final Class<? extends Projectile> projectile;
    private final float flightSpeed;
    private final float explosionPower;
    private final boolean breakBlocks;

    protected Rocket(Class<? extends Projectile> projectile) {
        this(projectile, 1.5F);
    }

    protected Rocket(Class<? extends Projectile> projectile, float flightSpeed) {
        this(projectile, flightSpeed, 5.0F);
    }

    protected Rocket(Class<? extends Projectile> projectile, float flightSpeed, float explosionPower) {
        this(projectile, flightSpeed, explosionPower, true);
    }

    protected Rocket(Class<? extends Projectile> projectile, float flightSpeed, float explosionPower, boolean breakBlocks) {
        Valid.checkBoolean(flightSpeed <= 10.0F, "Rocket cannot have speed over 10", new Object[0]);
        Valid.checkBoolean(explosionPower <= 30.0F, "Rocket cannot have explosion power over 30", new Object[0]);
        this.projectile = projectile;
        this.flightSpeed = flightSpeed;
        this.explosionPower = explosionPower;
        this.breakBlocks = breakBlocks;
    }

    protected void onBlockClick(PlayerInteractEvent e) {
    }

    protected boolean canLaunch(Player shooter, Location location) {
        return true;
    }

    protected void onLaunch(Projectile projectile, Player shooter) {
    }

    protected void onFlyTick(Projectile projectile, Player shooter) {
    }

    protected boolean canExplode(Projectile projectile, Player shooter) {
        return true;
    }

    protected void onExplode(Projectile projectile, Player shooter) {
    }

    protected boolean ignoreCancelled() {
        return false;
    }

    protected boolean autoCancel() {
        return true;
    }

    public Class<? extends Projectile> getProjectile() {
        return this.projectile;
    }

    public float getFlightSpeed() {
        return this.flightSpeed;
    }

    public float getExplosionPower() {
        return this.explosionPower;
    }

    public boolean isBreakBlocks() {
        return this.breakBlocks;
    }
}