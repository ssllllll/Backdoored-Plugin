package me.ego.ezbd.lib.fo.model;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.ReflectionUtil.ReflectionException;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Bukkit;

class RegionAccessor {
    private static Constructor<?> regionFileConstructor;
    private static Method isChunkSaved;
    private static boolean atleast1_13;
    private static boolean atleast1_14;
    private static boolean atleast1_15;
    private static boolean atleast1_16;
    private static final String saveMethodName;

    RegionAccessor() {
    }

    static Object getRegionFile(String worldName, File file) {
        try {
            File container = new File(Bukkit.getWorldContainer(), worldName);
            return atleast1_16 ? regionFileConstructor.newInstance(file, container, true) : (atleast1_15 ? regionFileConstructor.newInstance(file, container) : regionFileConstructor.newInstance(file));
        } catch (Throwable var3) {
            throw new RuntimeException("Could not create region file from " + file, var3);
        }
    }

    static boolean isChunkSaved(Object region, int x, int z) {
        try {
            if (MinecraftVersion.newerThan(V.v1_13)) {
                Object chunkCoordinates = ReflectionUtil.getNMSClass("ChunkCoordIntPair").getConstructor(Integer.TYPE, Integer.TYPE).newInstance(x, z);
                return (Boolean)isChunkSaved.invoke(region, chunkCoordinates);
            } else {
                return (Boolean)isChunkSaved.invoke(region, x, z);
            }
        } catch (ReflectiveOperationException var4) {
            throw new ReflectionException("Could not find if region file " + region + " has chunk at " + x + " " + z, var4);
        }
    }

    static void save(Object region) {
        try {
            region.getClass().getDeclaredMethod(saveMethodName).invoke(region);
        } catch (ReflectiveOperationException var2) {
            throw new ReflectionException("Error saving region " + region, var2);
        }
    }

    static {
        atleast1_13 = MinecraftVersion.atLeast(V.v1_13);
        atleast1_14 = MinecraftVersion.atLeast(V.v1_14);
        atleast1_15 = MinecraftVersion.atLeast(V.v1_15);
        atleast1_16 = MinecraftVersion.atLeast(V.v1_16);
        saveMethodName = atleast1_13 ? "close" : "c";

        try {
            Class<?> regionFileClass = ReflectionUtil.getNMSClass("RegionFile");
            regionFileConstructor = atleast1_16 ? regionFileClass.getConstructor(File.class, File.class, Boolean.TYPE) : (atleast1_15 ? regionFileClass.getConstructor(File.class, File.class) : regionFileClass.getConstructor(File.class));
            isChunkSaved = atleast1_14 ? regionFileClass.getMethod("b", ReflectionUtil.getNMSClass("ChunkCoordIntPair")) : regionFileClass.getMethod(atleast1_13 ? "b" : "c", Integer.TYPE, Integer.TYPE);
        } catch (ReflectiveOperationException var1) {
            Remain.sneaky(var1);
        }

    }
}