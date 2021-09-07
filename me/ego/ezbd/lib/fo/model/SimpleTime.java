package me.ego.ezbd.lib.fo.model;

import lombok.NonNull;
import me.ego.ezbd.lib.fo.TimeUtil;

public class SimpleTime {
    private final String raw;
    private final long timeTicks;

    protected SimpleTime(@NonNull String time) {
        if (time == null) {
            throw new NullPointerException("time is marked non-null but is null");
        } else {
            if (!"0".equals(time) && !"none".equalsIgnoreCase(time)) {
                this.raw = time;
                this.timeTicks = TimeUtil.toTicks(time);
            } else {
                this.raw = "0";
                this.timeTicks = 0L;
            }

        }
    }

    public static SimpleTime fromSeconds(int seconds) {
        return from(seconds + " seconds");
    }

    public static SimpleTime from(String time) {
        return new SimpleTime(time);
    }

    public long getTimeSeconds() {
        return this.timeTicks / 20L;
    }

    public int getTimeTicks() {
        return (int)this.timeTicks;
    }

    public String getRaw() {
        return this.timeTicks == 0L ? "0" : this.raw;
    }

    public String toString() {
        return this.raw;
    }
}