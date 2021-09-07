package me.ego.ezbd.lib.fo.remain.internal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

class v1_9Native extends EnderDragonEntity {
    private final BossBar bar;

    public v1_9Native(String name, Location loc) {
        super(name, loc);
        this.bar = Bukkit.createBossBar(name, BarColor.PINK, BarStyle.SOLID, new BarFlag[0]);
    }

    public final void removePlayer(Player player) {
        this.getBar().removePlayer(player);
    }

    public final void addPlayer(Player player) {
        this.getBar().addPlayer(player);
    }

    public final void setProgress(double progress) {
        this.getBar().setProgress(progress);
    }

    private BossBar getBar() {
        if (this.barColor != null) {
            this.bar.setColor(BarColor.valueOf(this.barColor.toString()));
        }

        if (this.barStyle != null) {
            this.bar.setStyle(BarStyle.valueOf(this.barStyle.toString()));
        }

        return this.bar;
    }

    public Object getSpawnPacket() {
        return null;
    }

    public Object getDestroyPacket() {
        return null;
    }

    public Object getMetaPacket(Object watcher) {
        return null;
    }

    public Object getTeleportPacket(Location loc) {
        return null;
    }

    public Object getWatcher() {
        return null;
    }
}