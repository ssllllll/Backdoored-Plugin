package me.ego.ezbd.lib.fo.remain.nbt;

import org.bukkit.block.BlockState;

public class NBTTileEntity extends NBTCompound {
    private final BlockState tile;

    public NBTTileEntity(BlockState tile) {
        super((NBTCompound)null, (String)null);
        if (tile != null && (!WrapperVersion.isAtLeastVersion(WrapperVersion.MC1_8_R3) || tile.isPlaced())) {
            this.tile = tile;
        } else {
            throw new NullPointerException("Tile can't be null/not placed!");
        }
    }

    public Object getCompound() {
        return NBTReflectionUtil.getTileEntityNBTTagCompound(this.tile);
    }

    protected void setCompound(Object compound) {
        NBTReflectionUtil.setTileEntityNBTTagCompound(this.tile, compound);
    }
}