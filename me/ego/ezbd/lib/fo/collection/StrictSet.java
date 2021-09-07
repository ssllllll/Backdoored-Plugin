package me.ego.ezbd.lib.fo.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.apache.commons.lang.StringUtils;

public final class StrictSet<E> extends StrictCollection implements Iterable<E> {
    private final Set<E> set;

    @SafeVarargs
    public StrictSet(E... elements) {
        this();
        this.addAll(Arrays.asList(elements));
    }

    public StrictSet(Collection<E> oldList) {
        this();
        this.addAll(oldList);
    }

    public StrictSet() {
        super("Cannot remove '%s' as it is not in the set!", "Value '%s' is already in the set!");
        this.set = new HashSet();
    }

    public void remove(E value) {
        Valid.checkNotNull(value, "Cannot remove null values");
        boolean removed = this.set.remove(value);
        Valid.checkBoolean(removed, String.format(this.getCannotRemoveMessage(), value), new Object[0]);
    }

    public void removeWeak(E value) {
        this.set.remove(value);
    }

    public void addAll(Collection<E> collection) {
        Iterator var2 = collection.iterator();

        while(var2.hasNext()) {
            E val = var2.next();
            this.add(val);
        }

    }

    public void add(E key) {
        Valid.checkNotNull(key, "Cannot add null values");
        Valid.checkBoolean(!this.set.contains(key), String.format(this.getCannotAddMessage(), key), new Object[0]);
        this.set.add(key);
    }

    public void override(E key) {
        this.set.add(key);
    }

    public E getAt(int index) {
        int i = 0;
        Iterator it = this.set.iterator();

        Object e;
        do {
            if (!it.hasNext()) {
                throw new FoException("Index (" + index + ") + out of size (" + this.set.size() + ")");
            }

            e = it.next();
        } while(i++ != index);

        return e;
    }

    public boolean contains(E key) {
        return this.set.contains(key);
    }

    public void clear() {
        this.set.clear();
    }

    public boolean isEmpty() {
        return this.set.isEmpty();
    }

    public int size() {
        return this.set.size();
    }

    public Set<E> getSource() {
        return this.set;
    }

    public String join(String separator) {
        return StringUtils.join(this.set, separator);
    }

    public E[] toArray(E[] e) {
        return this.set.toArray(e);
    }

    public Iterator<E> iterator() {
        return this.set.iterator();
    }

    public Object serialize() {
        return SerializeUtil.serialize(this.set);
    }

    public String toString() {
        return "StrictSet{\n" + StringUtils.join(this.set, "\n") + "}";
    }
}
