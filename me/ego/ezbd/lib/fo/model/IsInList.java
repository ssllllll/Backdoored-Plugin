package me.ego.ezbd.lib.fo.model;

import java.util.Collection;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.collection.StrictSet;

public final class IsInList<T> {
    private final StrictSet<T> list;
    private final boolean matchAll;

    public IsInList(StrictSet<T> list) {
        this((Collection)list.getSource());
    }

    public IsInList(StrictList<T> list) {
        this((Collection)list.getSource());
    }

    public IsInList(Collection<T> list) {
        this.list = new StrictSet(list);
        if (list.isEmpty()) {
            this.matchAll = true;
        } else if (list.iterator().next().equals("*")) {
            this.matchAll = true;
        } else {
            this.matchAll = false;
        }

    }

    public boolean contains(T toEvaluateAgainst) {
        return this.matchAll || this.list.contains(toEvaluateAgainst);
    }

    public boolean isEntireList() {
        return this.matchAll;
    }

    public StrictSet<T> getList() {
        return this.list;
    }
}