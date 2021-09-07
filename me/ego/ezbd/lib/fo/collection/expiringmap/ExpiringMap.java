package me.ego.ezbd.lib.fo.collection.expiringmap;

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Valid;

public final class ExpiringMap<K, V> implements ConcurrentMap<K, V> {
    static volatile ScheduledExecutorService EXPIRER;
    static volatile ThreadPoolExecutor LISTENER_SERVICE;
    static ThreadFactory THREAD_FACTORY;
    List<ExpiringMap.ExpirationListener<K, V>> expirationListeners;
    List<ExpiringMap.ExpirationListener<K, V>> asyncExpirationListeners;
    private final AtomicLong expirationNanos;
    private int maxSize;
    private final AtomicReference<ExpirationPolicy> expirationPolicy;
    private final EntryLoader<? super K, ? extends V> entryLoader;
    private final ExpiringEntryLoader<? super K, ? extends V> expiringEntryLoader;
    private final ReadWriteLock readWriteLock;
    private final Lock readLock;
    private final Lock writeLock;
    private final ExpiringMap.EntryMap<K, V> entries;
    private final boolean variableExpiration;

    public static void setThreadFactory(@NonNull ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory is marked non-null but is null");
        } else {
            THREAD_FACTORY = threadFactory;
        }
    }

    private ExpiringMap(ExpiringMap.Builder<K, V> builder) {
        this.readWriteLock = new ReentrantReadWriteLock();
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
        Class var2;
        if (EXPIRER == null) {
            var2 = ExpiringMap.class;
            synchronized(ExpiringMap.class) {
                if (EXPIRER == null) {
                    EXPIRER = Executors.newSingleThreadScheduledExecutor((ThreadFactory)(THREAD_FACTORY == null ? new NamedThreadFactory("ExpiringMap-Expirer") : THREAD_FACTORY));
                }
            }
        }

        if (LISTENER_SERVICE == null && builder.asyncExpirationListeners != null) {
            var2 = ExpiringMap.class;
            synchronized(ExpiringMap.class) {
                if (LISTENER_SERVICE == null) {
                    LISTENER_SERVICE = (ThreadPoolExecutor)Executors.newCachedThreadPool((ThreadFactory)(THREAD_FACTORY == null ? new NamedThreadFactory("ExpiringMap-Listener-%s") : THREAD_FACTORY));
                }
            }
        }

        this.variableExpiration = builder.variableExpiration;
        this.entries = (ExpiringMap.EntryMap)(this.variableExpiration ? new ExpiringMap.EntryTreeHashMap() : new ExpiringMap.EntryLinkedHashMap());
        if (builder.expirationListeners != null) {
            this.expirationListeners = new CopyOnWriteArrayList(builder.expirationListeners);
        }

        if (builder.asyncExpirationListeners != null) {
            this.asyncExpirationListeners = new CopyOnWriteArrayList(builder.asyncExpirationListeners);
        }

        this.expirationPolicy = new AtomicReference(builder.expirationPolicy);
        this.expirationNanos = new AtomicLong(TimeUnit.NANOSECONDS.convert(builder.duration, builder.timeUnit));
        this.maxSize = builder.maxSize;
        this.entryLoader = builder.entryLoader;
        this.expiringEntryLoader = builder.expiringEntryLoader;
    }

    public static ExpiringMap.Builder<Object, Object> builder() {
        return new ExpiringMap.Builder();
    }

    public static <K, V> ExpiringMap<K, V> create() {
        return new ExpiringMap(builder());
    }

    public synchronized void addExpirationListener(ExpiringMap.ExpirationListener<K, V> listener) {
        Valid.checkNotNull(listener, "listener");
        if (this.expirationListeners == null) {
            this.expirationListeners = new CopyOnWriteArrayList();
        }

        this.expirationListeners.add(listener);
    }

    public synchronized void addAsyncExpirationListener(ExpiringMap.ExpirationListener<K, V> listener) {
        Valid.checkNotNull(listener, "listener");
        if (this.asyncExpirationListeners == null) {
            this.asyncExpirationListeners = new CopyOnWriteArrayList();
        }

        this.asyncExpirationListeners.add(listener);
    }

    public void clear() {
        this.writeLock.lock();

        try {
            Iterator var1 = this.entries.values().iterator();

            while(var1.hasNext()) {
                ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)var1.next();
                entry.cancel();
            }

            this.entries.clear();
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean containsKey(Object key) {
        this.readLock.lock();

        boolean var2;
        try {
            var2 = this.entries.containsKey(key);
        } finally {
            this.readLock.unlock();
        }

        return var2;
    }

    public boolean containsValue(Object value) {
        this.readLock.lock();

        boolean var2;
        try {
            var2 = this.entries.containsValue(value);
        } finally {
            this.readLock.unlock();
        }

        return var2;
    }

    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            public void clear() {
                ExpiringMap.this.clear();
            }

            public boolean contains(Object entry) {
                if (!(entry instanceof Entry)) {
                    return false;
                } else {
                    Entry<?, ?> e = (Entry)entry;
                    return ExpiringMap.this.containsKey(e.getKey());
                }
            }

            public Iterator<Entry<K, V>> iterator() {
                return (Iterator)(ExpiringMap.this.entries instanceof ExpiringMap.EntryLinkedHashMap ? (ExpiringMap.EntryLinkedHashMap)ExpiringMap.this.entries.new EntryIterator() : (ExpiringMap.EntryTreeHashMap)ExpiringMap.this.entries.new EntryIterator());
            }

            public boolean remove(Object entry) {
                if (entry instanceof Entry) {
                    Entry<?, ?> e = (Entry)entry;
                    return ExpiringMap.this.remove(e.getKey()) != null;
                } else {
                    return false;
                }
            }

            public int size() {
                return ExpiringMap.this.size();
            }
        };
    }

    public boolean equals(Object obj) {
        this.readLock.lock();

        boolean var2;
        try {
            var2 = this.entries.equals(obj);
        } finally {
            this.readLock.unlock();
        }

        return var2;
    }

    public V get(Object key) {
        ExpiringMap.ExpiringEntry<K, V> entry = this.getEntry(key);
        if (entry == null) {
            return this.load(key);
        } else {
            if (ExpirationPolicy.ACCESSED.equals(entry.expirationPolicy.get())) {
                this.resetEntry(entry, false);
            }

            return entry.getValue();
        }
    }

    private V load(K key) {
        if (this.entryLoader == null && this.expiringEntryLoader == null) {
            return null;
        } else {
            this.writeLock.lock();

            Object var4;
            try {
                ExpiringMap.ExpiringEntry<K, V> entry = this.getEntry(key);
                Object value;
                if (entry != null) {
                    value = entry.getValue();
                    return value;
                }

                if (this.entryLoader != null) {
                    value = this.entryLoader.load(key);
                    this.put(key, value);
                    var4 = value;
                    return var4;
                }

                ExpiringValue<? extends V> expiringValue = this.expiringEntryLoader.load(key);
                if (expiringValue != null) {
                    long duration = expiringValue.getTimeUnit() == null ? this.expirationNanos.get() : expiringValue.getDuration();
                    TimeUnit timeUnit = expiringValue.getTimeUnit() == null ? TimeUnit.NANOSECONDS : expiringValue.getTimeUnit();
                    this.put(key, expiringValue.getValue(), expiringValue.getExpirationPolicy() == null ? (ExpirationPolicy)this.expirationPolicy.get() : expiringValue.getExpirationPolicy(), duration, timeUnit);
                    Object var7 = expiringValue.getValue();
                    return var7;
                }

                this.put(key, (Object)null);
                var4 = null;
            } finally {
                this.writeLock.unlock();
            }

            return var4;
        }
    }

    public long getExpiration() {
        return TimeUnit.NANOSECONDS.toMillis(this.expirationNanos.get());
    }

    public long getExpiration(K key) {
        Valid.checkNotNull(key, "key");
        ExpiringMap.ExpiringEntry<K, V> entry = this.getEntry(key);
        return TimeUnit.NANOSECONDS.toMillis(entry.expirationNanos.get());
    }

    public ExpirationPolicy getExpirationPolicy(K key) {
        Valid.checkNotNull(key, "key");
        ExpiringMap.ExpiringEntry<K, V> entry = this.getEntry(key);
        Valid.checkNotNull(entry);
        return (ExpirationPolicy)entry.expirationPolicy.get();
    }

    public long getExpectedExpiration(K key) {
        Valid.checkNotNull(key, "key");
        ExpiringMap.ExpiringEntry<K, V> entry = this.getEntry(key);
        Valid.checkNotNull(entry);
        return TimeUnit.NANOSECONDS.toMillis(entry.expectedExpiration.get() - System.nanoTime());
    }

    public int getMaxSize() {
        return this.maxSize;
    }

    public int hashCode() {
        this.readLock.lock();

        int var1;
        try {
            var1 = this.entries.hashCode();
        } finally {
            this.readLock.unlock();
        }

        return var1;
    }

    public boolean isEmpty() {
        this.readLock.lock();

        boolean var1;
        try {
            var1 = this.entries.isEmpty();
        } finally {
            this.readLock.unlock();
        }

        return var1;
    }

    public Set<K> keySet() {
        return new AbstractSet<K>() {
            public void clear() {
                ExpiringMap.this.clear();
            }

            public boolean contains(Object key) {
                return ExpiringMap.this.containsKey(key);
            }

            public Iterator<K> iterator() {
                return (Iterator)(ExpiringMap.this.entries instanceof ExpiringMap.EntryLinkedHashMap ? (ExpiringMap.EntryLinkedHashMap)ExpiringMap.this.entries.new KeyIterator() : (ExpiringMap.EntryTreeHashMap)ExpiringMap.this.entries.new KeyIterator());
            }

            public boolean remove(Object value) {
                return ExpiringMap.this.remove(value) != null;
            }

            public int size() {
                return ExpiringMap.this.size();
            }
        };
    }

    public V put(K key, V value) {
        Valid.checkNotNull(key, "key");
        return this.putInternal(key, value, (ExpirationPolicy)this.expirationPolicy.get(), this.expirationNanos.get());
    }

    public V put(K key, V value, ExpirationPolicy expirationPolicy) {
        return this.put(key, value, expirationPolicy, this.expirationNanos.get(), TimeUnit.NANOSECONDS);
    }

    public V put(K key, V value, long duration, TimeUnit timeUnit) {
        return this.put(key, value, (ExpirationPolicy)this.expirationPolicy.get(), duration, timeUnit);
    }

    public V put(K key, V value, ExpirationPolicy expirationPolicy, long duration, TimeUnit timeUnit) {
        Valid.checkNotNull(key, "key");
        Valid.checkNotNull(expirationPolicy, "expirationPolicy");
        Valid.checkNotNull(timeUnit, "timeUnit");
        Valid.checkBoolean(this.variableExpiration, "Variable expiration is not enabled", new Object[0]);
        return this.putInternal(key, value, expirationPolicy, TimeUnit.NANOSECONDS.convert(duration, timeUnit));
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        Valid.checkNotNull(map, "map");
        long expiration = this.expirationNanos.get();
        ExpirationPolicy expirationPolicy = (ExpirationPolicy)this.expirationPolicy.get();
        this.writeLock.lock();

        try {
            Iterator var5 = map.entrySet().iterator();

            while(var5.hasNext()) {
                Entry<? extends K, ? extends V> entry = (Entry)var5.next();
                this.putInternal(entry.getKey(), entry.getValue(), expirationPolicy, expiration);
            }
        } finally {
            this.writeLock.unlock();
        }

    }

    public V putIfAbsent(K key, V value) {
        Valid.checkNotNull(key, "key");
        this.writeLock.lock();

        Object var3;
        try {
            if (this.entries.containsKey(key)) {
                var3 = ((ExpiringMap.ExpiringEntry)this.entries.get(key)).getValue();
                return var3;
            }

            var3 = this.putInternal(key, value, (ExpirationPolicy)this.expirationPolicy.get(), this.expirationNanos.get());
        } finally {
            this.writeLock.unlock();
        }

        return var3;
    }

    public V remove(Object key) {
        Valid.checkNotNull(key, "key");
        this.writeLock.lock();

        Object var3;
        try {
            ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)this.entries.remove(key);
            if (entry == null) {
                var3 = null;
                return var3;
            }

            if (entry.cancel()) {
                this.scheduleEntry(this.entries.first());
            }

            var3 = entry.getValue();
        } finally {
            this.writeLock.unlock();
        }

        return var3;
    }

    public boolean remove(Object key, Object value) {
        Valid.checkNotNull(key, "key");
        this.writeLock.lock();

        boolean var4;
        try {
            ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)this.entries.get(key);
            if (entry != null && entry.getValue().equals(value)) {
                this.entries.remove(key);
                if (entry.cancel()) {
                    this.scheduleEntry(this.entries.first());
                }

                var4 = true;
                return var4;
            }

            var4 = false;
        } finally {
            this.writeLock.unlock();
        }

        return var4;
    }

    public V replace(K key, V value) {
        Valid.checkNotNull(key, "key");
        this.writeLock.lock();

        Object var3;
        try {
            if (!this.entries.containsKey(key)) {
                var3 = null;
                return var3;
            }

            var3 = this.putInternal(key, value, (ExpirationPolicy)this.expirationPolicy.get(), this.expirationNanos.get());
        } finally {
            this.writeLock.unlock();
        }

        return var3;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        Valid.checkNotNull(key, "key");
        this.writeLock.lock();

        boolean var5;
        try {
            ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)this.entries.get(key);
            if (entry == null || !entry.getValue().equals(oldValue)) {
                var5 = false;
                return var5;
            }

            this.putInternal(key, newValue, (ExpirationPolicy)this.expirationPolicy.get(), this.expirationNanos.get());
            var5 = true;
        } finally {
            this.writeLock.unlock();
        }

        return var5;
    }

    public void removeExpirationListener(ExpiringMap.ExpirationListener<K, V> listener) {
        Valid.checkNotNull(listener, "listener");

        for(int i = 0; i < this.expirationListeners.size(); ++i) {
            if (((ExpiringMap.ExpirationListener)this.expirationListeners.get(i)).equals(listener)) {
                this.expirationListeners.remove(i);
                return;
            }
        }

    }

    public void removeAsyncExpirationListener(ExpiringMap.ExpirationListener<K, V> listener) {
        Valid.checkNotNull(listener, "listener");

        for(int i = 0; i < this.asyncExpirationListeners.size(); ++i) {
            if (((ExpiringMap.ExpirationListener)this.asyncExpirationListeners.get(i)).equals(listener)) {
                this.asyncExpirationListeners.remove(i);
                return;
            }
        }

    }

    public void resetExpiration(K key) {
        Valid.checkNotNull(key, "key");
        ExpiringMap.ExpiringEntry<K, V> entry = this.getEntry(key);
        if (entry != null) {
            this.resetEntry(entry, false);
        }

    }

    public void setExpiration(K key, long duration, TimeUnit timeUnit) {
        Valid.checkNotNull(key, "key");
        Valid.checkNotNull(timeUnit, "timeUnit");
        Valid.checkBoolean(this.variableExpiration, "Variable expiration is not enabled", new Object[0]);
        this.writeLock.lock();

        try {
            ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)this.entries.get(key);
            if (entry != null) {
                entry.expirationNanos.set(TimeUnit.NANOSECONDS.convert(duration, timeUnit));
                this.resetEntry(entry, true);
            }
        } finally {
            this.writeLock.unlock();
        }

    }

    public void setExpiration(long duration, TimeUnit timeUnit) {
        Valid.checkNotNull(timeUnit, "timeUnit");
        Valid.checkBoolean(this.variableExpiration, "Variable expiration is not enabled", new Object[0]);
        this.expirationNanos.set(TimeUnit.NANOSECONDS.convert(duration, timeUnit));
    }

    public void setExpirationPolicy(ExpirationPolicy expirationPolicy) {
        Valid.checkNotNull(expirationPolicy, "expirationPolicy");
        this.expirationPolicy.set(expirationPolicy);
    }

    public void setExpirationPolicy(K key, ExpirationPolicy expirationPolicy) {
        Valid.checkNotNull(key, "key");
        Valid.checkNotNull(expirationPolicy, "expirationPolicy");
        Valid.checkBoolean(this.variableExpiration, "Variable expiration is not enabled", new Object[0]);
        ExpiringMap.ExpiringEntry<K, V> entry = this.getEntry(key);
        if (entry != null) {
            entry.expirationPolicy.set(expirationPolicy);
        }

    }

    public void setMaxSize(int maxSize) {
        Valid.checkBoolean(maxSize > 0, "maxSize", new Object[0]);
        this.maxSize = maxSize;
    }

    public int size() {
        this.readLock.lock();

        int var1;
        try {
            var1 = this.entries.size();
        } finally {
            this.readLock.unlock();
        }

        return var1;
    }

    public String toString() {
        this.readLock.lock();

        String var1;
        try {
            var1 = this.entries.toString();
        } finally {
            this.readLock.unlock();
        }

        return var1;
    }

    public Collection<V> values() {
        return new AbstractCollection<V>() {
            public void clear() {
                ExpiringMap.this.clear();
            }

            public boolean contains(Object value) {
                return ExpiringMap.this.containsValue(value);
            }

            public Iterator<V> iterator() {
                return (Iterator)(ExpiringMap.this.entries instanceof ExpiringMap.EntryLinkedHashMap ? (ExpiringMap.EntryLinkedHashMap)ExpiringMap.this.entries.new ValueIterator() : (ExpiringMap.EntryTreeHashMap)ExpiringMap.this.entries.new ValueIterator());
            }

            public int size() {
                return ExpiringMap.this.size();
            }
        };
    }

    void notifyListeners(ExpiringMap.ExpiringEntry<K, V> entry) {
        Iterator var2;
        ExpiringMap.ExpirationListener listener;
        if (this.asyncExpirationListeners != null) {
            var2 = this.asyncExpirationListeners.iterator();

            while(var2.hasNext()) {
                listener = (ExpiringMap.ExpirationListener)var2.next();
                LISTENER_SERVICE.execute(() -> {
                    try {
                        listener.expired(entry.key, entry.getValue());
                    } catch (Exception var3) {
                    }

                });
            }
        }

        if (this.expirationListeners != null) {
            var2 = this.expirationListeners.iterator();

            while(var2.hasNext()) {
                listener = (ExpiringMap.ExpirationListener)var2.next();

                try {
                    listener.expired(entry.key, entry.getValue());
                } catch (Exception var5) {
                }
            }
        }

    }

    ExpiringMap.ExpiringEntry<K, V> getEntry(Object key) {
        this.readLock.lock();

        ExpiringMap.ExpiringEntry var2;
        try {
            var2 = (ExpiringMap.ExpiringEntry)this.entries.get(key);
        } finally {
            this.readLock.unlock();
        }

        return var2;
    }

    V putInternal(K key, V value, ExpirationPolicy expirationPolicy, long expirationNanos) {
        this.writeLock.lock();

        try {
            ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)this.entries.get(key);
            V oldValue = null;
            Object var8;
            if (entry == null) {
                entry = new ExpiringMap.ExpiringEntry(key, value, this.variableExpiration ? new AtomicReference(expirationPolicy) : this.expirationPolicy, this.variableExpiration ? new AtomicLong(expirationNanos) : this.expirationNanos);
                if (this.entries.size() >= this.maxSize) {
                    ExpiringMap.ExpiringEntry<K, V> expiredEntry = this.entries.first();
                    this.entries.remove(expiredEntry.key);
                    this.notifyListeners(expiredEntry);
                }

                this.entries.put(key, entry);
                if (this.entries.size() == 1 || this.entries.first().equals(entry)) {
                    this.scheduleEntry(entry);
                }
            } else {
                oldValue = entry.getValue();
                if (!ExpirationPolicy.ACCESSED.equals(expirationPolicy) && (oldValue == null && value == null || oldValue != null && oldValue.equals(value))) {
                    var8 = value;
                    return var8;
                }

                entry.setValue(value);
                this.resetEntry(entry, false);
            }

            var8 = oldValue;
            return var8;
        } finally {
            this.writeLock.unlock();
        }
    }

    void resetEntry(ExpiringMap.ExpiringEntry<K, V> entry, boolean scheduleFirstEntry) {
        this.writeLock.lock();

        try {
            boolean scheduled = entry.cancel();
            this.entries.reorder(entry);
            if (scheduled || scheduleFirstEntry) {
                this.scheduleEntry(this.entries.first());
            }
        } finally {
            this.writeLock.unlock();
        }

    }

    void scheduleEntry(ExpiringMap.ExpiringEntry<K, V> entry) {
        if (entry != null && !entry.scheduled) {
            Runnable runnable = null;
            synchronized(entry) {
                if (!entry.scheduled) {
                    WeakReference<ExpiringMap.ExpiringEntry<K, V>> entryReference = new WeakReference(entry);
                    runnable = () -> {
                        ExpiringMap.ExpiringEntry<K, V> entry1 = (ExpiringMap.ExpiringEntry)entryReference.get();
                        this.writeLock.lock();

                        try {
                            if (entry1 != null && entry1.scheduled) {
                                this.entries.remove(entry1.key);
                                this.notifyListeners(entry1);
                            }

                            try {
                                Iterator<ExpiringMap.ExpiringEntry<K, V>> iterator = this.entries.valuesIterator();
                                boolean schedulePending = true;

                                while(iterator.hasNext() && schedulePending) {
                                    ExpiringMap.ExpiringEntry<K, V> nextEntry = (ExpiringMap.ExpiringEntry)iterator.next();
                                    if (nextEntry.expectedExpiration.get() <= System.nanoTime()) {
                                        iterator.remove();
                                        this.notifyListeners(nextEntry);
                                    } else {
                                        this.scheduleEntry(nextEntry);
                                        schedulePending = false;
                                    }
                                }
                            } catch (NoSuchElementException var9) {
                            }
                        } finally {
                            this.writeLock.unlock();
                        }

                    };
                    Future<?> entryFuture = EXPIRER.schedule(runnable, entry.expectedExpiration.get() - System.nanoTime(), TimeUnit.NANOSECONDS);
                    entry.schedule(entryFuture);
                }
            }
        }
    }

    private static <K, V> Entry<K, V> mapEntryFor(final ExpiringMap.ExpiringEntry<K, V> entry) {
        return new Entry<K, V>() {
            public K getKey() {
                return entry.key;
            }

            public V getValue() {
                return entry.value;
            }

            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    static class ExpiringEntry<K, V> implements Comparable<ExpiringMap.ExpiringEntry<K, V>> {
        final AtomicLong expirationNanos;
        final AtomicLong expectedExpiration;
        final AtomicReference<ExpirationPolicy> expirationPolicy;
        final K key;
        volatile Future<?> entryFuture;
        V value;
        volatile boolean scheduled;

        ExpiringEntry(K key, V value, AtomicReference<ExpirationPolicy> expirationPolicy, AtomicLong expirationNanos) {
            this.key = key;
            this.value = value;
            this.expirationPolicy = expirationPolicy;
            this.expirationNanos = expirationNanos;
            this.expectedExpiration = new AtomicLong();
            this.resetExpiration();
        }

        public int compareTo(ExpiringMap.ExpiringEntry<K, V> other) {
            if (this.key.equals(other.key)) {
                return 0;
            } else {
                return this.expectedExpiration.get() < other.expectedExpiration.get() ? -1 : 1;
            }
        }

        public int hashCode() {
            int prime = true;
            int result = 1;
            int result = 31 * result + (this.key == null ? 0 : this.key.hashCode());
            result = 31 * result + (this.value == null ? 0 : this.value.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                ExpiringMap.ExpiringEntry<?, ?> other = (ExpiringMap.ExpiringEntry)obj;
                if (!this.key.equals(other.key)) {
                    return false;
                } else {
                    if (this.value == null) {
                        if (other.value != null) {
                            return false;
                        }
                    } else if (!this.value.equals(other.value)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        public String toString() {
            return this.value != null ? this.value.toString() : "";
        }

        synchronized boolean cancel() {
            boolean result = this.scheduled;
            if (this.entryFuture != null) {
                this.entryFuture.cancel(false);
            }

            this.entryFuture = null;
            this.scheduled = false;
            return result;
        }

        synchronized V getValue() {
            return this.value;
        }

        void resetExpiration() {
            this.expectedExpiration.set(this.expirationNanos.get() + System.nanoTime());
        }

        synchronized void schedule(Future<?> entryFuture) {
            this.entryFuture = entryFuture;
            this.scheduled = true;
        }

        synchronized void setValue(V value) {
            this.value = value;
        }
    }

    private static class EntryTreeHashMap<K, V> extends HashMap<K, ExpiringMap.ExpiringEntry<K, V>> implements ExpiringMap.EntryMap<K, V> {
        private static final long serialVersionUID = 1L;
        SortedSet<ExpiringMap.ExpiringEntry<K, V>> sortedSet;

        private EntryTreeHashMap() {
            this.sortedSet = new ConcurrentSkipListSet();
        }

        public void clear() {
            super.clear();
            this.sortedSet.clear();
        }

        public boolean containsValue(Object value) {
            Iterator var2 = this.values().iterator();

            Object v;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)var2.next();
                v = entry.value;
            } while(v != value && (value == null || !value.equals(v)));

            return true;
        }

        public ExpiringMap.ExpiringEntry<K, V> first() {
            return this.sortedSet.isEmpty() ? null : (ExpiringMap.ExpiringEntry)this.sortedSet.first();
        }

        public ExpiringMap.ExpiringEntry<K, V> put(K key, ExpiringMap.ExpiringEntry<K, V> value) {
            this.sortedSet.add(value);
            return (ExpiringMap.ExpiringEntry)super.put(key, value);
        }

        public ExpiringMap.ExpiringEntry<K, V> remove(Object key) {
            ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)super.remove(key);
            if (entry != null) {
                this.sortedSet.remove(entry);
            }

            return entry;
        }

        public void reorder(ExpiringMap.ExpiringEntry<K, V> value) {
            this.sortedSet.remove(value);
            value.resetExpiration();
            this.sortedSet.add(value);
        }

        public Iterator<ExpiringMap.ExpiringEntry<K, V>> valuesIterator() {
            return new ExpiringMap.EntryTreeHashMap.ExpiringEntryIterator();
        }

        final class EntryIterator extends ExpiringMap.EntryTreeHashMap<K, V>.AbstractHashIterator implements Iterator<Entry<K, V>> {
            EntryIterator() {
                super();
            }

            public Entry<K, V> next() {
                return ExpiringMap.mapEntryFor(this.getNext());
            }
        }

        final class ValueIterator extends ExpiringMap.EntryTreeHashMap<K, V>.AbstractHashIterator implements Iterator<V> {
            ValueIterator() {
                super();
            }

            public V next() {
                return this.getNext().value;
            }
        }

        final class KeyIterator extends ExpiringMap.EntryTreeHashMap<K, V>.AbstractHashIterator implements Iterator<K> {
            KeyIterator() {
                super();
            }

            public K next() {
                return this.getNext().key;
            }
        }

        final class ExpiringEntryIterator extends ExpiringMap.EntryTreeHashMap<K, V>.AbstractHashIterator implements Iterator<ExpiringMap.ExpiringEntry<K, V>> {
            ExpiringEntryIterator() {
                super();
            }

            public ExpiringMap.ExpiringEntry<K, V> next() {
                return this.getNext();
            }
        }

        abstract class AbstractHashIterator {
            private final Iterator<ExpiringMap.ExpiringEntry<K, V>> iterator;
            protected ExpiringMap.ExpiringEntry<K, V> next;

            AbstractHashIterator() {
                this.iterator = EntryTreeHashMap.this.sortedSet.iterator();
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public ExpiringMap.ExpiringEntry<K, V> getNext() {
                this.next = (ExpiringMap.ExpiringEntry)this.iterator.next();
                return this.next;
            }

            public void remove() {
                ExpiringMap.EntryTreeHashMap.super.remove(this.next.key);
                this.iterator.remove();
            }
        }
    }

    private static class EntryLinkedHashMap<K, V> extends LinkedHashMap<K, ExpiringMap.ExpiringEntry<K, V>> implements ExpiringMap.EntryMap<K, V> {
        private static final long serialVersionUID = 1L;

        private EntryLinkedHashMap() {
        }

        public boolean containsValue(Object value) {
            Iterator var2 = this.values().iterator();

            Object v;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                ExpiringMap.ExpiringEntry<K, V> entry = (ExpiringMap.ExpiringEntry)var2.next();
                v = entry.value;
            } while(v != value && (value == null || !value.equals(v)));

            return true;
        }

        public ExpiringMap.ExpiringEntry<K, V> first() {
            return this.isEmpty() ? null : (ExpiringMap.ExpiringEntry)this.values().iterator().next();
        }

        public void reorder(ExpiringMap.ExpiringEntry<K, V> value) {
            this.remove(value.key);
            value.resetExpiration();
            this.put(value.key, value);
        }

        public Iterator<ExpiringMap.ExpiringEntry<K, V>> valuesIterator() {
            return this.values().iterator();
        }

        public final class EntryIterator extends ExpiringMap.EntryLinkedHashMap<K, V>.AbstractHashIterator implements Iterator<Entry<K, V>> {
            public EntryIterator() {
                super();
            }

            public Entry<K, V> next() {
                return ExpiringMap.mapEntryFor(this.getNext());
            }
        }

        final class ValueIterator extends ExpiringMap.EntryLinkedHashMap<K, V>.AbstractHashIterator implements Iterator<V> {
            ValueIterator() {
                super();
            }

            public V next() {
                return this.getNext().value;
            }
        }

        final class KeyIterator extends ExpiringMap.EntryLinkedHashMap<K, V>.AbstractHashIterator implements Iterator<K> {
            KeyIterator() {
                super();
            }

            public K next() {
                return this.getNext().key;
            }
        }

        abstract class AbstractHashIterator {
            private final Iterator<Entry<K, ExpiringMap.ExpiringEntry<K, V>>> iterator = EntryLinkedHashMap.this.entrySet().iterator();
            private ExpiringMap.ExpiringEntry<K, V> next;

            AbstractHashIterator() {
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public ExpiringMap.ExpiringEntry<K, V> getNext() {
                this.next = (ExpiringMap.ExpiringEntry)((Entry)this.iterator.next()).getValue();
                return this.next;
            }

            public void remove() {
                this.iterator.remove();
            }
        }
    }

    private interface EntryMap<K, V> extends Map<K, ExpiringMap.ExpiringEntry<K, V>> {
        ExpiringMap.ExpiringEntry<K, V> first();

        void reorder(ExpiringMap.ExpiringEntry<K, V> var1);

        Iterator<ExpiringMap.ExpiringEntry<K, V>> valuesIterator();
    }

    public static final class Builder<K, V> {
        private ExpirationPolicy expirationPolicy;
        private List<ExpiringMap.ExpirationListener<K, V>> expirationListeners;
        private List<ExpiringMap.ExpirationListener<K, V>> asyncExpirationListeners;
        private TimeUnit timeUnit;
        private boolean variableExpiration;
        private long duration;
        private int maxSize;
        private EntryLoader<K, V> entryLoader;
        private ExpiringEntryLoader<K, V> expiringEntryLoader;

        private Builder() {
            this.expirationPolicy = ExpirationPolicy.CREATED;
            this.timeUnit = TimeUnit.SECONDS;
            this.duration = 60L;
            this.maxSize = 2147483647;
        }

        public <K1 extends K, V1 extends V> ExpiringMap<K1, V1> build() {
            return new ExpiringMap(this);
        }

        public ExpiringMap.Builder<K, V> expiration(long duration, @NonNull TimeUnit timeUnit) {
            if (timeUnit == null) {
                throw new NullPointerException("timeUnit is marked non-null but is null");
            } else {
                this.duration = duration;
                this.timeUnit = timeUnit;
                return this;
            }
        }

        public ExpiringMap.Builder<K, V> maxSize(int maxSize) {
            Valid.checkBoolean(maxSize > 0, "maxSize", new Object[0]);
            this.maxSize = maxSize;
            return this;
        }

        public <K1 extends K, V1 extends V> ExpiringMap.Builder<K1, V1> entryLoader(@NonNull EntryLoader<? super K1, ? super V1> loader) {
            if (loader == null) {
                throw new NullPointerException("loader is marked non-null but is null");
            } else {
                this.assertNoLoaderSet();
                this.entryLoader = loader;
                return this;
            }
        }

        public <K1 extends K, V1 extends V> ExpiringMap.Builder<K1, V1> expiringEntryLoader(@NonNull ExpiringEntryLoader<? super K1, ? super V1> loader) {
            if (loader == null) {
                throw new NullPointerException("loader is marked non-null but is null");
            } else {
                this.assertNoLoaderSet();
                this.expiringEntryLoader = loader;
                this.variableExpiration();
                return this;
            }
        }

        public <K1 extends K, V1 extends V> ExpiringMap.Builder<K1, V1> expirationListener(ExpiringMap.ExpirationListener<? super K1, ? super V1> listener) {
            Valid.checkNotNull(listener, "listener");
            if (this.expirationListeners == null) {
                this.expirationListeners = new ArrayList();
            }

            this.expirationListeners.add(listener);
            return this;
        }

        public <K1 extends K, V1 extends V> ExpiringMap.Builder<K1, V1> expirationListeners(List<ExpiringMap.ExpirationListener<? super K1, ? super V1>> listeners) {
            Valid.checkNotNull(listeners, "listeners");
            if (this.expirationListeners == null) {
                this.expirationListeners = new ArrayList(listeners.size());
            }

            Iterator var2 = listeners.iterator();

            while(var2.hasNext()) {
                ExpiringMap.ExpirationListener<? super K1, ? super V1> listener = (ExpiringMap.ExpirationListener)var2.next();
                this.expirationListeners.add(listener);
            }

            return this;
        }

        public <K1 extends K, V1 extends V> ExpiringMap.Builder<K1, V1> asyncExpirationListener(ExpiringMap.ExpirationListener<? super K1, ? super V1> listener) {
            Valid.checkNotNull(listener, "listener");
            if (this.asyncExpirationListeners == null) {
                this.asyncExpirationListeners = new ArrayList();
            }

            this.asyncExpirationListeners.add(listener);
            return this;
        }

        public <K1 extends K, V1 extends V> ExpiringMap.Builder<K1, V1> asyncExpirationListeners(List<ExpiringMap.ExpirationListener<? super K1, ? super V1>> listeners) {
            Valid.checkNotNull(listeners, "listeners");
            if (this.asyncExpirationListeners == null) {
                this.asyncExpirationListeners = new ArrayList(listeners.size());
            }

            Iterator var2 = listeners.iterator();

            while(var2.hasNext()) {
                ExpiringMap.ExpirationListener<? super K1, ? super V1> listener = (ExpiringMap.ExpirationListener)var2.next();
                this.asyncExpirationListeners.add(listener);
            }

            return this;
        }

        public ExpiringMap.Builder<K, V> expirationPolicy(@NonNull ExpirationPolicy expirationPolicy) {
            if (expirationPolicy == null) {
                throw new NullPointerException("expirationPolicy is marked non-null but is null");
            } else {
                this.expirationPolicy = expirationPolicy;
                return this;
            }
        }

        public ExpiringMap.Builder<K, V> variableExpiration() {
            this.variableExpiration = true;
            return this;
        }

        private void assertNoLoaderSet() {
            Valid.checkBoolean(this.entryLoader == null && this.expiringEntryLoader == null, "Either entryLoader or expiringEntryLoader may be set, not both", new Object[0]);
        }
    }

    public interface ExpirationListener<K, V> {
        void expired(K var1, V var2);
    }
}
