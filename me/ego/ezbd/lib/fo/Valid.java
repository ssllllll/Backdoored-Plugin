package me.ego.ezbd.lib.fo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.RangedValue;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

public final class Valid {
    private static final Pattern PATTERN_INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern PATTERN_DECIMAL = Pattern.compile("-?\\d+.\\d+");

    public static void checkNotNull(Object toCheck) {
        if (toCheck == null) {
            throw new FoException();
        }
    }

    public static void checkNotNull(Object toCheck, String falseMessage) {
        if (toCheck == null) {
            throw new FoException(falseMessage);
        }
    }

    public static void checkBoolean(boolean expression) {
        if (!expression) {
            throw new FoException();
        }
    }

    public static void checkBoolean(boolean expression, String falseMessage, Object... replacements) {
        if (!expression) {
            throw new FoException(String.format(falseMessage, replacements));
        }
    }

    public static void checkInteger(String toCheck, String falseMessage, Object... replacements) {
        if (!isInteger(toCheck)) {
            throw new FoException(String.format(falseMessage, replacements));
        }
    }

    public static void checkNotEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.size() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkNotEmpty(String message, String emptyMessage) {
        if (message == null || message.length() == 0) {
            throw new IllegalArgumentException(emptyMessage);
        }
    }

    public static boolean checkPermission(CommandSender sender, String permission) {
        if (!PlayerUtil.hasPerm(sender, permission)) {
            Common.tell(sender, new String[]{SimpleLocalization.NO_PERMISSION.replace("{permission}", permission)});
            return false;
        } else {
            return true;
        }
    }

    public static void checkSync(String asyncErrorMessage) {
        checkBoolean(Bukkit.isPrimaryThread(), asyncErrorMessage);
    }

    public static void checkAsync(String syncErrorMessage) {
        checkBoolean(!Bukkit.isPrimaryThread(), syncErrorMessage);
    }

    public static boolean isInteger(String raw) {
        checkNotNull(raw, "Cannot check if null is an integer!");
        return PATTERN_INTEGER.matcher(raw).find();
    }

    public static boolean isDecimal(String raw) {
        checkNotNull(raw, "Cannot check if null is a decimal!");
        return PATTERN_DECIMAL.matcher(raw).find();
    }

    public static boolean isNullOrEmpty(Collection<?> array) {
        return array == null || isNullOrEmpty(array.toArray());
    }

    public static boolean isNullOrEmptyValues(SerializedMap map) {
        return isNullOrEmptyValues(map == null ? null : map.asMap());
    }

    public static boolean isNullOrEmptyValues(Map<?, ?> map) {
        if (map == null) {
            return true;
        } else {
            Iterator var1 = map.values().iterator();

            Object value;
            do {
                if (!var1.hasNext()) {
                    return true;
                }

                value = var1.next();
            } while(value == null);

            return false;
        }
    }

    public static boolean isNullOrEmpty(Object[] array) {
        if (array != null) {
            Object[] var1 = array;
            int var2 = array.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Object object = var1[var3];
                if (object instanceof String) {
                    if (!((String)object).isEmpty()) {
                        return false;
                    }
                } else if (object != null) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isNullOrEmpty(String message) {
        return message == null || message.isEmpty();
    }

    public static boolean isFinite(Vector vector) {
        return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
    }

    public static boolean isInRange(long value, RangedValue ranged) {
        return value >= ranged.getMinLong() && value <= ranged.getMaxLong();
    }

    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static boolean isInRange(long value, long min, long max) {
        return value >= min && value <= max;
    }

    public static boolean isUUID(Object object) {
        if (object instanceof String) {
            String[] components = object.toString().split("-");
            return components.length == 5;
        } else {
            return object instanceof UUID;
        }
    }

    public static boolean locationEquals(Location first, Location sec) {
        if (!first.getWorld().getName().equals(sec.getWorld().getName())) {
            return false;
        } else {
            return first.getBlockX() == sec.getBlockX() && first.getBlockY() == sec.getBlockY() && first.getBlockZ() == sec.getBlockZ();
        }
    }

    public static <T> boolean listEquals(List<T> first, List<T> second) {
        if (first == null && second == null) {
            return true;
        } else if (first == null) {
            return false;
        } else if (second == null) {
            return false;
        } else if (first.size() != second.size()) {
            return false;
        } else {
            for(int i = 0; i < first.size(); ++i) {
                T f = first.get(i);
                T s = second.get(i);
                if (f == null && s != null) {
                    return false;
                }

                if (f != null && s == null) {
                    return false;
                }

                if (f != null && !f.equals(s) && !Common.stripColors(f.toString()).equalsIgnoreCase(Common.stripColors(s.toString()))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean colorlessEquals(String first, String second) {
        return Common.stripColors(first).equalsIgnoreCase(Common.stripColors(second));
    }

    public static boolean colorlessEquals(List<String> first, List<String> second) {
        return colorlessEquals(Common.toArray(first), Common.toArray(second));
    }

    public static boolean colorlessEquals(String[] firstArray, String[] secondArray) {
        for(int i = 0; i < firstArray.length; ++i) {
            String first = Common.stripColors(firstArray[i]);
            String second = i < secondArray.length ? Common.stripColors(secondArray[i]) : "";
            if (!first.equalsIgnoreCase(second)) {
                return false;
            }
        }

        return true;
    }

    public static boolean valuesEqual(Collection<String> values) {
        List<String> copy = new ArrayList(values);
        String lastValue = null;

        for(int i = 0; i < copy.size(); ++i) {
            String value = (String)copy.get(i);
            if (lastValue == null) {
                lastValue = value;
            }

            if (!lastValue.equals(value)) {
                return false;
            }

            lastValue = value;
        }

        return true;
    }

    public static boolean isInList(String element, boolean isBlacklist, Iterable<String> list) {
        return isBlacklist == isInList(element, list);
    }

    public static boolean isInList(String element, Iterable<String> list) {
        try {
            Iterator var2 = list.iterator();

            while(var2.hasNext()) {
                String matched = (String)var2.next();
                if (removeSlash(element).equalsIgnoreCase(removeSlash(matched))) {
                    return true;
                }
            }
        } catch (ClassCastException var4) {
        }

        return false;
    }

    public static boolean isInListStartsWith(String element, Iterable<String> list) {
        try {
            Iterator var2 = list.iterator();

            while(var2.hasNext()) {
                String matched = (String)var2.next();
                if (removeSlash(element).toLowerCase().startsWith(removeSlash(matched).toLowerCase())) {
                    return true;
                }
            }
        } catch (ClassCastException var4) {
        }

        return false;
    }

    /** @deprecated */
    @Deprecated
    public static boolean isInListContains(String element, Iterable<String> list) {
        try {
            Iterator var2 = list.iterator();

            while(var2.hasNext()) {
                String matched = (String)var2.next();
                if (removeSlash(element).toLowerCase().contains(removeSlash(matched).toLowerCase())) {
                    return true;
                }
            }
        } catch (ClassCastException var4) {
        }

        return false;
    }

    public static boolean isInListRegex(String element, Iterable<String> list) {
        try {
            Iterator var2 = list.iterator();

            while(var2.hasNext()) {
                String regex = (String)var2.next();
                if (Common.regExMatch(regex, element)) {
                    return true;
                }
            }
        } catch (ClassCastException var4) {
        }

        return false;
    }

    public static boolean isInListEnum(String element, Enum<?>[] enumeration) {
        Enum[] var2 = enumeration;
        int var3 = enumeration.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Enum<?> constant = var2[var4];
            if (constant.name().equalsIgnoreCase(element)) {
                return true;
            }
        }

        return false;
    }

    private static String removeSlash(String message) {
        return message.startsWith("/") ? message.substring(1) : message;
    }

    private Valid() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}