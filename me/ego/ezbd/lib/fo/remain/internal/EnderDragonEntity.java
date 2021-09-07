package me.ego.ezbd.lib.fo.remain.internal;

import me.ego.ezbd.lib.fo.remain.CompBarColor;
import me.ego.ezbd.lib.fo.remain.CompBarStyle;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Location;

abstract class EnderDragonEntity {
    private float maxHealth = 200.0F;
    private int x;
    private int y;
    private int z;
    private int pitch = 0;
    private int yaw = 0;
    private byte xvel = 0;
    private byte yvel = 0;
    private byte zvel = 0;
    public float health = 0.0F;
    private boolean visible = false;
    public String name;
    private Object world;
    protected CompBarColor barColor;
    protected CompBarStyle barStyle;

    EnderDragonEntity(String name, Location loc, int percent) {
        this.name = name;
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.health = (float)percent / 100.0F * this.maxHealth;
        this.world = Remain.getHandleWorld(loc.getWorld());
    }

    EnderDragonEntity(String name, Location loc) {
        this.name = name;
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.world = Remain.getHandleWorld(loc.getWorld());
    }

    public void setHealth(int percent) {
        this.health = (float)percent / 100.0F * this.maxHealth;
    }

    public abstract Object getSpawnPacket();

    public abstract Object getDestroyPacket();

    public abstract Object getMetaPacket(Object var1);

    public abstract Object getTeleportPacket(Location var1);

    public abstract Object getWatcher();

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public int getPitch() {
        return this.pitch;
    }

    public int getYaw() {
        return this.yaw;
    }

    public byte getXvel() {
        return this.xvel;
    }

    public byte getYvel() {
        return this.yvel;
    }

    public byte getZvel() {
        return this.zvel;
    }

    public float getHealth() {
        return this.health;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public String getName() {
        return this.name;
    }

    public Object getWorld() {
        return this.world;
    }

    public CompBarColor getBarColor() {
        return this.barColor;
    }

    public CompBarStyle getBarStyle() {
        return this.barStyle;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public void setXvel(byte xvel) {
        this.xvel = xvel;
    }

    public void setYvel(byte yvel) {
        this.yvel = yvel;
    }

    public void setZvel(byte zvel) {
        this.zvel = zvel;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorld(Object world) {
        this.world = world;
    }

    public void setBarColor(CompBarColor barColor) {
        this.barColor = barColor;
    }

    public void setBarStyle(CompBarStyle barStyle) {
        this.barStyle = barStyle;
    }
}