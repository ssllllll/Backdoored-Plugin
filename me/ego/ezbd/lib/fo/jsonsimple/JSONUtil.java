package me.ego.ezbd.lib.fo.jsonsimple;

import java.util.Collection;
import java.util.Map;

public final class JSONUtil {
    private JSONUtil() {
    }

    public static String escape(String string) {
        if (string != null) {
            StringBuilder builder = new StringBuilder();
            JsonSimpleUtil.escape(string, builder);
            return builder.toString();
        } else {
            return null;
        }
    }

    public static boolean isJSONType(Object value) {
        return value == null || value instanceof Number || value instanceof String || value instanceof Boolean || value instanceof Collection || value instanceof Map || value.getClass().isArray();
    }
}
