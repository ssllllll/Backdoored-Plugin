package me.ego.ezbd.lib.fo.collection.expiringmap;

public interface EntryLoader<K, V> {
    V load(K var1);
}