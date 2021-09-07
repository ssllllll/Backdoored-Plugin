package me.ego.ezbd;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

class FilterLegacy implements Filter {
    FilterLegacy() {
    }

    public boolean isLoggable(LogRecord record) {
        String message = record.getMessage();
        return !me.ego.ezbd.Filter.isFiltered(message);
    }
}
