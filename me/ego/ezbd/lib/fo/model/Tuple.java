package me.ego.ezbd.lib.fo.model;

import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.exception.FoException;

public final class Tuple<K, V> implements ConfigSerializable {
    private final K key;
    private final V value;

    public SerializedMap serialize() {
        return SerializedMap.ofArray(new Object[]{"Key", this.key, "Value", this.value});
    }

    public String toLine() {
        return this.key + " - " + this.value;
    }

    public String toString() {
        return this.toLine();
    }

    public static <K, V> Tuple<K, V> deserialize(SerializedMap map, Class<K> keyType, Class<V> valueType) {
        K key = SerializeUtil.deserialize(keyType, map.getObject("Key"));
        V value = SerializeUtil.deserialize(valueType, map.getObject("Value"));
        return new Tuple(key, value);
    }

    public static <K, V> Tuple<K, V> deserialize(@Nullable String line, Class<K> keyType, Class<V> valueType) {
        if (line == null) {
            return null;
        } else {
            String[] split = line.split(" - ");
            Valid.checkBoolean(split.length == 2, "Line must have the syntax <" + keyType.getSimpleName() + "> - <" + valueType.getSimpleName() + "> but got: " + line, new Object[0]);
            K key = SerializeUtil.deserialize(keyType, split[0]);
            V value = SerializeUtil.deserialize(valueType, split[1]);
            return new Tuple(key, value);
        }
    }

    /** @deprecated */
    @Deprecated
    public static <K, V> Tuple<K, V> deserialize(SerializedMap map) {
        throw new FoException("Tuple cannot be deserialized automatically, call Tuple#deserialize(map, keyType, valueType)");
    }

    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Tuple)) {
            return false;
        } else {
            Tuple<?, ?> other = (Tuple)o;
            Object this$key = this.getKey();
            Object other$key = other.getKey();
            if (this$key == null) {
                if (other$key != null) {
                    return false;
                }
            } else if (!this$key.equals(other$key)) {
                return false;
            }

            Object this$value = this.getValue();
            Object other$value = other.getValue();
            if (this$value == null) {
                if (other$value != null) {
                    return false;
                }
            } else if (!this$value.equals(other$value)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        int PRIME = true;
        int result = 1;
        Object $key = this.getKey();
        int result = result * 59 + ($key == null ? 43 : $key.hashCode());
        Object $value = this.getValue();
        result = result * 59 + ($value == null ? 43 : $value.hashCode());
        return result;
    }
}