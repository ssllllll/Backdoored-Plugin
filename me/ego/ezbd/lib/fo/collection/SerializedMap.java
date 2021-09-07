package me.ego.ezbd.lib.fo.collection;

import com.google.gson.Gson;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.Tuple;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class SerializedMap extends StrictCollection {
    private static final Gson gson = new Gson();
    private final StrictMap<String, Object> map;
    private boolean removeOnGet;

    private SerializedMap(String key, Object value) {
        this();
        this.put(key, value);
    }

    public SerializedMap() {
        super("Cannot remove '%s' as it is not in the map!", "Value '%s' is already in the map!");
        this.map = new StrictMap();
        this.removeOnGet = false;
    }

    public void merge(SerializedMap map) {
        Iterator var2 = map.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<String, Object> entry = (Entry)var2.next();
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null && !map.containsKey(key)) {
                map.put(key, value);
            }
        }

    }

    public boolean containsKey(String key) {
        return this.map.contains(key);
    }

    public SerializedMap putArray(Object... associativeArray) {
        boolean string = true;
        String lastKey = null;
        Object[] var4 = associativeArray;
        int var5 = associativeArray.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Object obj = var4[var6];
            if (string) {
                Valid.checkBoolean(obj instanceof String, "Expected String at " + obj + ", got " + obj.getClass().getSimpleName(), new Object[0]);
                lastKey = (String)obj;
            } else {
                this.map.override(lastKey, obj);
            }

            string = !string;
        }

        return this;
    }

    public SerializedMap put(@NonNull SerializedMap anotherMap) {
        if (anotherMap == null) {
            throw new NullPointerException("anotherMap is marked non-null but is null");
        } else {
            this.map.putAll(anotherMap.asMap());
            return this;
        }
    }

    public void putIfTrue(String key, @Nullable boolean value) {
        if (value) {
            this.put(key, value);
        }

    }

    public void putIfExist(String key, @Nullable Object value) {
        if (value != null) {
            this.put(key, value);
        }

    }

    public void putIf(String key, @Nullable Map<?, ?> value) {
        if (value != null && !value.isEmpty()) {
            this.put(key, value);
        } else {
            this.map.getSource().put(key, (Object)null);
        }

    }

    public void putIf(String key, @Nullable Collection<?> value) {
        if (value != null && !value.isEmpty()) {
            this.put(key, value);
        } else {
            this.map.getSource().put(key, (Object)null);
        }

    }

    public void putIf(String key, boolean value) {
        if (value) {
            this.put(key, value);
        } else {
            this.map.getSource().put(key, (Object)null);
        }

    }

    public void putIf(String key, @Nullable Object value) {
        if (value != null) {
            this.put(key, value);
        } else {
            this.map.getSource().put(key, (Object)null);
        }

    }

    public void put(String key, Object value) {
        Valid.checkNotNull(value, "Value with key '" + key + "' is null!");
        this.map.put(key, value);
    }

    public void override(String key, Object value) {
        Valid.checkNotNull(value, "Value with key '" + key + "' is null!");
        this.map.override(key, value);
    }

    public Object removeWeak(String key) {
        return this.map.removeWeak(key);
    }

    public Object remove(String key) {
        return this.map.remove(key);
    }

    public void removeByValue(Object value) {
        this.map.removeByValue(value);
    }

    public String getString(String key) {
        return this.getString(key, (String)null);
    }

    public String getString(String key, String def) {
        return (String)this.get(key, String.class, def);
    }

    public Location getLocation(String key) {
        return (Location)this.get(key, Location.class, (Object)null);
    }

    public Long getLong(String key) {
        return this.getLong(key, (Long)null);
    }

    public Long getLong(String key, Long def) {
        Number n = (Number)this.get(key, Long.class, def);
        return n != null ? n.longValue() : null;
    }

    public Integer getInteger(String key) {
        return this.getInteger(key, (Integer)null);
    }

    public Integer getInteger(String key, Integer def) {
        return (Integer)this.get(key, Integer.class, def);
    }

    public Double getDouble(String key) {
        return this.getDouble(key, (Double)null);
    }

    public Double getDouble(String key, Double def) {
        return (Double)this.get(key, Double.class, def);
    }

    public Float getFloat(String key) {
        return this.getFloat(key, (Float)null);
    }

    public Float getFloat(String key, Float def) {
        return (Float)this.get(key, Float.class, def);
    }

    public Boolean getBoolean(String key) {
        return this.getBoolean(key, (Boolean)null);
    }

    public Boolean getBoolean(String key, Boolean def) {
        return (Boolean)this.get(key, Boolean.class, def);
    }

    public CompMaterial getMaterial(String key) {
        return this.getMaterial(key, (CompMaterial)null);
    }

    public CompMaterial getMaterial(String key, CompMaterial def) {
        String raw = this.getString(key);
        return raw != null ? CompMaterial.fromString(raw) : def;
    }

    public ItemStack getItem(String key) {
        return this.getItem(key, (ItemStack)null);
    }

    public ItemStack getItem(String key, ItemStack def) {
        Object obj = this.get(key, Object.class, (Object)null);
        if (obj == null) {
            return def;
        } else if (obj instanceof ItemStack) {
            return (ItemStack)obj;
        } else {
            Map<String, Object> map = (Map)obj;
            ItemStack item = ItemStack.deserialize(map);
            Object raw = map.get("meta");
            if (raw != null) {
                if (raw instanceof ItemMeta) {
                    item.setItemMeta((ItemMeta)raw);
                } else if (raw instanceof Map) {
                    Map meta = (Map)raw;

                    try {
                        Class<?> cl = ReflectionUtil.getOBCClass("inventory." + (meta.containsKey("spawnedType") ? "CraftMetaSpawnEgg" : "CraftMetaItem"));
                        Constructor<?> c = cl.getDeclaredConstructor(Map.class);
                        c.setAccessible(true);
                        Object craftMeta = c.newInstance((Map)raw);
                        if (craftMeta instanceof ItemMeta) {
                            item.setItemMeta((ItemMeta)craftMeta);
                        }
                    } catch (Throwable var18) {
                        ItemMeta itemMeta = item.getItemMeta();
                        String display = meta.containsKey("display-name") ? (String)meta.get("display-name") : null;
                        if (display != null) {
                            itemMeta.setDisplayName(display);
                        }

                        List<String> lore = meta.containsKey("lore") ? (List)meta.get("lore") : null;
                        if (lore != null) {
                            itemMeta.setLore(lore);
                        }

                        SerializedMap enchants = meta.containsKey("enchants") ? of(meta.get("enchants")) : null;
                        if (enchants != null) {
                            Iterator var13 = enchants.entrySet().iterator();

                            while(var13.hasNext()) {
                                Entry<String, Object> entry = (Entry)var13.next();
                                Enchantment enchantment = Enchantment.getByName((String)entry.getKey());
                                int level = (Integer)entry.getValue();
                                itemMeta.addEnchant(enchantment, level, true);
                            }
                        }

                        List<String> itemFlags = meta.containsKey("ItemFlags") ? (List)meta.get("ItemFlags") : null;
                        if (itemFlags != null) {
                            Iterator var22 = itemFlags.iterator();

                            while(var22.hasNext()) {
                                String flag = (String)var22.next();

                                try {
                                    itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.valueOf(flag)});
                                } catch (Exception var17) {
                                }
                            }
                        }

                        Common.log(new String[]{"**************** NOTICE ****************", SimplePlugin.getNamed() + " manually deserialized your item.", "Item: " + item, "This is ONLY supported for basic items, items having", "special flags like monster eggs will NOT function."});
                        item.setItemMeta(itemMeta);
                    }
                }
            }

            return item;
        }
    }

    public <K, V> Tuple<K, V> getTuple(String key) {
        return this.getTuple(key, (Tuple)null);
    }

    public <K, V> Tuple<K, V> getTuple(String key, Tuple<K, V> def) {
        return (Tuple)this.get(key, Tuple.class, def);
    }

    public List<String> getStringList(String key) {
        return this.getStringList(key, (List)null);
    }

    public List<String> getStringList(String key, List<String> def) {
        List<String> list = this.getList(key, String.class);
        return list == null ? def : list;
    }

    public List<SerializedMap> getMapList(String key) {
        return this.getList(key, SerializedMap.class);
    }

    public <T> List<T> getListSafe(String key, Class<T> type) {
        List<T> list = this.getList(key, type);
        return (List)Common.getOrDefault(list, new ArrayList());
    }

    public <T> Set<T> getSetSafe(String key, Class<T> type) {
        Set<T> list = this.getSet(key, type);
        return (Set)Common.getOrDefault(list, new HashSet());
    }

    public <T> Set<T> getSet(String key, Class<T> type) {
        List<T> list = this.getList(key, type);
        return list == null ? null : new HashSet(list);
    }

    public <T> List<T> getList(String key, Class<T> type) {
        List<T> list = new ArrayList();
        if (!this.map.contains(key)) {
            return list;
        } else {
            Object rawList = this.removeOnGet ? this.map.removeWeak(key) : this.map.get(key);
            if (type == String.class && rawList instanceof String) {
                list.add(rawList);
            } else {
                Valid.checkBoolean(rawList instanceof List, "Key '" + key + "' expected to have a list, got " + rawList.getClass().getSimpleName() + " instead! Try putting '' quotes around the message!", new Object[0]);
                Iterator var5 = ((List)rawList).iterator();

                while(var5.hasNext()) {
                    Object object = var5.next();
                    list.add(object == null ? null : SerializeUtil.deserialize(type, object));
                }
            }

            return list;
        }
    }

    public SerializedMap getMap(String key) {
        Object raw = this.get(key, Object.class);
        return raw != null ? of(Common.getMapFromSection(raw)) : new SerializedMap();
    }

    public <Key, Value> LinkedHashMap<Key, Value> getMap(@NonNull String path, Class<Key> keyType, Class<Value> valueType) {
        if (path == null) {
            throw new NullPointerException("path is marked non-null but is null");
        } else {
            LinkedHashMap<Key, Value> map = new LinkedHashMap();
            Object raw = this.map.get(path);
            if (raw != null) {
                Valid.checkBoolean(raw instanceof Map || raw instanceof MemorySection, "Expected Map<" + keyType.getSimpleName() + ", " + valueType.getSimpleName() + "> at " + path + ", got " + raw.getClass(), new Object[0]);
                Iterator var6 = Common.getMapFromSection(raw).entrySet().iterator();

                while(var6.hasNext()) {
                    Entry<?, ?> entry = (Entry)var6.next();
                    Key key = SerializeUtil.deserialize(keyType, entry.getKey());
                    Value value = SerializeUtil.deserialize(valueType, entry.getValue());
                    this.checkAssignable(path, key, keyType);
                    this.checkAssignable(path, value, valueType);
                    map.put(key, value);
                }
            }

            return map;
        }
    }

    public <Key, Value> LinkedHashMap<Key, Set<Value>> getMapSet(@NonNull String path, Class<Key> keyType, Class<Value> setType) {
        if (path == null) {
            throw new NullPointerException("path is marked non-null but is null");
        } else {
            LinkedHashMap<Key, Set<Value>> map = new LinkedHashMap();
            Object raw = this.map.get(path);
            if (raw != null) {
                if (raw instanceof MemorySection) {
                    raw = Common.getMapFromSection(raw);
                }

                Valid.checkBoolean(raw instanceof Map, "Expected Map<" + keyType.getSimpleName() + ", Set<" + setType.getSimpleName() + ">> at " + path + ", got " + raw.getClass(), new Object[0]);

                Object key;
                List value;
                for(Iterator var6 = ((Map)raw).entrySet().iterator(); var6.hasNext(); map.put(key, new HashSet(value))) {
                    Entry<?, ?> entry = (Entry)var6.next();
                    key = SerializeUtil.deserialize(keyType, entry.getKey());
                    value = (List)SerializeUtil.deserialize(List.class, entry.getValue());
                    this.checkAssignable(path, key, keyType);
                    if (!value.isEmpty()) {
                        Iterator var10 = value.iterator();

                        while(var10.hasNext()) {
                            Value item = var10.next();
                            this.checkAssignable(path, item, setType);
                        }
                    }
                }
            }

            return map;
        }
    }

    private void checkAssignable(String path, Object value, Class<?> clazz) {
        if (!clazz.isAssignableFrom(value.getClass()) && !clazz.getSimpleName().equals(value.getClass().getSimpleName())) {
            throw new FoException("Malformed map! Key '" + path + "' in the map must be " + clazz.getSimpleName() + " but got " + value.getClass().getSimpleName() + ": '" + value + "'");
        }
    }

    public Object getObject(String key) {
        return this.get(key, Object.class);
    }

    public Object getObject(String key, Object def) {
        return this.get(key, Object.class, def);
    }

    public <T> T get(String key, Class<T> type) {
        return this.get(key, type, (Object)null);
    }

    public <T> T get(String key, Class<T> type, T def) {
        Object raw = this.removeOnGet ? this.map.removeWeak(key) : this.map.get(key);
        if (raw == null) {
            raw = this.getValueIgnoreCase(key);
        }

        if ("".equals(raw) && Enum.class.isAssignableFrom(type)) {
            return def;
        } else {
            return raw == null ? def : SerializeUtil.deserialize(type, raw, new Object[]{key});
        }
    }

    public Object getValueIgnoreCase(String key) {
        Iterator var2 = this.map.entrySet().iterator();

        Entry e;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            e = (Entry)var2.next();
        } while(!((String)e.getKey()).equalsIgnoreCase(key));

        return e.getValue();
    }

    public void forEach(BiConsumer<String, Object> consumer) {
        Iterator var2 = this.map.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<String, Object> e = (Entry)var2.next();
            consumer.accept(e.getKey(), e.getValue());
        }

    }

    public Entry<String, Object> firstEntry() {
        return this.isEmpty() ? null : (Entry)this.map.getSource().entrySet().iterator().next();
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    public Collection<Object> values() {
        return this.map.values();
    }

    public Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    public int size() {
        return this.map.size();
    }

    public Map<String, Object> asMap() {
        return this.map.getSource();
    }

    public Object serialize() {
        return this.map.serialize();
    }

    public String toJson() {
        Object map = this.serialize();

        try {
            return gson.toJson(map);
        } catch (Throwable var3) {
            Common.error(var3, new String[]{"Failed to serialize to json, data: " + map});
            return "{}";
        }
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public <O, N> void convert(String path, Class<O> from, Class<N> to, Function<O, N> converter) {
        Object old = this.getObject(path);
        if (old != null) {
            if (old instanceof Collection) {
                Collection<?> collection = (Collection)old;
                if (collection.isEmpty() || !from.isAssignableFrom(collection.iterator().next().getClass())) {
                    return;
                }

                List<N> newCollection = new ArrayList();
                Iterator var8 = collection.iterator();

                while(var8.hasNext()) {
                    O oldItem = var8.next();
                    newCollection.add(converter.apply(oldItem));
                }

                this.override(path, newCollection);
                Common.logNoPrefix(new String[]{"[" + SimplePlugin.getNamed() + "] Converted '" + path + "' from " + from.getSimpleName() + "[] to " + to.getSimpleName() + "[]"});
            } else if (from.isAssignableFrom(old.getClass())) {
                this.override(path, converter.apply(old));
                Common.logNoPrefix(new String[]{"[" + SimplePlugin.getNamed() + "] Converted '" + path + "' from '" + from.getSimpleName() + "' to '" + to.getSimpleName() + "'"});
            }
        }

    }

    public String toStringFormatted() {
        Map<?, ?> map = (Map)this.serialize();
        List<String> lines = new ArrayList();
        lines.add("{");
        Iterator var3 = map.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<?, ?> entry = (Entry)var3.next();
            Object value = entry.getValue();
            if (value != null && !value.toString().equals("[]") && !value.toString().equals("{}") && !value.toString().isEmpty() && !value.toString().equals("0.0") && !value.toString().equals("false")) {
                lines.add("\t'" + entry.getKey() + "' = '" + entry.getValue() + "'");
            }
        }

        lines.add("}");
        return String.join("\n", lines);
    }

    public String toString() {
        return this.serialize().toString();
    }

    public static SerializedMap of(String key, Object value) {
        return new SerializedMap(key, value);
    }

    public static SerializedMap ofArray(Object... array) {
        if (array != null && array.length == 1) {
            Object firstArgument = array[0];
            if (firstArgument instanceof SerializedMap) {
                return (SerializedMap)firstArgument;
            }

            if (firstArgument instanceof Map) {
                return of((Map)firstArgument);
            }

            if (firstArgument instanceof StrictMap) {
                return of(((StrictMap)firstArgument).getSource());
            }
        }

        SerializedMap map = new SerializedMap();
        map.putArray(array);
        return map;
    }

    public static SerializedMap of(Object object) {
        if (object instanceof SerializedMap) {
            return (SerializedMap)object;
        } else {
            return !(object instanceof Map) && !(object instanceof MemorySection) ? new SerializedMap() : of(Common.getMapFromSection(object));
        }
    }

    public static SerializedMap of(Map<String, Object> map) {
        SerializedMap serialized = new SerializedMap();
        serialized.map.clear();
        serialized.map.putAll(map);
        return serialized;
    }

    public static SerializedMap fromJson(String json) {
        SerializedMap serializedMap = new SerializedMap();

        try {
            Map<String, Object> map = (Map)gson.fromJson(json, Map.class);
            serializedMap.map.putAll(map);
        } catch (Throwable var3) {
            Common.throwError(var3, new String[]{"Failed to parse JSON from " + json});
        }

        return serializedMap;
    }

    public void setRemoveOnGet(boolean removeOnGet) {
        this.removeOnGet = removeOnGet;
    }
}
