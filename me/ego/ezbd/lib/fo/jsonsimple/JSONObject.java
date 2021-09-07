package me.ego.ezbd.lib.fo.jsonsimple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JSONObject extends LinkedHashMap<Object, Object> {
    private static final long serialVersionUID = -503443796854799292L;

    public JSONObject() {
    }

    public JSONObject(Map<?, ?> map) {
        super(map);
    }

    public JSONObject(String json) throws JSONParseException {
        super((JSONObject)(new JSONParser()).parse(json));
    }

    public JSONObject(Reader reader) throws IOException, JSONParseException {
        super((JSONObject)(new JSONParser()).parse(reader));
    }

    public void write(Writer writer) throws IOException {
        JsonSimpleUtil.write(this, writer);
    }

    public JSONObject compact() {
        JSONObject object = new JSONObject();
        this.forEach((key, value) -> {
            if (value != null) {
                object.put(key, value);
            }

        });
        return object;
    }

    public JSONObject getObject(String key) {
        return JsonSimpleUtil.getObject(this.get(key));
    }

    public JSONArray getArray(String key) {
        return JsonSimpleUtil.getArray(this.get(key));
    }

    public Boolean getBoolean(String key) {
        return JsonSimpleUtil.getBoolean(this.get(key));
    }

    public Byte getByte(String key) {
        return JsonSimpleUtil.getByte(this.get(key));
    }

    public Short getShort(String key) {
        return JsonSimpleUtil.getShort(this.get(key));
    }

    public Integer getInteger(String key) {
        return JsonSimpleUtil.getInteger(this.get(key));
    }

    public Long getLong(String key) {
        return JsonSimpleUtil.getLong(this.get(key));
    }

    public Float getFloat(String key) {
        return JsonSimpleUtil.getFloat(this.get(key));
    }

    public Double getDouble(String key) {
        return JsonSimpleUtil.getDouble(this.get(key));
    }

    public String getString(String key) {
        return JsonSimpleUtil.getString(this.get(key));
    }

    public Date getDate(String key, DateFormat format) throws ParseException {
        return JsonSimpleUtil.getDate(this.get(key), format);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> type) {
        return JsonSimpleUtil.getEnum(this.get(key), type);
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
        if (object != null && object instanceof Map) {
            Map<?, ?> map = (Map)object;
            if (this.size() == map.size()) {
                Iterator var3 = this.entrySet().iterator();

                Object key;
                Object value;
                do {
                    if (!var3.hasNext()) {
                        return true;
                    }

                    Entry<Object, Object> thisEntry = (Entry)var3.next();
                    key = thisEntry.getKey();
                    value = thisEntry.getValue();
                } while(map.containsKey(key) && value.equals(map.get(key)));

                return false;
            }
        }

        return false;
    }

    public String toXML(String rootName) {
        StringBuilder builder = new StringBuilder();
        builder.append('<');
        builder.append(rootName);
        builder.append('>');
        this.forEach((key, value) -> {
            if (value instanceof JSONObject) {
                builder.append(((JSONObject)value).toXML(key.toString()));
            } else if (value instanceof JSONArray) {
                builder.append(((JSONArray)value).toXML(key.toString()));
            } else {
                builder.append('<');
                builder.append(key);
                builder.append('>');
                if (value != null) {
                    builder.append(String.valueOf(value));
                }

                builder.append("</");
                builder.append(key);
                builder.append('>');
            }

        });
        builder.append("</");
        builder.append(rootName);
        builder.append('>');
        return builder.toString();
    }
}
