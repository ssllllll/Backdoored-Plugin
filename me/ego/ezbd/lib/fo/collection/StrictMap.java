package me.ego.ezbd.lib.fo.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;

public final class StrictMap<E, T> extends StrictCollection {
    private final Map<E, T> map;

    public StrictMap() {
        super("Cannot remove '%s' as it is not in the map!", "Key '%s' is already in the map --> '%s'");
        this.map = new LinkedHashMap();
    }

    public StrictMap(String removeMessage, String addMessage) {
        super(removeMessage, addMessage);
        this.map = new LinkedHashMap();
    }

    public StrictMap(Map<E, T> copyOf) {
        this();
        this.putAll(copyOf);
    }

    public void removeByValue(T value) {
        Iterator var2 = this.map.entrySet().iterator();

        Entry e;
        do {
            if (!var2.hasNext()) {
                throw new NullPointerException(String.format(this.getCannotRemoveMessage(), value));
            }

            e = (Entry)var2.next();
        } while(!e.getValue().equals(value));

        this.map.remove(e.getKey());
    }

    public Object[] removeAll(Collection<E> keys) {
        List<T> removedKeys = new ArrayList();
        Iterator var3 = keys.iterator();

        while(var3.hasNext()) {
            E key = var3.next();
            removedKeys.add(this.remove(key));
        }

        return removedKeys.toArray();
    }

    public T remove(E key) {
        T removed = this.removeWeak(key);
        Valid.checkNotNull(removed, String.format(this.getCannotRemoveMessage(), key));
        return removed;
    }

    public void put(E key, T value) {
        Valid.checkBoolean(!this.map.containsKey(key), String.format(this.getCannotAddMessage(), key, this.map.get(key)), new Object[0]);
        this.override(key, value);
    }

    public void putAll(Map<? extends E, ? extends T> m) {
        Iterator var2 = m.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<? extends E, ? extends T> e = (Entry)var2.next();
            Valid.checkBoolean(!this.map.containsKey(e.getKey()), String.format(this.getCannotAddMessage(), e.getKey(), this.map.get(e.getKey())), new Object[0]);
        }

        this.override(m);
    }

    public T removeWeak(E value) {
        return this.map.remove(value);
    }

    public void override(E key, T value) {
        this.map.put(key, value);
    }

    public void override(Map<? extends E, ? extends T> m) {
        this.map.putAll(m);
    }

    public T getOrPut(E key, T defaultToPut) {
        if (this.contains(key)) {
            return this.get(key);
        } else {
            this.put(key, defaultToPut);
            return defaultToPut;
        }
    }

    public E getKeyFromValue(T value) {
        Iterator var2 = this.map.entrySet().iterator();

        Entry e;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            e = (Entry)var2.next();
        } while(!e.getValue().equals(value));

        return e.getKey();
    }

    public E getFirstKey() {
        return this.map.isEmpty() ? null : this.map.keySet().iterator().next();
    }

    public T get(E key) {
        return this.map.get(key);
    }

    public T getOrDefault(E key, T def) {
        return this.map.getOrDefault(key, def);
    }

    public boolean contains(E key) {
        return key == null ? false : this.map.containsKey(key);
    }

    public boolean containsValue(T value) {
        return value == null ? false : this.map.containsValue(value);
    }

    public void forEachIterate(BiConsumer<E, T> consumer) {
        Iterator it = this.entrySet().iterator();

        while(it.hasNext()) {
            Entry<E, T> entry = (Entry)it.next();
            consumer.accept(entry.getKey(), entry.getValue());
        }

    }

    public Set<Entry<E, T>> entrySet() {
        return this.map.entrySet();
    }

    public Set<E> keySet() {
        return this.map.keySet();
    }

    public Collection<T> values() {
        return this.map.values();
    }

    public void clear() {
        this.map.clear();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public Map<E, T> getSource() {
        return this.map;
    }

    public int size() {
        return this.map.size();
    }

    public Object serialize() {
        if (!this.map.isEmpty()) {
            Map<Object, Object> copy = new LinkedHashMap();
            Iterator var2 = this.entrySet().iterator();

            while(var2.hasNext()) {
                Entry<E, T> entry = (Entry)var2.next();
                T val = entry.getValue();
                if (val != null) {
                    copy.put(SerializeUtil.serialize(entry.getKey()), SerializeUtil.serialize(val));
                }
            }

            return copy;
        } else {
            return this.getSource();
        }
    }

    public String toString() {
        return this.map.toString();
    }
}
