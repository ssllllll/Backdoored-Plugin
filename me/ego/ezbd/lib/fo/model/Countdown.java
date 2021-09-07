package me.ego.ezbd.lib.fo.model;

import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public abstract class Countdown implements Runnable {
    private static final int START_DELAY = 20;
    private static final int TICK_PERIOD = 20;
    private final int countdownSeconds;
    private int secondsSinceStart;
    private int taskId;

    protected Countdown(SimpleTime time) {
        this((int)time.getTimeSeconds());
    }

    protected Countdown(int countdownSeconds) {
        this.secondsSinceStart = 0;
        this.taskId = -1;
        this.countdownSeconds = countdownSeconds;
    }

    public final void run() {
        ++this.secondsSinceStart;
        if (this.secondsSinceStart < this.countdownSeconds) {
            try {
                this.onTick();
            } catch (Throwable var4) {
                Throwable t = var4;

                try {
                    this.onTickError(t);
                } catch (Throwable var3) {
                    Common.log(new String[]{"Unable to handle onTickError, got " + var4 + ": " + var3.getMessage()});
                }

                Common.error(var4, new String[]{"Error in countdown!", "Seconds since start: " + this.secondsSinceStart, "Counting till: " + this.countdownSeconds, "%error"});
            }
        } else {
            this.cancel();
            this.onEnd();
        }

    }

    protected void onStart() {
    }

    protected abstract void onTick();

    protected abstract void onEnd();

    protected void onTickError(Throwable t) {
    }

    public int getTimeLeft() {
        return this.countdownSeconds - this.secondsSinceStart;
    }

    public final void launch() {
        Valid.checkBoolean(!this.isRunning(), "Task " + this + " already scheduled!", new Object[0]);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(SimplePlugin.getInstance(), this, 20L, 20L);
        this.taskId = task.getTaskId();
        this.onStart();
    }

    public final void cancel() {
        Bukkit.getScheduler().cancelTask(this.getTaskId());
        this.taskId = -1;
        this.secondsSinceStart = 0;
    }

    public final boolean isRunning() {
        return this.taskId != -1;
    }

    public final int getTaskId() {
        Valid.checkBoolean(this.isRunning(), "Task " + this + " not scheduled yet", new Object[0]);
        return this.taskId;
    }

    public final String toString() {
        return this.getClass().getSimpleName() + "{" + this.countdownSeconds + ", id=" + this.taskId + "}";
    }

    public int getCountdownSeconds() {
        return this.countdownSeconds;
    }

    protected int getSecondsSinceStart() {
        return this.secondsSinceStart;
    }
}