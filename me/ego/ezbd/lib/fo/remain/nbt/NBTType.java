package me.ego.ezbd.lib.fo.remain.nbt;

public enum NBTType {
    NBTTagEnd(0),
    NBTTagByte(1),
    NBTTagShort(2),
    NBTTagInt(3),
    NBTTagLong(4),
    NBTTagFloat(5),
    NBTTagDouble(6),
    NBTTagByteArray(7),
    NBTTagIntArray(11),
    NBTTagString(8),
    NBTTagList(9),
    NBTTagCompound(10);

    private final int id;

    private NBTType(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public static NBTType valueOf(int id) {
        NBTType[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            NBTType t = var1[var3];
            if (t.getId() == id) {
                return t;
            }
        }

        return NBTTagEnd;
    }
}