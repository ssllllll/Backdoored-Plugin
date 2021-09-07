package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Valid;

public final class Whiteblacklist {
    private final Set<String> items;
    private final boolean whitelist;
    private final boolean entireList;

    public Whiteblacklist(@NonNull List<String> items) {
        if (items == null) {
            throw new NullPointerException("items is marked non-null but is null");
        } else {
            if (!items.isEmpty()) {
                String firstLine = (String)items.get(0);
                this.entireList = firstLine.equals("*");
                this.whitelist = !firstLine.equals("@blacklist") && !this.entireList;
                List<String> newItems = new ArrayList(items);
                if (this.entireList || firstLine.equals("@blacklist")) {
                    newItems.remove(0);
                }

                this.items = new HashSet((Collection)(this.whitelist ? items : newItems));
            } else {
                this.items = new HashSet();
                this.whitelist = true;
                this.entireList = false;
            }

        }
    }

    public boolean isInList(String item) {
        if (this.entireList) {
            return true;
        } else {
            boolean match = Valid.isInList(item, this.items);
            return this.whitelist ? match : !match;
        }
    }

    public boolean isInListRegex(String item) {
        if (this.entireList) {
            return true;
        } else {
            boolean match = Valid.isInListRegex(item, this.items);
            return this.whitelist ? match : !match;
        }
    }

    /** @deprecated */
    @Deprecated
    public boolean isInListContains(String item) {
        if (this.entireList) {
            return true;
        } else {
            boolean match = Valid.isInListContains(item, this.items);
            return this.whitelist ? match : !match;
        }
    }

    public boolean isInListStartsWith(String item) {
        if (this.entireList) {
            return true;
        } else {
            boolean match = Valid.isInListStartsWith(item, this.items);
            return this.whitelist ? match : !match;
        }
    }

    public String toString() {
        return "{" + (this.entireList ? "entire list" : (this.whitelist ? "whitelist" : "blacklist")) + " " + this.items + "}";
    }

    public Set<String> getItems() {
        return this.items;
    }

    public boolean isWhitelist() {
        return this.whitelist;
    }

    public boolean isEntireList() {
        return this.entireList;
    }
}