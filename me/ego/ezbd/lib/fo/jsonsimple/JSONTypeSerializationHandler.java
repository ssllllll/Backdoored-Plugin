package me.ego.ezbd.lib.fo.jsonsimple;

public interface JSONTypeSerializationHandler {
    Object serialize(Class<?> var1, Object var2);

    Object deserialize(Class<?> var1, Object var2);
}
