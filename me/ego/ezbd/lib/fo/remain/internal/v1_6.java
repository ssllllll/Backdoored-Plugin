package me.ego.ezbd.lib.fo.remain.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

class v1_6 extends EnderDragonEntity {
    private static final Integer EntityID = 6000;

    public v1_6(String name, Location loc) {
        super(name, loc);
    }

    public Object getSpawnPacket() {
        Class<?> mob_class = ReflectionUtil.getNMSClass("Packet24MobSpawn");
        Object mobPacket = null;

        try {
            mobPacket = mob_class.newInstance();
            Field a = ReflectionUtil.getDeclaredField(mob_class, "a");
            a.setAccessible(true);
            a.set(mobPacket, EntityID);
            Field b = ReflectionUtil.getDeclaredField(mob_class, "b");
            b.setAccessible(true);
            b.set(mobPacket, EntityType.ENDER_DRAGON.getTypeId());
            Field c = ReflectionUtil.getDeclaredField(mob_class, "c");
            c.setAccessible(true);
            c.set(mobPacket, this.getX());
            Field d = ReflectionUtil.getDeclaredField(mob_class, "d");
            d.setAccessible(true);
            d.set(mobPacket, this.getY());
            Field e = ReflectionUtil.getDeclaredField(mob_class, "e");
            e.setAccessible(true);
            e.set(mobPacket, this.getZ());
            Field f = ReflectionUtil.getDeclaredField(mob_class, "f");
            f.setAccessible(true);
            f.set(mobPacket, (byte)((int)((float)this.getPitch() * 256.0F / 360.0F)));
            Field g = ReflectionUtil.getDeclaredField(mob_class, "g");
            g.setAccessible(true);
            g.set(mobPacket, (byte)0);
            Field h = ReflectionUtil.getDeclaredField(mob_class, "h");
            h.setAccessible(true);
            h.set(mobPacket, (byte)((int)((float)this.getYaw() * 256.0F / 360.0F)));
            Field i = ReflectionUtil.getDeclaredField(mob_class, "i");
            i.setAccessible(true);
            i.set(mobPacket, this.getXvel());
            Field j = ReflectionUtil.getDeclaredField(mob_class, "j");
            j.setAccessible(true);
            j.set(mobPacket, this.getYvel());
            Field k = ReflectionUtil.getDeclaredField(mob_class, "k");
            k.setAccessible(true);
            k.set(mobPacket, this.getZvel());
            Object watcher = this.getWatcher();
            Field t = ReflectionUtil.getDeclaredField(mob_class, "t");
            t.setAccessible(true);
            t.set(mobPacket, watcher);
        } catch (ReflectiveOperationException var16) {
            var16.printStackTrace();
        }

        return mobPacket;
    }

    public Object getDestroyPacket() {
        Class<?> packet_class = ReflectionUtil.getNMSClass("Packet29DestroyEntity");
        Object packet = null;

        try {
            packet = packet_class.newInstance();
            Field a = ReflectionUtil.getDeclaredField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, new int[]{EntityID});
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
        }

        return packet;
    }

    public Object getMetaPacket(Object watcher) {
        Class<?> packet_class = ReflectionUtil.getNMSClass("Packet40EntityMetadata");
        Object packet = null;

        try {
            packet = packet_class.newInstance();
            Field a = ReflectionUtil.getDeclaredField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, EntityID);
            Method watcher_c = ReflectionUtil.getMethod(watcher.getClass(), "c");
            Field b = ReflectionUtil.getDeclaredField(packet_class, "b");
            b.setAccessible(true);
            b.set(packet, watcher_c.invoke(watcher));
        } catch (ReflectiveOperationException var7) {
            var7.printStackTrace();
        }

        return packet;
    }

    public Object getTeleportPacket(Location loc) {
        Class<?> packet_class = ReflectionUtil.getNMSClass("Packet34EntityTeleport");
        Object packet = null;

        try {
            packet = packet_class.newInstance();
            Field a = ReflectionUtil.getDeclaredField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, EntityID);
            Field b = ReflectionUtil.getDeclaredField(packet_class, "b");
            b.setAccessible(true);
            b.set(packet, (int)Math.floor(loc.getX() * 32.0D));
            Field c = ReflectionUtil.getDeclaredField(packet_class, "c");
            c.setAccessible(true);
            c.set(packet, (int)Math.floor(loc.getY() * 32.0D));
            Field d = ReflectionUtil.getDeclaredField(packet_class, "d");
            d.setAccessible(true);
            d.set(packet, (int)Math.floor(loc.getZ() * 32.0D));
            Field e = ReflectionUtil.getDeclaredField(packet_class, "e");
            e.setAccessible(true);
            e.set(packet, (byte)((int)(loc.getYaw() * 256.0F / 360.0F)));
            Field f = ReflectionUtil.getDeclaredField(packet_class, "f");
            f.setAccessible(true);
            f.set(packet, (byte)((int)(loc.getPitch() * 256.0F / 360.0F)));
        } catch (ReflectiveOperationException var10) {
            var10.printStackTrace();
        }

        return packet;
    }

    public Object getWatcher() {
        Class<?> watcher_class = ReflectionUtil.getNMSClass("DataWatcher");
        Object watcher = null;

        try {
            watcher = watcher_class.newInstance();
            Method a = ReflectionUtil.getMethod(watcher_class, "a", new Class[]{Integer.TYPE, Object.class});
            a.setAccessible(true);
            a.invoke(watcher, 0, Byte.valueOf((byte)(this.isVisible() ? 0 : 32)));
            a.invoke(watcher, 6, this.health);
            a.invoke(watcher, 7, 0);
            a.invoke(watcher, 8, 0);
            a.invoke(watcher, 10, this.name);
            a.invoke(watcher, 11, 1);
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
        }

        return watcher;
    }
}