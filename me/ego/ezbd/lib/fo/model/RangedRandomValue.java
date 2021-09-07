package me.ego.ezbd.lib.fo.model;

import me.ego.ezbd.lib.fo.RandomUtil;
import me.ego.ezbd.lib.fo.Valid;

public final class RangedRandomValue extends RangedValue {
    public RangedRandomValue(int value) {
        this(value, value);
    }

    public RangedRandomValue(int min, int max) {
        super(min, max);
        Valid.checkBoolean(min >= 0 && max >= 0, "Values may not be negative", new Object[0]);
        Valid.checkBoolean(min <= max, "Minimum must be lower or equal maximum", new Object[0]);
    }

    public static RangedRandomValue parse(String line) {
        RangedValue random = RangedValue.parse(line);
        return new RangedRandomValue(random.getMinInt(), random.getMaxInt());
    }

    public int getRandom() {
        return RandomUtil.nextBetween(this.getMinInt(), this.getMaxInt());
    }

    public boolean isInRange(int value) {
        return value >= this.getMinInt() && value <= this.getMaxInt();
    }
}