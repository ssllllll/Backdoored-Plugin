package me.ego.ezbd.lib.fo.collection.expiringmap;

public interface ExpiringEntryLoader<K, V> {
    ExpiringValue<V> load(K var1);
}
