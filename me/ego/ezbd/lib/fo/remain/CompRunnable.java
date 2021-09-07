package me.ego.ezbd.lib.fo.remain;

import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.debug.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class CompRunnable implements Runnable {
    private int taskId = -1;

    public CompRunnable() {
    }

    public synchronized void cancel() throws IllegalStateException {
        if (this.taskId != -1) {
            Bukkit.getScheduler().cancelTask(this.getTaskId());
        }

    }

    public synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this));
    }

    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this));
    }

    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, delay));
    }

    public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this, delay));
    }

    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period));
    }

    public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        this.checkState();
        return this.setupId(Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this, delay, period));
    }

    public synchronized int getTaskId() throws IllegalStateException {
        int id = this.taskId;
        if (id == -1) {
            throw new IllegalStateException("Not scheduled yet");
        } else {
            return id;
        }
    }

    private void checkState() {
        if (this.taskId != -1) {
            throw new IllegalStateException("Already scheduled as " + this.taskId);
        }
    }

    private BukkitTask setupId(int taskId) {
        this.taskId = taskId;
        Iterator var2 = Bukkit.getScheduler().getPendingTasks().iterator();

        BukkitTask task;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            task = (BukkitTask)var2.next();
        } while(task.getTaskId() != taskId);

        return task;
    }

    public static final class SafeRunnable implements Runnable {
        private static int scheduledTasks = 0;
        private final Runnable delegate;
        private final List<String> source;
        private final int taskId;

        public SafeRunnable(Runnable delegate) {
            this.delegate = delegate;
            this.source = Debugger.traceRoute(true);
            this.taskId = ++scheduledTasks;
            this.source.remove(0);
            this.source.remove(0);
        }

        public void run() {
            Debugger.debug("runnable", new String[]{"Running task #" + this.taskId + " from " + Common.join(this.source)});

            try {
                this.delegate.run();
            } catch (Exception var2) {
                if (var2 instanceof IllegalStateException && var2.getMessage() != null && (var2.getMessage().contains("Not scheduled yet") || var2.getMessage().contains("Already scheduled"))) {
                    return;
                }

                Common.error(var2, new String[]{"Failed to execute scheduled task: " + var2});
            }

        }

        public Runnable getDelegate() {
            return this.delegate;
        }

        public List<String> getSource() {
            return this.source;
        }

        public int getTaskId() {
            return this.taskId;
        }
    }
}