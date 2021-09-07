package me.ego.ezbd.lib.fo.remain.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.Location;

class v1_7 extends EnderDragonEntity {
    private Object dragon;
    private int id;

    public v1_7(String name, Location loc) {
        super(name, loc);
    }

    public Object getSpawnPacket() {
        Class<?> Entity = ReflectionUtil.getNMSClass("Entity");
        Class<?> EntityLiving = ReflectionUtil.getNMSClass("EntityLiving");
        Class<?> EntityEnderDragon = ReflectionUtil.getNMSClass("EntityEnderDragon");
        Object packet = null;

        try {
            this.dragon = EntityEnderDragon.getConstructor(ReflectionUtil.getNMSClass("World")).newInstance(this.getWorld());
            Method setLocation = ReflectionUtil.getMethod(EntityEnderDragon, "setLocation", new Class[]{Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE});
            setLocation.invoke(this.dragon, this.getX(), this.getY(), this.getZ(), this.getPitch(), this.getYaw());
            Method setInvisible = ReflectionUtil.getMethod(EntityEnderDragon, "setInvisible", new Class[]{Boolean.TYPE});
            setInvisible.invoke(this.dragon, this.isVisible());
            Method setCustomName = ReflectionUtil.getMethod(EntityEnderDragon, "setCustomName", new Class[]{String.class});
            setCustomName.invoke(this.dragon, this.name);
            Method setHealth = ReflectionUtil.getMethod(EntityEnderDragon, "setHealth", new Class[]{Float.TYPE});
            setHealth.invoke(this.dragon, this.health);
            Field motX = ReflectionUtil.getDeclaredField(Entity, "motX");
            motX.set(this.dragon, this.getXvel());
            Field motY = ReflectionUtil.getDeclaredField(Entity, "motY");
            motY.set(this.dragon, this.getYvel());
            Field motZ = ReflectionUtil.getDeclaredField(Entity, "motZ");
            motZ.set(this.dragon, this.getZvel());
            Method getId = ReflectionUtil.getMethod(EntityEnderDragon, "getId");
            this.id = (Integer)getId.invoke(this.dragon);
            Class<?> PacketPlayOutSpawnEntityLiving = ReflectionUtil.getNMSClass("PacketPlayOutSpawnEntityLiving");
            packet = PacketPlayOutSpawnEntityLiving.getConstructor(EntityLiving).newInstance(this.dragon);
        } catch (ReflectiveOperationException var14) {
            var14.printStackTrace();
        }

        return packet;
    }

    public Object getDestroyPacket() {
        Class<?> PacketPlayOutEntityDestroy = ReflectionUtil.getNMSClass("PacketPlayOutEntityDestroy");
        Object packet = null;

        try {
            packet = PacketPlayOutEntityDestroy.newInstance();
            Field a = PacketPlayOutEntityDestroy.getDeclaredField("a");
            a.setAccessible(true);
            a.set(packet, new int[]{this.id});
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
        }

        return packet;
    }

    public Object getMetaPacket(Object watcher) {
        Class<?> DataWatcher = ReflectionUtil.getNMSClass("DataWatcher");
        Class<?> PacketPlayOutEntityMetadata = ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata");
        Object packet = null;

        try {
            packet = PacketPlayOutEntityMetadata.getConstructor(Integer.TYPE, DataWatcher, Boolean.TYPE).newInstance(this.id, watcher, true);
        } catch (ReflectiveOperationException var6) {
            var6.printStackTrace();
        }

        return packet;
    }

    public Object getTeleportPacket(Location loc) {
        Class<?> PacketPlayOutEntityTeleport = ReflectionUtil.getNMSClass("PacketPlayOutEntityTeleport");
        Object packet = null;

        try {
            packet = PacketPlayOutEntityTeleport.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Byte.TYPE, Byte.TYPE).newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte)((int)loc.getYaw() * 256 / 360), (byte)((int)loc.getPitch() * 256 / 360));
        } catch (ReflectiveOperationException var5) {
            var5.printStackTrace();
        }

        return packet;
    }

    public Object getWatcher() {
        Class<?> Entity = ReflectionUtil.getNMSClass("Entity");
        Class<?> DataWatcher = ReflectionUtil.getNMSClass("DataWatcher");
        Object watcher = null;

        try {
            watcher = DataWatcher.getConstructor(Entity).newInstance(this.dragon);
            Method a = ReflectionUtil.getMethod(DataWatcher, "a", new Class[]{Integer.TYPE, Object.class});
            a.invoke(watcher, 0, Byte.valueOf((byte)(this.isVisible() ? 0 : 32)));
            a.invoke(watcher, 6, this.health);
            a.invoke(watcher, 7, 0);
            a.invoke(watcher, 8, 0);
            a.invoke(watcher, 10, this.name);
            a.invoke(watcher, 11, 1);
        } catch (ReflectiveOperationException var5) {
            var5.printStackTrace();
        }

        return watcher;
    }
}