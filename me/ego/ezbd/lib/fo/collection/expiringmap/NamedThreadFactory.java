package me.ego.ezbd.lib.fo.collection.expiringmap;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String nameFormat;

    public NamedThreadFactory(String nameFormat) {
        this.nameFormat = nameFormat;
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, String.format(this.nameFormat, this.threadNumber.getAndIncrement()));
        thread.setDaemon(true);
        return thread;
    }
}