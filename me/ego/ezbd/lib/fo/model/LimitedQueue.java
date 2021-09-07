package me.ego.ezbd.lib.fo.model;

import com.google.common.collect.ForwardingQueue;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

public final class LimitedQueue<E> extends ForwardingQueue<E> {
    private final Queue<E> delegate;
    private final int capacity;

    public LimitedQueue(int capacity) {
        this.delegate = new ArrayDeque(capacity);
        this.capacity = capacity;
    }

    protected Queue<E> delegate() {
        return this.delegate;
    }

    public boolean add(E element) {
        if (this.size() >= this.capacity) {
            this.delegate.poll();
        }

        return this.delegate.add(element);
    }

    public boolean addAll(Collection<? extends E> collection) {
        return this.standardAddAll(collection);
    }

    public boolean offer(E o) {
        return this.standardOffer(o);
    }
}