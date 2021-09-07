package me.ego.ezbd.lib.fo.jsonsimple;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import me.ego.ezbd.lib.fo.jsonsimple.annotation.JSONAttribute;
import me.ego.ezbd.lib.fo.jsonsimple.annotation.JSONRoot;
import me.ego.ezbd.lib.fo.jsonsimple.annotation.JSONAttribute.Type;

public final class JSONSerializer {
    private JSONSerializer() {
    }

    public static void deserialize(JSONObject json, Object object) throws Exception {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getFields();
        Method[] methods = clazz.getMethods();
        Field[] var5 = fields;
        int var6 = fields.length;

        int var7;
        JSONAttribute annotation;
        Class targetType;
        Object value;
        Object newArray;
        int var13;
        int var14;
        Object constant;
        Map map;
        Collection collection;
        Object[] var20;
        for(var7 = 0; var7 < var6; ++var7) {
            Field field = var5[var7];
            annotation = (JSONAttribute)field.getAnnotation(JSONAttribute.class);
            if (!Modifier.isTransient(field.getModifiers()) && annotation != null && contains(Type.SETTER, annotation)) {
                targetType = field.getType();
                value = json.get(annotation.name());
                if (value != null) {
                    if (!Float.TYPE.isAssignableFrom(targetType) && !Float.class.isAssignableFrom(targetType)) {
                        if (!Double.TYPE.isAssignableFrom(targetType) && !Double.class.isAssignableFrom(targetType)) {
                            if (!Byte.TYPE.isAssignableFrom(targetType) && !Byte.class.isAssignableFrom(targetType)) {
                                if (!Short.TYPE.isAssignableFrom(targetType) && !Short.class.isAssignableFrom(targetType)) {
                                    if (!Integer.TYPE.isAssignableFrom(targetType) && !Integer.class.isAssignableFrom(targetType)) {
                                        if (!Long.TYPE.isAssignableFrom(targetType) && !Long.class.isAssignableFrom(targetType)) {
                                            if (!String.class.isAssignableFrom(targetType) && !Number.class.isAssignableFrom(targetType) && !Boolean.class.isAssignableFrom(targetType) && !Boolean.TYPE.isAssignableFrom(targetType)) {
                                                if (Enum.class.isAssignableFrom(targetType)) {
                                                    var20 = targetType.getEnumConstants();
                                                    var13 = var20.length;

                                                    for(var14 = 0; var14 < var13; ++var14) {
                                                        constant = var20[var14];
                                                        if (((Enum)constant).name().equals(value.toString())) {
                                                            field.set(object, constant);
                                                            break;
                                                        }
                                                    }
                                                } else if (targetType.getAnnotation(JSONRoot.class) != null) {
                                                    newArray = targetType.getDeclaredConstructor().newInstance();
                                                    deserialize((JSONObject)value, newArray);
                                                    field.set(object, newArray);
                                                } else if (Map.class.isAssignableFrom(targetType)) {
                                                    map = (Map)targetType.getDeclaredConstructor().newInstance();
                                                    deserialize(clazz, object, map, (JSONObject)value);
                                                    field.set(object, map);
                                                } else if (Collection.class.isAssignableFrom(targetType)) {
                                                    collection = (Collection)targetType.getDeclaredConstructor().newInstance();
                                                    deserialize(clazz, object, collection, (JSONArray)value);
                                                    field.set(object, collection);
                                                } else if (targetType.isArray()) {
                                                    newArray = Array.newInstance(targetType.getComponentType(), ((JSONArray)value).size());
                                                    deserialize(clazz, object, newArray, (JSONArray)value);
                                                    field.set(object, newArray);
                                                } else if (JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                                                    field.set(object, ((JSONTypeSerializationHandler)object).deserialize(targetType, value));
                                                }
                                            } else {
                                                field.set(object, value);
                                            }
                                        } else {
                                            field.set(object, ((Number)value).longValue());
                                        }
                                    } else {
                                        field.set(object, ((Number)value).intValue());
                                    }
                                } else {
                                    field.set(object, ((Number)value).shortValue());
                                }
                            } else {
                                field.set(object, ((Number)value).byteValue());
                            }
                        } else {
                            field.set(object, ((Number)value).doubleValue());
                        }
                    } else {
                        field.set(object, ((Number)value).floatValue());
                    }
                }
            }
        }

        Method[] var16 = methods;
        var6 = methods.length;

        for(var7 = 0; var7 < var6; ++var7) {
            Method method = var16[var7];
            annotation = (JSONAttribute)method.getAnnotation(JSONAttribute.class);
            if (!Modifier.isTransient(method.getModifiers()) && annotation != null && contains(Type.SETTER, annotation)) {
                targetType = method.getParameterTypes()[0];
                if (targetType.isInterface()) {
                    throw new Exception("Cannot deserialize an interface! Method: " + method.getName() + ", Interface: " + targetType.getName());
                }

                value = json.get(annotation.name());
                if (value != null) {
                    if (!Float.TYPE.isAssignableFrom(targetType) && !Float.class.isAssignableFrom(targetType)) {
                        if (!Double.TYPE.isAssignableFrom(targetType) && !Double.class.isAssignableFrom(targetType)) {
                            if (!Byte.TYPE.isAssignableFrom(targetType) && !Byte.class.isAssignableFrom(targetType)) {
                                if (!Short.TYPE.isAssignableFrom(targetType) && !Short.class.isAssignableFrom(targetType)) {
                                    if (!Integer.TYPE.isAssignableFrom(targetType) && !Integer.class.isAssignableFrom(targetType)) {
                                        if (!Long.TYPE.isAssignableFrom(targetType) && !Long.class.isAssignableFrom(targetType)) {
                                            if (!String.class.isAssignableFrom(targetType) && !Number.class.isAssignableFrom(targetType) && !Boolean.class.isAssignableFrom(targetType) && !Boolean.TYPE.isAssignableFrom(targetType)) {
                                                if (Enum.class.isAssignableFrom(targetType)) {
                                                    var20 = targetType.getEnumConstants();
                                                    var13 = var20.length;

                                                    for(var14 = 0; var14 < var13; ++var14) {
                                                        constant = var20[var14];
                                                        if (((Enum)constant).name().equals(value.toString())) {
                                                            method.invoke(object, constant);
                                                            break;
                                                        }
                                                    }
                                                } else if (targetType.getAnnotation(JSONRoot.class) != null) {
                                                    newArray = targetType.getDeclaredConstructor().newInstance();
                                                    deserialize((JSONObject)value, newArray);
                                                    method.invoke(object, newArray);
                                                } else if (Map.class.isAssignableFrom(targetType)) {
                                                    map = (Map)targetType.getDeclaredConstructor().newInstance();
                                                    deserialize(clazz, object, map, (JSONObject)value);
                                                    method.invoke(object, map);
                                                } else if (Collection.class.isAssignableFrom(targetType)) {
                                                    collection = (Collection)targetType.getDeclaredConstructor().newInstance();
                                                    deserialize(clazz, object, collection, (JSONArray)value);
                                                    method.invoke(object, collection);
                                                } else if (targetType.isArray()) {
                                                    newArray = Array.newInstance(targetType.getComponentType(), ((JSONArray)value).size());
                                                    deserialize(clazz, object, newArray, (JSONArray)value);
                                                    method.invoke(object, newArray);
                                                } else if (JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                                                    method.invoke(object, ((JSONTypeSerializationHandler)object).deserialize(targetType, value));
                                                }
                                            } else {
                                                method.invoke(object, value);
                                            }
                                        } else {
                                            method.invoke(object, ((Number)value).longValue());
                                        }
                                    } else {
                                        method.invoke(object, ((Number)value).intValue());
                                    }
                                } else {
                                    method.invoke(object, ((Number)value).shortValue());
                                }
                            } else {
                                method.invoke(object, ((Number)value).byteValue());
                            }
                        } else {
                            method.invoke(object, ((Number)value).doubleValue());
                        }
                    } else {
                        method.invoke(object, ((Number)value).floatValue());
                    }
                }
            }
        }

    }

    private static void deserialize(Class<?> clazz, Object classObject, Collection<Object> collection, JSONArray json) throws Exception {
        Iterator var4 = json.iterator();

        while(true) {
            while(true) {
                while(true) {
                    while(true) {
                        while(true) {
                            while(true) {
                                while(true) {
                                    while(true) {
                                        while(true) {
                                            while(true) {
                                                while(var4.hasNext()) {
                                                    Object value = var4.next();
                                                    if (value != null) {
                                                        Class<?> type = value.getClass();
                                                        if (type.isInterface()) {
                                                            throw new Exception("Cannot deserialize an interface! Interface: " + type.getName());
                                                        }

                                                        if (!Float.TYPE.isAssignableFrom(type) && !Float.class.isAssignableFrom(type)) {
                                                            if (!Double.TYPE.isAssignableFrom(type) && !Double.class.isAssignableFrom(type)) {
                                                                if (!Byte.TYPE.isAssignableFrom(type) && !Byte.class.isAssignableFrom(type)) {
                                                                    if (!Short.TYPE.isAssignableFrom(type) && !Short.class.isAssignableFrom(type)) {
                                                                        if (!Integer.TYPE.isAssignableFrom(type) && !Integer.class.isAssignableFrom(type)) {
                                                                            if (!Long.TYPE.isAssignableFrom(type) && !Long.class.isAssignableFrom(type)) {
                                                                                if (!String.class.isAssignableFrom(type) && !Number.class.isAssignableFrom(type) && !Boolean.class.isAssignableFrom(type) && !Boolean.TYPE.isAssignableFrom(type)) {
                                                                                    if (Enum.class.isAssignableFrom(type)) {
                                                                                        Object[] var13 = type.getEnumConstants();
                                                                                        int var8 = var13.length;

                                                                                        for(int var9 = 0; var9 < var8; ++var9) {
                                                                                            Object constant = var13[var9];
                                                                                            if (((Enum)constant).name().equals(value.toString())) {
                                                                                                collection.add(constant);
                                                                                                break;
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        Object newArray;
                                                                                        if (type.getAnnotation(JSONRoot.class) != null) {
                                                                                            newArray = type.getDeclaredConstructor().newInstance();
                                                                                            deserialize((JSONObject)value, newArray);
                                                                                            collection.add(newArray);
                                                                                        } else if (Map.class.isAssignableFrom(type)) {
                                                                                            Map<Object, Object> newMap = (Map)type.getDeclaredConstructor().newInstance();
                                                                                            deserialize(clazz, classObject, newMap, (JSONObject)value);
                                                                                            collection.add(newMap);
                                                                                        } else if (Collection.class.isAssignableFrom(type)) {
                                                                                            Collection<Object> newCollection = (Collection)type.getDeclaredConstructor().newInstance();
                                                                                            deserialize(clazz, classObject, newCollection, (JSONArray)value);
                                                                                            collection.add(newCollection);
                                                                                        } else if (type.isArray()) {
                                                                                            newArray = Array.newInstance(type.getComponentType(), ((JSONArray)value).size());
                                                                                            deserialize(clazz, classObject, newArray, (JSONArray)value);
                                                                                            collection.add(newArray);
                                                                                        } else if (JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                                                                                            collection.add(((JSONTypeSerializationHandler)classObject).deserialize(type, value));
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    collection.add(value);
                                                                                }
                                                                            } else {
                                                                                collection.add(((Number)value).longValue());
                                                                            }
                                                                        } else {
                                                                            collection.add(((Number)value).intValue());
                                                                        }
                                                                    } else {
                                                                        collection.add(((Number)value).shortValue());
                                                                    }
                                                                } else {
                                                                    collection.add(((Number)value).byteValue());
                                                                }
                                                            } else {
                                                                collection.add(((Number)value).doubleValue());
                                                            }
                                                        } else {
                                                            collection.add(((Number)value).floatValue());
                                                        }
                                                    } else {
                                                        collection.add(value);
                                                    }
                                                }

                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void deserialize(Class<?> clazz, Object classObject, Map<Object, Object> map, JSONObject json) throws Exception {
        Iterator var4 = json.entrySet().iterator();

        while(true) {
            while(true) {
                while(true) {
                    while(true) {
                        while(true) {
                            while(true) {
                                while(true) {
                                    while(true) {
                                        while(true) {
                                            while(true) {
                                                while(var4.hasNext()) {
                                                    Object jsonEntry = var4.next();
                                                    Entry<?, ?> entry = (Entry)jsonEntry;
                                                    Object value = entry.getValue();
                                                    if (value != null) {
                                                        Class<?> type = value.getClass();
                                                        if (type.isInterface()) {
                                                            throw new Exception("Cannot deserialize an interface! Interface: " + type.getName());
                                                        }

                                                        if (!Float.TYPE.isAssignableFrom(type) && !Float.class.isAssignableFrom(type)) {
                                                            if (!Double.TYPE.isAssignableFrom(type) && !Double.class.isAssignableFrom(type)) {
                                                                if (!Byte.TYPE.isAssignableFrom(type) && !Byte.class.isAssignableFrom(type)) {
                                                                    if (!Short.TYPE.isAssignableFrom(type) && !Short.class.isAssignableFrom(type)) {
                                                                        if (!Integer.TYPE.isAssignableFrom(type) && !Integer.class.isAssignableFrom(type)) {
                                                                            if (!Long.TYPE.isAssignableFrom(type) && !Long.class.isAssignableFrom(type)) {
                                                                                if (!String.class.isAssignableFrom(type) && !Number.class.isAssignableFrom(type) && !Boolean.class.isAssignableFrom(type) && !Boolean.TYPE.isAssignableFrom(type)) {
                                                                                    if (Enum.class.isAssignableFrom(type)) {
                                                                                        Object[] var15 = type.getEnumConstants();
                                                                                        int var10 = var15.length;

                                                                                        for(int var11 = 0; var11 < var10; ++var11) {
                                                                                            Object constant = var15[var11];
                                                                                            if (((Enum)constant).name().equals(value.toString())) {
                                                                                                map.put(entry.getKey(), constant);
                                                                                                break;
                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        Object newArray;
                                                                                        if (type.getAnnotation(JSONRoot.class) != null) {
                                                                                            newArray = type.getDeclaredConstructor().newInstance();
                                                                                            deserialize((JSONObject)value, newArray);
                                                                                            map.put(entry.getKey(), newArray);
                                                                                        } else if (Map.class.isAssignableFrom(type)) {
                                                                                            Map<Object, Object> newMap = (Map)type.getDeclaredConstructor().newInstance();
                                                                                            deserialize(clazz, classObject, newMap, (JSONObject)value);
                                                                                            map.put(entry.getKey(), newMap);
                                                                                        } else if (Collection.class.isAssignableFrom(type)) {
                                                                                            Collection<Object> collection = (Collection)type.getDeclaredConstructor().newInstance();
                                                                                            deserialize(clazz, classObject, collection, (JSONArray)value);
                                                                                            map.put(entry.getKey(), collection);
                                                                                        } else if (type.isArray()) {
                                                                                            newArray = Array.newInstance(type.getComponentType(), ((JSONArray)value).size());
                                                                                            deserialize(clazz, classObject, newArray, (JSONArray)value);
                                                                                            map.put(entry.getKey(), newArray);
                                                                                        } else if (JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                                                                                            map.put(entry.getKey(), ((JSONTypeSerializationHandler)classObject).deserialize(type, value));
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    map.put(entry.getKey(), value);
                                                                                }
                                                                            } else {
                                                                                map.put(entry.getKey(), ((Number)value).longValue());
                                                                            }
                                                                        } else {
                                                                            map.put(entry.getKey(), ((Number)value).intValue());
                                                                        }
                                                                    } else {
                                                                        map.put(entry.getKey(), ((Number)value).shortValue());
                                                                    }
                                                                } else {
                                                                    map.put(entry.getKey(), ((Number)value).byteValue());
                                                                }
                                                            } else {
                                                                map.put(entry.getKey(), ((Number)value).doubleValue());
                                                            }
                                                        } else {
                                                            map.put(entry.getKey(), ((Number)value).floatValue());
                                                        }
                                                    } else {
                                                        map.put(entry.getKey(), value);
                                                    }
                                                }

                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void deserialize(Class<?> clazz, Object classObject, Object targetObject, JSONArray json) throws Exception {
        Class<?> type = targetObject.getClass().getComponentType();
        if (type.isInterface()) {
            throw new Exception("Cannot deserialize an interface! Interface: " + type.getName());
        } else {
            for(int index = 0; index < json.size(); ++index) {
                Object value = json.get(index);
                if (!Float.TYPE.isAssignableFrom(type) && !Float.class.isAssignableFrom(type)) {
                    if (!Double.TYPE.isAssignableFrom(type) && !Double.class.isAssignableFrom(type)) {
                        if (!Byte.TYPE.isAssignableFrom(type) && !Byte.class.isAssignableFrom(type)) {
                            if (!Short.TYPE.isAssignableFrom(type) && !Short.class.isAssignableFrom(type)) {
                                if (!Integer.TYPE.isAssignableFrom(type) && !Integer.class.isAssignableFrom(type)) {
                                    if (!Long.TYPE.isAssignableFrom(type) && !Long.class.isAssignableFrom(type)) {
                                        if (!String.class.isAssignableFrom(type) && !Number.class.isAssignableFrom(type) && !Boolean.class.isAssignableFrom(type) && !Boolean.TYPE.isAssignableFrom(type)) {
                                            if (Enum.class.isAssignableFrom(type)) {
                                                Object[] var13 = type.getEnumConstants();
                                                int var8 = var13.length;

                                                for(int var9 = 0; var9 < var8; ++var9) {
                                                    Object constant = var13[var9];
                                                    if (((Enum)constant).name().equals(value.toString())) {
                                                        Array.set(targetObject, index, constant);
                                                        break;
                                                    }
                                                }
                                            } else {
                                                Object newArray;
                                                if (type.getAnnotation(JSONRoot.class) != null) {
                                                    newArray = type.getDeclaredConstructor().newInstance();
                                                    deserialize((JSONObject)value, newArray);
                                                    Array.set(targetObject, index, newArray);
                                                } else if (Map.class.isAssignableFrom(type)) {
                                                    Map<Object, Object> map = (Map)type.getDeclaredConstructor().newInstance();
                                                    deserialize(clazz, classObject, map, (JSONObject)value);
                                                    Array.set(targetObject, index, map);
                                                } else if (Collection.class.isAssignableFrom(type)) {
                                                    Collection<Object> collection = (Collection)type.getDeclaredConstructor().newInstance();
                                                    deserialize(clazz, classObject, collection, (JSONArray)value);
                                                    Array.set(targetObject, index, collection);
                                                } else if (type.isArray()) {
                                                    newArray = Array.newInstance(type.getComponentType(), ((JSONArray)value).size());
                                                    deserialize(clazz, classObject, newArray, (JSONArray)value);
                                                    Array.set(targetObject, index, newArray);
                                                } else if (JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                                                    Array.set(targetObject, index, ((JSONTypeSerializationHandler)classObject).deserialize(type, value));
                                                }
                                            }
                                        } else {
                                            Array.set(targetObject, index, value);
                                        }
                                    } else {
                                        Array.set(targetObject, index, ((Number)value).longValue());
                                    }
                                } else {
                                    Array.set(targetObject, index, ((Number)value).intValue());
                                }
                            } else {
                                Array.set(targetObject, index, ((Number)value).shortValue());
                            }
                        } else {
                            Array.set(targetObject, index, ((Number)value).byteValue());
                        }
                    } else {
                        Array.set(targetObject, index, ((Number)value).doubleValue());
                    }
                } else {
                    Array.set(targetObject, index, ((Number)value).floatValue());
                }
            }

        }
    }

    public static JSONObject serialize(Object object) throws Exception {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getFields();
        Method[] methods = clazz.getMethods();
        JSONObject json = new JSONObject();
        Field[] var5 = fields;
        int var6 = fields.length;

        int var7;
        JSONAttribute annotation;
        for(var7 = 0; var7 < var6; ++var7) {
            Field field = var5[var7];
            annotation = (JSONAttribute)field.getAnnotation(JSONAttribute.class);
            if (!Modifier.isTransient(field.getModifiers()) && annotation != null && contains(Type.GETTER, annotation)) {
                serialize(json, clazz, object, field.getType(), field.get(object), annotation);
            }
        }

        Method[] var10 = methods;
        var6 = methods.length;

        for(var7 = 0; var7 < var6; ++var7) {
            Method method = var10[var7];
            annotation = (JSONAttribute)method.getAnnotation(JSONAttribute.class);
            if (!Modifier.isTransient(method.getModifiers()) && annotation != null && contains(Type.GETTER, annotation)) {
                serialize(json, clazz, object, method.getReturnType(), method.invoke(object), annotation);
            }
        }

        return json;
    }

    private static void serialize(JSONObject json, Class<?> clazz, Object classObject, Class<?> type, Object value, JSONAttribute annotation) throws Exception {
        if (!(value instanceof String) && !(value instanceof Boolean)) {
            if (value instanceof Number) {
                json.put(annotation.name(), value != null ? ((Number)value).longValue() : null);
            } else if (value instanceof boolean[]) {
                json.put(annotation.name(), new JSONArray((boolean[])((boolean[])value)));
            } else if (value instanceof byte[]) {
                json.put(annotation.name(), new JSONArray((byte[])((byte[])value)));
            } else if (value instanceof short[]) {
                json.put(annotation.name(), new JSONArray((short[])((short[])value)));
            } else if (value instanceof int[]) {
                json.put(annotation.name(), new JSONArray((int[])((int[])value)));
            } else if (value instanceof long[]) {
                json.put(annotation.name(), new JSONArray((long[])((long[])value)));
            } else if (value instanceof float[]) {
                json.put(annotation.name(), new JSONArray((float[])((float[])value)));
            } else if (value instanceof double[]) {
                json.put(annotation.name(), new JSONArray((double[])((double[])value)));
            } else if (value != null && value.getClass().isArray()) {
                json.put(annotation.name(), serializeArray(value, clazz, classObject));
            } else if (value != null && value.getClass().getAnnotation(JSONRoot.class) != null) {
                json.put(annotation.name(), serialize(value));
            } else if (value instanceof Collection) {
                json.put(annotation.name(), serializeArray((Collection)value, clazz, classObject));
            } else if (value instanceof Map) {
                json.put(annotation.name(), serializeObject((Map)value, clazz, classObject));
            } else if (value instanceof Enum) {
                json.put(annotation.name(), ((Enum)value).name());
            } else if (JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                json.put(annotation.name(), ((JSONTypeSerializationHandler)classObject).serialize(type, value));
            } else {
                json.put(annotation.name(), value);
            }
        } else {
            json.put(annotation.name(), value);
        }

    }

    private static JSONObject serializeObject(Map<?, ?> map, Class<?> clazz, Object classObject) throws Exception {
        JSONObject json = new JSONObject();
        Iterator var4 = map.entrySet().iterator();

        while(true) {
            while(var4.hasNext()) {
                Entry<?, ?> entry = (Entry)var4.next();
                Object value = entry.getValue();
                if (value instanceof boolean[]) {
                    json.put(entry.getKey(), new JSONArray((boolean[])((boolean[])value)));
                } else if (value instanceof byte[]) {
                    json.put(entry.getKey(), new JSONArray((byte[])((byte[])value)));
                } else if (value instanceof short[]) {
                    json.put(entry.getKey(), new JSONArray((short[])((short[])value)));
                } else if (value instanceof int[]) {
                    json.put(entry.getKey(), new JSONArray((int[])((int[])value)));
                } else if (value instanceof long[]) {
                    json.put(entry.getKey(), new JSONArray((long[])((long[])value)));
                } else if (value instanceof float[]) {
                    json.put(entry.getKey(), new JSONArray((float[])((float[])value)));
                } else if (value instanceof double[]) {
                    json.put(entry.getKey(), new JSONArray((double[])((double[])value)));
                } else if (value != null && value.getClass().getAnnotation(JSONRoot.class) != null) {
                    json.put(entry.getKey(), serialize(value));
                } else if (value instanceof Map) {
                    json.put(entry.getKey(), serializeObject((Map)value, clazz, classObject));
                } else if (value instanceof Collection) {
                    json.put(entry.getKey(), serializeArray((Collection)value, clazz, classObject));
                } else if (value != null && value.getClass().isArray()) {
                    json.put(entry.getKey(), serializeArray(value, clazz, classObject));
                } else if (value instanceof Enum) {
                    json.put(entry.getKey(), ((Enum)value).name());
                } else if (!(value instanceof String) && !(value instanceof Boolean) && !(value instanceof Number)) {
                    if (value != null && JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                        json.put(entry.getKey(), ((JSONTypeSerializationHandler)classObject).serialize(value.getClass(), value));
                    } else {
                        json.put(entry.getKey(), value);
                    }
                } else {
                    json.put(entry.getKey(), value);
                }
            }

            return json;
        }
    }

    private static JSONArray serializeArray(Collection<?> collection, Class<?> clazz, Object classObject) throws Exception {
        JSONArray json = new JSONArray();
        Iterator iterator = collection.iterator();

        while(true) {
            while(iterator.hasNext()) {
                Object value = iterator.next();
                if (value instanceof boolean[]) {
                    json.add(new JSONArray((boolean[])((boolean[])value)));
                } else if (value instanceof byte[]) {
                    json.add(new JSONArray((byte[])((byte[])value)));
                } else if (value instanceof short[]) {
                    json.add(new JSONArray((short[])((short[])value)));
                } else if (value instanceof int[]) {
                    json.add(new JSONArray((int[])((int[])value)));
                } else if (value instanceof long[]) {
                    json.add(new JSONArray((long[])((long[])value)));
                } else if (value instanceof float[]) {
                    json.add(new JSONArray((float[])((float[])value)));
                } else if (value instanceof double[]) {
                    json.add(new JSONArray((double[])((double[])value)));
                } else if (value != null && value.getClass().getAnnotation(JSONRoot.class) != null) {
                    json.add(serialize(value));
                } else if (value instanceof Map) {
                    json.add(serializeObject((Map)value, clazz, classObject));
                } else if (value instanceof Collection) {
                    json.add(serializeArray((Collection)value, clazz, classObject));
                } else if (value != null && value.getClass().isArray()) {
                    json.add(serializeArray(value, clazz, classObject));
                } else if (value instanceof Enum) {
                    json.add(((Enum)value).name());
                } else if (!(value instanceof String) && !(value instanceof Boolean) && !(value instanceof Number)) {
                    if (value != null && JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                        ((JSONTypeSerializationHandler)classObject).serialize(value.getClass(), value);
                    } else {
                        json.add(value);
                    }
                } else {
                    json.add(value);
                }
            }

            return json;
        }
    }

    private static JSONArray serializeArray(Object array, Class<?> clazz, Object classObject) throws Exception {
        JSONArray json = new JSONArray();

        for(int index = 0; index < Array.getLength(array); ++index) {
            Object value = Array.get(array, index);
            if (value instanceof boolean[]) {
                json.add(new JSONArray((boolean[])((boolean[])value)));
            } else if (value instanceof byte[]) {
                json.add(new JSONArray((byte[])((byte[])value)));
            } else if (value instanceof short[]) {
                json.add(new JSONArray((short[])((short[])value)));
            } else if (value instanceof int[]) {
                json.add(new JSONArray((int[])((int[])value)));
            } else if (value instanceof long[]) {
                json.add(new JSONArray((long[])((long[])value)));
            } else if (value instanceof float[]) {
                json.add(new JSONArray((float[])((float[])value)));
            } else if (value instanceof double[]) {
                json.add(new JSONArray((double[])((double[])value)));
            } else if (value != null && value.getClass().getAnnotation(JSONRoot.class) != null) {
                json.add(serialize(value));
            } else if (value instanceof Map) {
                json.add(serializeObject((Map)value, clazz, classObject));
            } else if (value instanceof Collection) {
                json.add(serializeArray((Collection)value, clazz, classObject));
            } else if (value != null && value.getClass().isArray()) {
                json.add(serializeArray(value, clazz, classObject));
            } else if (value instanceof Enum) {
                json.add(((Enum)value).name());
            } else if (!(value instanceof String) && !(value instanceof Boolean) && !(value instanceof Number)) {
                if (value != null && JSONTypeSerializationHandler.class.isAssignableFrom(clazz)) {
                    json.add(((JSONTypeSerializationHandler)classObject).serialize(value.getClass(), value));
                } else {
                    json.add(value);
                }
            } else {
                json.add(value);
            }
        }

        return json;
    }

    private static boolean contains(Type type, JSONAttribute attribute) {
        Type[] var2 = attribute.type();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Type value = var2[var4];
            if (value.equals(type)) {
                return true;
            }
        }

        return false;
    }
}
