package me.ego.ezbd.lib.fo.remain.nbt;

import org.bukkit.entity.Entity;

public class NBTEntity extends NBTCompound {
    private final Entity ent;

    public NBTEntity(Entity entity) {
        super((NBTCompound)null, (String)null);
        if (entity == null) {
            throw new NullPointerException("Entity can't be null!");
        } else {
            this.ent = entity;
        }
    }

    public Object getCompound() {
        return NBTReflectionUtil.getEntityNBTTagCompound(NBTReflectionUtil.getNMSEntity(this.ent));
    }

    protected void setCompound(Object compound) {
        NBTReflectionUtil.setEntityNBTTag(compound, NBTReflectionUtil.getNMSEntity(this.ent));
    }
}