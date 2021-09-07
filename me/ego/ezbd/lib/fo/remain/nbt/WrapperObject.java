package me.ego.ezbd.lib.fo.remain.nbt;

import java.lang.reflect.Constructor;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.exception.FoException;

enum WrapperObject {
    NMS_NBTTAGCOMPOUND((WrapperVersion)null, (WrapperVersion)null, WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[0]),
    NMS_BLOCKPOSITION((WrapperVersion)null, (WrapperVersion)null, WrapperClass.NMS_BLOCKPOSITION.getClazz(), new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE}),
    NMS_COMPOUNDFROMITEM(WrapperVersion.MC1_11_R1, (WrapperVersion)null, WrapperClass.NMS_ITEMSTACK.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()});

    private Constructor<?> construct;
    private Class<?> targetClass;

    private WrapperObject(WrapperVersion from, WrapperVersion to, Class<?> clazz, Class<?>... args) {
        if (clazz != null) {
            if (from == null || WrapperVersion.getVersion().getVersionId() >= from.getVersionId()) {
                if (to == null || WrapperVersion.getVersion().getVersionId() <= to.getVersionId()) {
                    try {
                        this.targetClass = clazz;
                        this.construct = clazz.getDeclaredConstructor(args);
                        this.construct.setAccessible(true);
                    } catch (Exception var8) {
                        Common.error(var8, new String[]{"Unable to find the constructor for the class '" + clazz.getName() + "'"});
                    }

                }
            }
        }
    }

    public Object getInstance(Object... args) {
        try {
            return this.construct.newInstance(args);
        } catch (Exception var3) {
            throw new FoException(var3, "Exception while creating a new instance of '" + this.targetClass + "'");
        }
    }
}