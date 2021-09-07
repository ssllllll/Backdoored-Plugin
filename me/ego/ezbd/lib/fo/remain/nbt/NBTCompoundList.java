package me.ego.ezbd.lib.fo.remain.nbt;

import me.ego.ezbd.lib.fo.exception.FoException;
import org.apache.commons.lang.NotImplementedException;

public class NBTCompoundList extends NBTList<NBTListCompound> {
    protected NBTCompoundList(NBTCompound owner, String name, NBTType type, Object list) {
        super(owner, name, type, list);
    }

    public NBTListCompound addCompound() {
        return (NBTListCompound)this.addCompound((NBTCompound)null);
    }

    public NBTCompound addCompound(NBTCompound comp) {
        try {
            Object compound = WrapperClass.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            if (WrapperVersion.getVersion().getVersionId() >= WrapperVersion.MC1_14_R1.getVersionId()) {
                WrapperReflection.LIST_ADD.run(this.listObject, new Object[]{this.size(), compound});
            } else {
                WrapperReflection.LEGACY_LIST_ADD.run(this.listObject, new Object[]{compound});
            }

            this.getParent().saveCompound();
            NBTListCompound listcomp = new NBTListCompound(this, compound);
            if (comp != null) {
                listcomp.mergeCompound(comp);
            }

            return listcomp;
        } catch (Exception var4) {
            throw new FoException(var4);
        }
    }

    /** @deprecated */
    @Deprecated
    public boolean add(NBTListCompound empty) {
        return this.addCompound(empty) != null;
    }

    public void add(int index, NBTListCompound element) {
        if (element != null) {
            throw new NotImplementedException("You need to pass null! ListCompounds from other lists won't work.");
        } else {
            try {
                Object compound = WrapperClass.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
                if (WrapperVersion.getVersion().getVersionId() >= WrapperVersion.MC1_14_R1.getVersionId()) {
                    WrapperReflection.LIST_ADD.run(this.listObject, new Object[]{index, compound});
                } else {
                    WrapperReflection.LEGACY_LIST_ADD.run(this.listObject, new Object[]{compound});
                }

                super.getParent().saveCompound();
            } catch (Exception var4) {
                throw new FoException(var4);
            }
        }
    }

    public NBTListCompound get(int index) {
        try {
            Object compound = WrapperReflection.LIST_GET_COMPOUND.run(this.listObject, new Object[]{index});
            return new NBTListCompound(this, compound);
        } catch (Exception var3) {
            throw new FoException(var3);
        }
    }

    public NBTListCompound set(int index, NBTListCompound element) {
        throw new NotImplementedException("This method doesn't work in the ListCompound context.");
    }

    protected Object asTag(NBTListCompound object) {
        return null;
    }
}