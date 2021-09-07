package me.ego.ezbd.lib.fo.jsonsimple;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class JsonSimpleUtil {
    private JsonSimpleUtil() {
    }

    public static void write(byte[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static void write(short[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static void write(int[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static void write(long[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static void write(float[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static void write(double[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static void write(boolean[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static void write(char[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            writer.write("" + array[0]);

            for(int index = 1; index < array.length; ++index) {
                writer.write(",");
                writer.write("" + array[index]);
            }

            writer.write("]");
        }

    }

    public static <T> void write(T[] array, Writer writer) throws IOException {
        if (array == null) {
            writer.write("null");
        } else if (array.length == 0) {
            writer.write("[]");
        } else {
            writer.write("[");
            write(array[0], writer);

            for(int i = 1; i < array.length; ++i) {
                writer.write(",");
                write(array[i], writer);
            }

            writer.write("]");
        }

    }

    public static void write(Collection<?> collection, Writer writer) throws IOException {
        if (collection != null) {
            boolean first = true;
            Iterator<?> iterator = collection.iterator();
            writer.write(91);

            while(iterator.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    writer.write(44);
                }

                Object value = iterator.next();
                if (value == null) {
                    writer.write("null");
                } else {
                    write(value, writer);
                }
            }

            writer.write(93);
        } else {
            writer.write("null");
        }

    }

    public static void write(Map<?, ?> map, Writer writer) throws IOException {
        if (map != null) {
            boolean first = true;
            writer.write(123);
            Iterator var3 = map.entrySet().iterator();

            while(var3.hasNext()) {
                Entry<?, ?> entry = (Entry)var3.next();
                if (first) {
                    first = false;
                } else {
                    writer.write(44);
                }

                writer.write(34);
                writer.write(JSONUtil.escape(String.valueOf(entry.getKey())));
                writer.write(34);
                writer.write(58);
                write(entry.getValue(), writer);
            }

            writer.write(125);
        } else {
            writer.write("null");
        }

    }

    public static void write(Object value, Writer writer) throws IOException {
        if (value == null) {
            writer.write("null");
        } else if (value instanceof String) {
            writer.write(34);
            writer.write(JSONUtil.escape((String)value));
            writer.write(34);
        } else if (value instanceof Double) {
            writer.write(!((Double)value).isInfinite() && !((Double)value).isNaN() ? value.toString() : "null");
        } else if (value instanceof Float) {
            writer.write(!((Float)value).isInfinite() && !((Float)value).isNaN() ? value.toString() : "null");
        } else if (value instanceof Number) {
            writer.write(value.toString());
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof JSONObject) {
            ((JSONObject)value).write(writer);
        } else if (value instanceof JSONArray) {
            ((JSONArray)value).write(writer);
        } else if (value instanceof Map) {
            write((Map)value, writer);
        } else if (value instanceof Collection) {
            write((Collection)value, writer);
        } else if (value instanceof byte[]) {
            write((byte[])((byte[])value), writer);
        } else if (value instanceof short[]) {
            write((short[])((short[])value), writer);
        } else if (value instanceof int[]) {
            write((int[])((int[])value), writer);
        } else if (value instanceof long[]) {
            write((long[])((long[])value), writer);
        } else if (value instanceof float[]) {
            write((float[])((float[])value), writer);
        } else if (value instanceof double[]) {
            write((double[])((double[])value), writer);
        } else if (value instanceof boolean[]) {
            write((boolean[])((boolean[])value), writer);
        } else if (value instanceof char[]) {
            write((char[])((char[])value), writer);
        } else if (value.getClass().isArray()) {
            write((Object[])((Object[])value), writer);
        } else {
            writer.write(34);
            writer.write(JSONUtil.escape(value.toString()));
            writer.write(34);
        }

    }

    public static JSONObject getObject(Object value) {
        if (value != null) {
            if (value instanceof JSONObject) {
                return (JSONObject)value;
            }

            if (value instanceof Map) {
                return new JSONObject((Map)value);
            }
        }

        return null;
    }

    public static JSONArray getArray(Object value) {
        if (value != null) {
            if (value instanceof JSONArray) {
                return (JSONArray)value;
            }

            if (value instanceof boolean[]) {
                return new JSONArray((boolean[])((boolean[])value));
            }

            if (value instanceof byte[]) {
                return new JSONArray((byte[])((byte[])value));
            }

            if (value instanceof char[]) {
                return new JSONArray((char[])((char[])value));
            }

            if (value instanceof short[]) {
                return new JSONArray((short[])((short[])value));
            }

            if (value instanceof int[]) {
                return new JSONArray((int[])((int[])value));
            }

            if (value instanceof long[]) {
                return new JSONArray((long[])((long[])value));
            }

            if (value instanceof float[]) {
                return new JSONArray((float[])((float[])value));
            }

            if (value instanceof double[]) {
                return new JSONArray((double[])((double[])value));
            }

            if (value instanceof Collection) {
                return new JSONArray((Collection)value);
            }

            if (value.getClass().isArray()) {
                return new JSONArray(value);
            }
        }

        return null;
    }

    public static Boolean getBoolean(Object value) {
        if (value != null) {
            if (value instanceof Boolean) {
                return (Boolean)value;
            }

            if (value instanceof String) {
                return Boolean.parseBoolean((String)value);
            }

            if (value instanceof Number) {
                return ((Number)value).longValue() == 1L;
            }
        }

        return null;
    }

    public static Byte getByte(Object value) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).byteValue();
            }

            if (value instanceof String) {
                return Byte.parseByte((String)value);
            }

            if (value instanceof Boolean) {
                return Byte.valueOf((byte)((Boolean)value ? 1 : 0));
            }
        }

        return null;
    }

    public static Short getShort(Object value) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).shortValue();
            }

            if (value instanceof String) {
                return Short.parseShort((String)value);
            }

            if (value instanceof Boolean) {
                return Short.valueOf((short)((Boolean)value ? 1 : 0));
            }
        }

        return null;
    }

    public static Integer getInteger(Object value) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).intValue();
            }

            if (value instanceof String) {
                return Integer.parseInt((String)value);
            }

            if (value instanceof Boolean) {
                return (Boolean)value ? 1 : 0;
            }
        }

        return null;
    }

    public static Long getLong(Object value) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).longValue();
            }

            if (value instanceof String) {
                return Long.parseLong((String)value);
            }

            if (value instanceof Boolean) {
                return (Boolean)value ? 1L : 0L;
            }
        }

        return null;
    }

    public static Float getFloat(Object value) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).floatValue();
            }

            if (value instanceof String) {
                return Float.parseFloat((String)value);
            }

            if (value instanceof Boolean) {
                return (Boolean)value ? 1.0F : 0.0F;
            }
        }

        return null;
    }

    public static Double getDouble(Object value) {
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).doubleValue();
            }

            if (value instanceof String) {
                return Double.parseDouble((String)value);
            }

            if (value instanceof Boolean) {
                return (Boolean)value ? 1.0D : 0.0D;
            }
        }

        return null;
    }

    public static String getString(Object value) {
        return value != null ? value.toString() : null;
    }

    public static Date getDate(Object value, DateFormat format) throws ParseException {
        if (value != null) {
            return value instanceof Date ? (Date)value : format.parse(value.toString());
        } else {
            return null;
        }
    }

    public static <T extends Enum<T>> T getEnum(Object value, Class<T> type) {
        if (value != null) {
            Enum[] var2 = (Enum[])type.getEnumConstants();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object enumConstant = var2[var4];
                if (((Enum)enumConstant).name().equals(value.toString())) {
                    return (Enum)enumConstant;
                }
            }
        }

        return null;
    }

    public static void escape(String string, StringBuilder builder) {
        for(int index = 0; index < string.length(); ++index) {
            char character = string.charAt(index);
            if (character == '"') {
                builder.append("\\\"");
            } else if (character == '\\') {
                builder.append("\\\\");
            } else if (character == '\b') {
                builder.append("\\b");
            } else if (character == '\f') {
                builder.append("\\f");
            } else if (character == '\n') {
                builder.append("\\n");
            } else if (character == '\r') {
                builder.append("\\r");
            } else if (character == '\t') {
                builder.append("\\t");
            } else if (character == '/') {
                builder.append("\\/");
            } else if (character >= 0 && character <= 31 || character >= 127 && character <= 159 || character >= 8192 && character <= 8447) {
                String hex = Integer.toHexString(character);
                builder.append("\\u");

                for(int k = 0; k < 4 - hex.length(); ++k) {
                    builder.append('0');
                }

                builder.append(hex.toUpperCase());
            } else {
                builder.append(character);
            }
        }

    }
}