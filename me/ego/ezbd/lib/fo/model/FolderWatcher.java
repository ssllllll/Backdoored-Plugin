package me.ego.ezbd.lib.fo.model;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class FolderWatcher extends Thread {
    private static volatile Set<FolderWatcher> activeThreads = new HashSet();
    private final Map<String, BukkitTask> scheduledUpdates = new HashMap();
    private final Path folder;
    private boolean watching = true;

    public static void stopThreads() {
        Iterator var0 = activeThreads.iterator();

        while(var0.hasNext()) {
            FolderWatcher thread = (FolderWatcher)var0.next();
            thread.stopWatching();
        }

        activeThreads.clear();
    }

    public FolderWatcher(File folder) {
        Valid.checkBoolean(folder.exists(), folder + " does not exists!", new Object[0]);
        Valid.checkBoolean(folder.isDirectory(), folder + " must be a directory!", new Object[0]);
        this.folder = folder.toPath();
        this.start();
        Iterator var2 = activeThreads.iterator();

        while(var2.hasNext()) {
            FolderWatcher other = (FolderWatcher)var2.next();
            if (other.folder.toString().equals(this.folder.toString())) {
                Common.log(new String[]{"&cWarning: &fA duplicate file watcher for '" + folder.getPath() + "' was added. This is untested and may causes fatal issues!"});
            }
        }

        activeThreads.add(this);
        Debugger.debug("upload", new String[]{"Started folder watcher for " + folder + " in " + folder.getAbsolutePath() + " (path: " + this.folder + ")"});
    }

    public final void run() {
        FileSystem fileSystem = this.folder.getFileSystem();

        try {
            WatchService service = fileSystem.newWatchService();
            Throwable var3 = null;

            try {
                WatchKey registration = this.folder.register(service, StandardWatchEventKinds.ENTRY_MODIFY);

                while(this.watching) {
                    try {
                        WatchKey watchKey = service.take();
                        Iterator var6 = watchKey.pollEvents().iterator();

                        while(var6.hasNext()) {
                            WatchEvent<?> watchEvent = (WatchEvent)var6.next();
                            Kind<?> kind = watchEvent.kind();
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                Path watchEventPath = (Path)watchEvent.context();
                                File fileModified = new File(SimplePlugin.getData(), watchEventPath.toFile().getName());
                                String path = fileModified.getAbsolutePath();
                                BukkitTask pendingTask = (BukkitTask)this.scheduledUpdates.remove(path);
                                if (pendingTask != null) {
                                    pendingTask.cancel();
                                }

                                this.scheduledUpdates.put(path, Common.runLater(10, () -> {
                                    if (this.watching) {
                                        try {
                                            this.onModified(fileModified);
                                            this.scheduledUpdates.remove(path);
                                        } catch (Throwable var4) {
                                            Common.error(var4, new String[]{"Error in calling onModified when watching changed file " + fileModified});
                                        }

                                    }
                                }));
                                break;
                            }
                        }

                        if (!watchKey.reset()) {
                            Common.error(new FoException("Failed to reset watch key! Restarting sync engine.."), new String[0]);
                        }
                    } catch (Throwable var22) {
                        Common.error(var22, new String[]{"Error in handling watching thread loop for folder " + this.getFolder()});
                    }
                }

                registration.cancel();
            } catch (Throwable var23) {
                var3 = var23;
                throw var23;
            } finally {
                if (service != null) {
                    if (var3 != null) {
                        try {
                            service.close();
                        } catch (Throwable var21) {
                            var3.addSuppressed(var21);
                        }
                    } else {
                        service.close();
                    }
                }

            }
        } catch (Throwable var25) {
            Common.error(var25, new String[]{"Error in initializing watching thread loop for folder " + this.getFolder()});
        }

    }

    protected abstract void onModified(File var1);

    public void stopWatching() {
        Valid.checkBoolean(this.watching, "The folder watcher for folder " + this.folder + " is no longer watching!", new Object[0]);
        this.watching = false;
        Iterator var1 = this.scheduledUpdates.values().iterator();

        while(var1.hasNext()) {
            BukkitTask task = (BukkitTask)var1.next();

            try {
                task.cancel();
            } catch (Exception var4) {
            }
        }

    }

    public boolean equals(Object obj) {
        return obj instanceof FolderWatcher && ((FolderWatcher)obj).folder.toString().equals(this.folder.toString());
    }

    protected Map<String, BukkitTask> getScheduledUpdates() {
        return this.scheduledUpdates;
    }

    protected Path getFolder() {
        return this.folder;
    }

    public boolean isWatching() {
        return this.watching;
    }
}