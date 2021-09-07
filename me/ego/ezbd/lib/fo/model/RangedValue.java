package me.ego.ezbd.lib.fo.model;

import me.ego.ezbd.lib.fo.TimeUtil;
import me.ego.ezbd.lib.fo.Valid;
import org.apache.commons.lang.math.NumberUtils;

public class RangedValue {
    private final Number min;
    private final Number max;

    public RangedValue(Number value) {
        this(value, value);
    }

    public RangedValue(Number min, Number max) {
        Valid.checkBoolean(min.longValue() >= 0L && max.longValue() >= 0L, "Values may not be negative", new Object[0]);
        Valid.checkBoolean(min.longValue() <= max.longValue(), "Minimum must be lower or equal maximum", new Object[0]);
        this.min = min;
        this.max = max;
    }

    public final int getMinInt() {
        return this.min.intValue();
    }

    public final int getMaxInt() {
        return this.max.intValue();
    }

    public final long getMinLong() {
        return this.min.longValue();
    }

    public final long getMaxLong() {
        return this.max.longValue();
    }

    public boolean isWithin(Number value) {
        return value.longValue() >= this.min.longValue() && value.longValue() <= this.max.longValue();
    }

    public final boolean isStatic() {
        return this.min == this.max;
    }

    public final String toLine() {
        return this.min + " - " + this.max;
    }

    public static RangedValue parse(String line) {
        line = line.replace(" ", "");
        String[] parts = line.split("\\-");
        Valid.checkBoolean(parts.length == 1 || parts.length == 2, "Malformed value " + line, new Object[0]);
        String first = parts[0];
        Integer min = NumberUtils.isNumber(first) ? Integer.parseInt(first) : (int)(TimeUtil.toTicks(first) / 20L);
        String second = parts.length == 2 ? parts[1] : "";
        Integer max = parts.length == 2 ? (NumberUtils.isNumber(second) ? Integer.parseInt(second) : (int)(TimeUtil.toTicks(second) / 20L)) : min;
        Valid.checkBoolean(min != null && max != null, "Malformed value " + line, new Object[0]);
        return new RangedValue(min, max);
    }

    public final String toString() {
        return this.isStatic() ? this.min + "" : this.min + " - " + this.max;
    }

    public Number getMin() {
        return this.min;
    }

    public Number getMax() {
        return this.max;
    }
}