package me.ego.ezbd.lib.fo.remain.nbt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import me.ego.ezbd.lib.fo.exception.FoException;

public class NBTDoubleList extends NBTList<Double> {
    protected NBTDoubleList(NBTCompound owner, String name, NBTType type, Object list) {
        super(owner, name, type, list);
    }

    protected Object asTag(Double object) {
        try {
            Constructor<?> con = WrapperClass.NMS_NBTTAGDOUBLE.getClazz().getDeclaredConstructor(Double.TYPE);
            con.setAccessible(true);
            return con.newInstance(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var3) {
            throw new FoException(var3, "Error while wrapping the Object " + object + " to it's NMS object!");
        }
    }

    public Double get(int index) {
        try {
            Object obj = WrapperReflection.LIST_GET.run(this.listObject, new Object[]{index});
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException var3) {
            return 0.0D;
        } catch (Exception var4) {
            throw new FoException(var4);
        }
    }
}
