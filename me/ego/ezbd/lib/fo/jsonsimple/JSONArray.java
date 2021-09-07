package me.ego.ezbd.lib.fo.jsonsimple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class JSONArray extends ArrayList<Object> {
    private static final long serialVersionUID = 3957988303675231981L;

    public JSONArray() {
    }

    public JSONArray(Collection<?> collection) {
        super(collection);
    }

    public <T> JSONArray(T[] array) {
        Object[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            T element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(byte[] array) {
        byte[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(boolean[] array) {
        boolean[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            boolean element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(char[] array) {
        char[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            char element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(short[] array) {
        short[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            short element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(int[] array) {
        int[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(long[] array) {
        long[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            long element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(float[] array) {
        float[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            float element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(double[] array) {
        double[] var2 = array;
        int var3 = array.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            double element = var2[var4];
            this.add(element);
        }

    }

    public JSONArray(Object array) {
        if (array != null && array.getClass().isArray()) {
            int length = Array.getLength(array);

            for(int index = 0; index < length; ++index) {
                this.add(Array.get(array, index));
            }
        }

    }

    public JSONArray(String json) throws JSONParseException {
        super((JSONArray)(new JSONParser()).parse(json));
    }

    public JSONArray(Reader reader) throws JSONParseException, IOException {
        super((JSONArray)(new JSONParser()).parse(reader));
    }

    public void write(Writer writer) throws IOException {
        JsonSimpleUtil.write(this, writer);
    }

    public String toString() {
        try {
            StringWriter writer = new StringWriter();
            Throwable var2 = null;

            String var3;
            try {
                JsonSimpleUtil.write(this, writer);
                var3 = writer.toString();
            } catch (Throwable var13) {
                var2 = var13;
                throw var13;
            } finally {
                if (writer != null) {
                    if (var2 != null) {
                        try {
                            writer.close();
                        } catch (Throwable var12) {
                            var2.addSuppressed(var12);
                        }
                    } else {
                        writer.close();
                    }
                }

            }

            return var3;
        } catch (IOException var15) {
            return null;
        }
    }

    public boolean equals(Object object) {
        if (object != null) {
            int index;
            if (object instanceof Collection) {
                Collection<?> collection = (Collection)object;
                if (collection.size() == this.size()) {
                    index = 0;

                    for(Iterator var4 = collection.iterator(); var4.hasNext(); ++index) {
                        Object element = var4.next();
                        if ((element != null || this.get(index) != null) && !this.get(index).equals(element)) {
                            return false;
                        }
                    }

                    return true;
                }
            } else if (object.getClass().isArray()) {
                int length = Array.getLength(object);
                if (length == this.size()) {
                    for(index = 0; index < length; ++index) {
                        Object element = Array.get(object, index);
                        if ((element != null || this.get(index) != null) && !element.equals(this.get(index))) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public JSONObject getObject(int index) {
        return JsonSimpleUtil.getObject(this.get(index));
    }

    public JSONArray getArray(int index) {
        return JsonSimpleUtil.getArray(this.get(index));
    }

    public Boolean getBoolean(int index) {
        return JsonSimpleUtil.getBoolean(this.get(index));
    }

    public Byte getByte(int index) {
        return JsonSimpleUtil.getByte(this.get(index));
    }

    public Short getShort(int index) {
        return JsonSimpleUtil.getShort(this.get(index));
    }

    public Integer getInteger(int index) {
        return JsonSimpleUtil.getInteger(this.get(index));
    }

    public Long getLong(int index) {
        return JsonSimpleUtil.getLong(this.get(index));
    }

    public Float getFloat(int index) {
        return JsonSimpleUtil.getFloat(this.get(index));
    }

    public Double getDouble(int index) {
        return JsonSimpleUtil.getDouble(this.get(index));
    }

    public String getString(int index) {
        return JsonSimpleUtil.getString(this.get(index));
    }

    public Date getDate(int index, DateFormat format) throws ParseException {
        return JsonSimpleUtil.getDate(this.get(index), format);
    }

    public <T extends Enum<T>> T getEnum(int index, Class<T> type) {
        return JsonSimpleUtil.getEnum(this.get(index), type);
    }

    public Boolean[] toBooleanArray() {
        Boolean[] array = new Boolean[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getBoolean(index);
        }

        return array;
    }

    public Byte[] toByteArray() {
        Byte[] array = new Byte[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getByte(index);
        }

        return array;
    }

    public Short[] toShortArray() {
        Short[] array = new Short[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getShort(index);
        }

        return array;
    }

    public Integer[] toIntegerArray() {
        Integer[] array = new Integer[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getInteger(index);
        }

        return array;
    }

    public Long[] toLongArray() {
        Long[] array = new Long[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getLong(index);
        }

        return array;
    }

    public Float[] toFloatArray() {
        Float[] array = new Float[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getFloat(index);
        }

        return array;
    }

    public Double[] toDoubleArray() {
        Double[] array = new Double[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getDouble(index);
        }

        return array;
    }

    public String[] toStringArray() {
        String[] array = new String[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getString(index);
        }

        return array;
    }

    public JSONObject[] toObjectArray() {
        JSONObject[] array = new JSONObject[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getObject(index);
        }

        return array;
    }

    public JSONArray[] toArrayArray() {
        JSONArray[] array = new JSONArray[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getArray(index);
        }

        return array;
    }

    public byte[] toPrimitiveByteArray() {
        byte[] array = new byte[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getByte(index);
        }

        return array;
    }

    public short[] toPrimitiveShortArray() {
        short[] array = new short[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getShort(index);
        }

        return array;
    }

    public int[] toPrimitiveIntArray() {
        int[] array = new int[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getInteger(index);
        }

        return array;
    }

    public long[] toPrimitiveLongArray() {
        long[] array = new long[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getLong(index);
        }

        return array;
    }

    public float[] toPrimitiveFloatArray() {
        float[] array = new float[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getFloat(index);
        }

        return array;
    }

    public double[] toPrimitiveDoubleArray() {
        double[] array = new double[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getDouble(index);
        }

        return array;
    }

    public boolean[] toPrimitiveBooleanArray() {
        boolean[] array = new boolean[this.size()];

        for(int index = 0; index < array.length; ++index) {
            array[index] = this.getBoolean(index);
        }

        return array;
    }

    public String toXML(String rootName) {
        StringBuilder builder = new StringBuilder();
        builder.append('<');
        builder.append(rootName);
        builder.append(" length=");
        builder.append(this.size());
        builder.append('>');
        Iterator var3 = this.iterator();

        while(var3.hasNext()) {
            Object element = var3.next();
            if (element instanceof JSONObject) {
                builder.append(((JSONObject)element).toXML("item"));
            } else if (element instanceof JSONArray) {
                builder.append(((JSONArray)element).toXML("item"));
            } else {
                builder.append("<item>");
                if (element != null) {
                    builder.append(String.valueOf(element));
                }

                builder.append("</item>");
            }
        }

        builder.append("</");
        builder.append(rootName);
        builder.append('>');
        return builder.toString();
    }
}
