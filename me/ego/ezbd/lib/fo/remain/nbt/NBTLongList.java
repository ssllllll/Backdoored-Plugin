package me.ego.ezbd.lib.fo.remain.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import me.ego.ezbd.lib.fo.exception.FoException;

public class NBTLongList extends NBTList<Long> {
    protected NBTLongList(NBTCompound owner, String name, NBTType type, Object list) {
        super(owner, name, type, list);
    }

    protected Object asTag(Long object) {
        try {
            Constructor<?> con = WrapperClass.NMS_NBTTAGLONG.getClazz().getDeclaredConstructor(Long.TYPE);
            con.setAccessible(true);
            return con.newInstance(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var3) {
            throw new FoException(var3, "Error while wrapping the Object " + object + " to it's NMS object!");
        }
    }

    public Long get(int index) {
        try {
            Object obj = WrapperReflection.LIST_GET.run(this.listObject, new Object[]{index});
            return Long.parseLong(obj.toString().replace("L", ""));
        } catch (NumberFormatException var3) {
            return 0L;
        } catch (Exception var4) {
            throw new FoException(var4);
        }
    }
}