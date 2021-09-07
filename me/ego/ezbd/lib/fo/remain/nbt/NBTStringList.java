package me.ego.ezbd.lib.fo.remain.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import me.ego.ezbd.lib.fo.exception.FoException;

public class NBTStringList extends NBTList<String> {
    protected NBTStringList(NBTCompound owner, String name, NBTType type, Object list) {
        super(owner, name, type, list);
    }

    public String get(int index) {
        try {
            return (String)WrapperReflection.LIST_GET_STRING.run(this.listObject, new Object[]{index});
        } catch (Exception var3) {
            throw new FoException(var3);
        }
    }

    protected Object asTag(String object) {
        try {
            Constructor<?> con = WrapperClass.NMS_NBTTAGSTRING.getClazz().getDeclaredConstructor(String.class);
            con.setAccessible(true);
            return con.newInstance(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var3) {
            throw new FoException(var3, "Error while wrapping the Object " + object + " to it's NMS object!");
        }
    }
}