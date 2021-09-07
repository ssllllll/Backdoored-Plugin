package me.ego.ezbd.lib.fo.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import org.apache.commons.lang.StringUtils;

public final class StrictList<E> extends StrictCollection implements Iterable<E> {
    private final List<E> list;

    @SafeVarargs
    public StrictList(E... elements) {
        this();
        this.addAll(Arrays.asList(elements));
    }

    public StrictList(Iterable<E> oldList) {
        this();
        this.addAll(oldList);
    }

    public StrictList() {
        super("Cannot remove '%s' as it is not in the list!", "Value '%s' is already in the list!");
        this.list = new ArrayList();
    }

    public List<E> getSource() {
        return this.list;
    }

    public E getAndRemove(int index) {
        E e = this.list.get(index);
        this.remove(index);
        return e;
    }

    public void remove(E key) {
        boolean removed = this.removeWeak(key);
        Valid.checkBoolean(removed, String.format(this.getCannotRemoveMessage(), key), new Object[0]);
    }

    public E remove(int index) {
        E removed = this.list.remove(index);
        Valid.checkNotNull(removed, String.format(this.getCannotRemoveMessage(), "index: " + index));
        return removed;
    }

    public void addAll(Iterable<E> elements) {
        Iterator var2 = elements.iterator();

        while(var2.hasNext()) {
            E key = var2.next();
            this.add(key);
        }

    }

    public void addIfNotExist(E key) {
        if (!this.contains(key)) {
            this.add(key);
        }

    }

    public void add(E key) {
        Valid.checkNotNull(key, "Cannot add null values");
        Valid.checkBoolean(!this.list.contains(key), String.format(this.getCannotAddMessage(), key), new Object[0]);
        this.addWeak(key);
    }

    public StrictList<E> range(int startIndex) {
        Valid.checkBoolean(startIndex <= this.list.size(), "Start index out of range " + startIndex + " vs. list size " + this.list.size(), new Object[0]);
        StrictList<E> ranged = new StrictList();

        for(int i = startIndex; i < this.list.size(); ++i) {
            ranged.add(this.list.get(i));
        }

        return ranged;
    }

    public boolean removeWeak(E value) {
        Valid.checkNotNull(value, "Cannot remove null values");
        return this.list.remove(value);
    }

    public void addWeakAll(Iterable<E> keys) {
        Iterator var2 = keys.iterator();

        while(var2.hasNext()) {
            E key = var2.next();
            this.addWeak(key);
        }

    }

    public void addWeak(E key) {
        this.list.add(key);
    }

    public void set(int index, E key) {
        this.list.set(index, key);
    }

    public E getOrDefault(int index, E def) {
        return index < this.list.size() ? this.list.get(index) : def;
    }

    public E get(int index) {
        return this.list.get(index);
    }

    public boolean contains(E key) {
        Iterator var2 = this.list.iterator();

        Object other;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            other = var2.next();
            if (other instanceof String && key instanceof String && ((String)other).equalsIgnoreCase((String)key)) {
                return true;
            }
        } while(!other.equals(key));

        return true;
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return this.list.subList(fromIndex, toIndex);
    }

    public void clear() {
        this.list.clear();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public int size() {
        return this.list.size();
    }

    public String join(String separator) {
        return StringUtils.join(this.list, separator);
    }

    public E[] toArray(E[] e) {
        return this.list.toArray(e);
    }

    public Object[] toArray() {
        return this.list.toArray();
    }

    public Iterator<E> iterator() {
        return this.list.iterator();
    }

    public Object serialize() {
        return SerializeUtil.serialize(this.list);
    }

    public String toString() {
        return this.list.toString();
    }
}