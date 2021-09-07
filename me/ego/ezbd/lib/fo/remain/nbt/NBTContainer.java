package me.ego.ezbd.lib.fo.remain.nbt;

import java.io.InputStream;
import me.ego.ezbd.lib.fo.exception.FoException;

public class NBTContainer extends NBTCompound {
    private Object nbt;

    public NBTContainer() {
        super((NBTCompound)null, (String)null);
        this.nbt = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
    }

    public NBTContainer(Object nbt) {
        super((NBTCompound)null, (String)null);
        if (nbt == null) {
            throw new NullPointerException("The NBT-Object can't be null!");
        } else if (!WrapperClass.NMS_NBTTAGCOMPOUND.getClazz().isAssignableFrom(nbt.getClass())) {
            throw new FoException("The object '" + nbt.getClass() + "' is not a valid NBT-Object!");
        } else {
            this.nbt = nbt;
        }
    }

    public NBTContainer(InputStream inputsteam) {
        super((NBTCompound)null, (String)null);
        this.nbt = NBTReflectionUtil.readNBT(inputsteam);
    }

    public NBTContainer(String nbtString) {
        super((NBTCompound)null, (String)null);
        if (nbtString == null) {
            throw new NullPointerException("The String can't be null!");
        } else {
            try {
                this.nbt = WrapperReflection.PARSE_NBT.run((Object)null, new Object[]{nbtString});
            } catch (Exception var3) {
                throw new FoException(var3, "Unable to parse a malformed json!");
            }
        }
    }

    public Object getCompound() {
        return this.nbt;
    }

    public void setCompound(Object tag) {
        this.nbt = tag;
    }
}