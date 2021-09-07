package me.ego.ezbd;

import java.io.PrintStream;

class FilterSystem extends PrintStream {
    FilterSystem() {
        super(System.out);
    }

    public void println(Object x) {
        if (x != null && !Filter.isFiltered(x.toString())) {
            super.println(x);
        }

    }

    public void println(String x) {
        if (x != null && !Filter.isFiltered(x)) {
            super.println(x);
        }

    }
}
