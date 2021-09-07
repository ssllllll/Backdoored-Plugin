package me.ego.ezbd.lib.fo.model;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.debug.LagCatcher;
import me.ego.ezbd.lib.fo.event.RegionScanCompleteEvent;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompRunnable;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public abstract class OfflineRegionScanner {
    private static final String[] FOLDERS = new String[]{"region", "DIM-1/region", "DIM1/region"};
    private static final Pattern FILE_PATTERN = Pattern.compile("r\\.(.+)\\.(.+)\\.mca");
    private static int OPERATION_WAIT_SECONDS = 1;
    private int done = 0;
    private int totalFiles = 0;
    private World world;
    private long lastTick = System.currentTimeMillis();

    public OfflineRegionScanner() {
    }

    public final void scan(World world) {
        this.scan0(world);
    }

    private final void scan0(World world) {
        Thread watchdog = null;

        try {
            Field f = Class.forName("org.spigotmc.WatchdogThread").getDeclaredField("instance");

            try {
                f.setAccessible(true);
                watchdog = (Thread)f.get((Object)null);
                watchdog.suspend();
            } catch (Throwable var5) {
                Bukkit.getLogger().severe("FAILED TO DISABLE WATCHDOG, ABORTING! See below and report to us. NO DATA WERE MANIPULATED.");
                Common.callEvent(new RegionScanCompleteEvent(world));
                var5.printStackTrace();
                return;
            }
        } catch (ReflectiveOperationException var6) {
        }

        Bukkit.getLogger().info(Common.consoleLine());
        Bukkit.getLogger().info("Scanning regions in " + world.getName());
        Bukkit.getLogger().info(Common.consoleLine());
        LagCatcher.start("Region scanner for " + world.getName());
        File[] files = getRegionFiles(world);
        if (files == null) {
            Bukkit.getLogger().warning("Unable to locate the region files for: " + world.getName());
        } else {
            Queue<File> queue = new LimitedQueue(files.length + 1);
            queue.addAll(Arrays.asList(files));
            this.totalFiles = files.length;
            this.world = world;
            this.schedule(world.getName(), queue);
            if (watchdog != null) {
                watchdog.resume();
            }

            LagCatcher.end("Region scanner for " + world.getName(), true);
        }
    }

    private final void schedule(final String worldName, final Queue<File> queue) {
        (new CompRunnable() {
            public void run() {
                File file = (File)queue.poll();
                if (file == null) {
                    Bukkit.getLogger().info(Common.consoleLine());
                    Bukkit.getLogger().info("Region scanner finished.");
                    Bukkit.getLogger().info(Common.consoleLine());
                    Common.callEvent(new RegionScanCompleteEvent(OfflineRegionScanner.this.world));
                    OfflineRegionScanner.this.onScanFinished();
                    this.cancel();
                } else {
                    OfflineRegionScanner.this.scanFile(worldName, file);
                    Common.runLater(20 * OfflineRegionScanner.OPERATION_WAIT_SECONDS, () -> {
                        OfflineRegionScanner.this.schedule(worldName, queue);
                    });
                }
            }
        }).runTask(SimplePlugin.getInstance());
    }

    private final void scanFile(String worldName, File file) {
        Matcher matcher = FILE_PATTERN.matcher(file.getName());
        if (matcher.matches()) {
            int regionX = Integer.parseInt(matcher.group(1));
            int regionZ = Integer.parseInt(matcher.group(2));
            System.out.print("[" + Math.round((double)(this.done++) / (double)this.totalFiles * 100.0D) + "%] Processing " + file);
            if (System.currentTimeMillis() - this.lastTick > 4000L) {
                long free = Runtime.getRuntime().freeMemory() / 1000000L;
                if (free < 200L) {
                    System.out.print(" [Low memory (" + free + "Mb)! Running GC and increasing delay between operations ..]");
                    OPERATION_WAIT_SECONDS = 2;
                    System.gc();
                    Common.sleep(5000);
                } else {
                    System.out.print(" [free memory = " + free + " mb]");
                }

                this.lastTick = System.currentTimeMillis();
            }

            System.out.println();
            Object region = RegionAccessor.getRegionFile(worldName, file);

            for(int x = 0; x < 32; ++x) {
                for(int z = 0; z < 32; ++z) {
                    int chunkX = x + (regionX << 5);
                    int chunkZ = z + (regionZ << 5);
                    if (RegionAccessor.isChunkSaved(region, x, z)) {
                        Chunk chunk = this.world.getChunkAt(chunkX, chunkZ);
                        this.onChunkScan(chunk);
                    }
                }
            }

            try {
                RegionAccessor.save(region);
            } catch (Throwable var12) {
                Bukkit.getLogger().severe("Failed to save region " + file + ", operation stopped.");
                Remain.sneaky(var12);
            }

        }
    }

    protected abstract void onChunkScan(Chunk var1);

    protected void onScanFinished() {
    }

    public static File[] getRegionFiles(World world) {
        File regionDir = getRegionDirectory(world);
        return regionDir == null ? null : regionDir.listFiles((dir, name) -> {
            return name.toLowerCase().endsWith(".mca");
        });
    }

    private static final File getRegionDirectory(World world) {
        String[] var1 = FOLDERS;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String f = var1[var3];
            File file = new File(world.getWorldFolder(), f);
            if (file.isDirectory()) {
                return file;
            }
        }

        return null;
    }

    public static int getEstimatedWaitTimeSec(World world) {
        File[] files = getRegionFiles(world);
        return (OPERATION_WAIT_SECONDS + 2) * files.length;
    }
}