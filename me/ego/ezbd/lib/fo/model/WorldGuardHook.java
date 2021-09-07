package me.ego.ezbd.lib.fo.model;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

class WorldGuardHook {
    private final boolean legacy;

    public WorldGuardHook(WorldEditHook we) {
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        this.legacy = !wg.getDescription().getVersion().startsWith("7") || we != null && we.legacy;
    }

    public List<String> getRegionsAt(Location loc) {
        List<String> list = new ArrayList();
        this.getApplicableRegions(loc).forEach((reg) -> {
            String name = Common.stripColors(reg.getId());
            if (!name.startsWith("__")) {
                list.add(name);
            }

        });
        return list;
    }

    public Region getRegion(String name) {
        Iterator var2 = Bukkit.getWorlds().iterator();

        while(true) {
            while(var2.hasNext()) {
                World w = (World)var2.next();
                Object rm = this.getRegionManager(w);
                if (this.legacy) {
                    try {
                        Map<?, ?> regionMap = (Map)rm.getClass().getMethod("getRegions").invoke(rm);
                        Iterator var21 = regionMap.values().iterator();

                        while(var21.hasNext()) {
                            Object regObj = var21.next();
                            if (regObj != null && Common.stripColors(((ProtectedRegion)regObj).getId()).equals(name)) {
                                Class<?> clazz = regObj.getClass();
                                Method getMax = clazz.getMethod("getMaximumPoint");
                                Method getMin = clazz.getMethod("getMinimumPoint");
                                Object regMax = getMax.invoke(regObj);
                                Object regMin = getMin.invoke(regObj);
                                Class<?> vectorClass = Class.forName("com.sk89q.worldedit.BlockVector");
                                Method getX = vectorClass.getMethod("getX");
                                Method getY = vectorClass.getMethod("getY");
                                Method getZ = vectorClass.getMethod("getZ");
                                Location locMax = new Location(w, (Double)getX.invoke(regMax), (Double)getY.invoke(regMax), (Double)getZ.invoke(regMax));
                                Location locMin = new Location(w, (Double)getX.invoke(regMin), (Double)getY.invoke(regMin), (Double)getZ.invoke(regMin));
                                return new Region(name, locMin, locMax);
                            }
                        }
                    } catch (Throwable var19) {
                        var19.printStackTrace();
                        throw new FoException("Failed WorldEdit 6 legacy hook, see above & report");
                    }
                } else {
                    Iterator var5 = ((RegionManager)rm).getRegions().values().iterator();

                    while(var5.hasNext()) {
                        ProtectedRegion reg = (ProtectedRegion)var5.next();
                        if (reg != null && reg.getId() != null && Common.stripColors(reg.getId()).equals(name)) {
                            BlockVector3 regMax = reg.getMaximumPoint();
                            BlockVector3 regMin = reg.getMinimumPoint();
                            Location locMax = new Location(w, (double)regMax.getX(), (double)regMax.getY(), (double)regMax.getZ());
                            Location locMin = new Location(w, (double)regMin.getX(), (double)regMin.getY(), (double)regMin.getZ());
                            return new Region(name, locMin, locMax);
                        }
                    }
                }
            }

            return null;
        }
    }

    public List<String> getAllRegions() {
        List<String> list = new ArrayList();
        Iterator var2 = Bukkit.getWorlds().iterator();

        while(true) {
            while(var2.hasNext()) {
                World w = (World)var2.next();
                Object rm = this.getRegionManager(w);
                if (this.legacy) {
                    try {
                        Map<?, ?> regionMap = (Map)rm.getClass().getMethod("getRegions").invoke(rm);
                        Method getId = null;
                        Iterator var7 = regionMap.values().iterator();

                        while(var7.hasNext()) {
                            Object regObj = var7.next();
                            if (regObj != null) {
                                if (getId == null) {
                                    getId = regObj.getClass().getMethod("getId");
                                }

                                String name = Common.stripColors(getId.invoke(regObj).toString());
                                if (!name.startsWith("__")) {
                                    list.add(name);
                                }
                            }
                        }
                    } catch (Throwable var10) {
                        var10.printStackTrace();
                        throw new FoException("Failed WorldEdit 6 legacy hook, see above & report");
                    }
                } else {
                    ((RegionManager)rm).getRegions().values().forEach((reg) -> {
                        if (reg != null && reg.getId() != null) {
                            String name = Common.stripColors(reg.getId());
                            if (!name.startsWith("__")) {
                                list.add(name);
                            }

                        }
                    });
                }
            }

            return list;
        }
    }

    private Iterable<ProtectedRegion> getApplicableRegions(Location loc) {
        Object rm = this.getRegionManager(loc.getWorld());
        if (this.legacy) {
            try {
                return (Iterable)rm.getClass().getMethod("getApplicableRegions", Location.class).invoke(rm, loc);
            } catch (Throwable var4) {
                var4.printStackTrace();
                throw new FoException("Failed WorldEdit 6 legacy hook, see above & report");
            }
        } else {
            return ((RegionManager)rm).getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
        }
    }

    private Object getRegionManager(World w) {
        if (this.legacy) {
            try {
                return Class.forName("com.sk89q.worldguard.bukkit.WGBukkit").getMethod("getRegionManager", World.class).invoke((Object)null, w);
            } catch (Throwable var5) {
                var5.printStackTrace();
                throw new FoException("Failed WorldGuard 6 legacy hook, see above & report");
            }
        } else {
            try {
                Class<?> bwClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitWorld");
                Constructor<?> bwClassNew = bwClass.getConstructor(World.class);
                Object t = Class.forName("com.sk89q.worldguard.WorldGuard").getMethod("getInstance").invoke((Object)null);
                t = t.getClass().getMethod("getPlatform").invoke(t);
                t = t.getClass().getMethod("getRegionContainer").invoke(t);
                return t.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World")).invoke(t, bwClassNew.newInstance(w));
            } catch (Throwable var6) {
                var6.printStackTrace();
                throw new FoException("Failed WorldGuard hook, see above & report");
            }
        }
    }
}