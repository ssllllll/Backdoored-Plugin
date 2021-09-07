package me.ego.ezbd.lib.fo.remain.nbt;

import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** @deprecated */
@Deprecated
public class NBTInternals {
    private static final String STRING_KEY = "stringTest";
    private static final String INT_KEY = "intTest";
    private static final String DOUBLE_KEY = "doubleTest";
    private static final String BOOLEAN_KEY = "booleanTest";
    private static final String COMPONENT_KEY = "componentTest";
    private static final String SHORT_KEY = "shortTest";
    private static final String BYTE_KEY = "byteTest";
    private static final String FLOAT_KEY = "floatTest";
    private static final String LONG_KEY = "longTest";
    private static final String INTARRAY_KEY = "intarrayTest";
    private static final String BYTEARRAY_KEY = "bytearrayTest";
    private static final String STRING_VALUE = "TestString";
    private static final int INT_VALUE = 42;
    private static final double DOUBLE_VALUE = 1.5D;
    private static final boolean BOOLEAN_VALUE = true;
    private static final short SHORT_VALUE = 64;
    private static final byte BYTE_VALUE = 7;
    private static final float FLOAT_VALUE = 13.37F;
    private static final long LONG_VALUE = 2147483689L;
    private static final int[] INTARRAY_VALUE = new int[]{1337, 42, 69};
    private static final byte[] BYTEARRAY_VALUE = new byte[]{8, 7, 3, 2};

    public NBTInternals() {
    }

    public static boolean checkCompatible() {
        boolean compatible = true;
        boolean jsonCompatible = true;
        ItemStack item = new ItemStack(Material.STONE, 1);
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("stringTest", "TestString");
        nbtItem.setInteger("intTest", 42);
        nbtItem.setDouble("doubleTest", 1.5D);
        nbtItem.setBoolean("booleanTest", true);
        nbtItem.setByte("byteTest", (byte)7);
        nbtItem.setShort("shortTest", Short.valueOf((short)64));
        nbtItem.setLong("longTest", 2147483689L);
        nbtItem.setFloat("floatTest", 13.37F);
        nbtItem.setIntArray("intarrayTest", INTARRAY_VALUE);
        nbtItem.setByteArray("bytearrayTest", BYTEARRAY_VALUE);
        nbtItem.addCompound("componentTest");
        NBTCompound comp = nbtItem.getCompound("componentTest");
        comp.setString("stringTest", "TestString2");
        comp.setInteger("intTest", 84);
        comp.setDouble("doubleTest", 3.0D);
        NBTList list = comp.getStringList("testlist");
        list.add("test1");
        list.add("test2");
        list.add("test3");
        list.add("test4");
        list.set(2, "test42");
        list.remove(1);
        NBTCompoundList taglist = comp.getCompoundList("complist");
        NBTListCompound lcomp = taglist.addCompound();
        lcomp.setDouble("double1", 0.3333D);
        lcomp.setInteger("int1", 42);
        lcomp.setString("test1", "test1");
        lcomp.setString("test2", "test2");
        lcomp.removeKey("test1");
        item = nbtItem.getItem();
        nbtItem = null;
        comp = null;
        list = null;
        nbtItem = new NBTItem(item);
        if (!nbtItem.hasKey("stringTest")) {
            Common.log(new String[]{"NBTAPI was not able to check a key!"});
            compatible = false;
        }

        if (!"TestString".equals(nbtItem.getString("stringTest")) || nbtItem.getInteger("intTest") != 42 || nbtItem.getDouble("doubleTest") != 1.5D || nbtItem.getByte("byteTest") != 7 || nbtItem.getShort("shortTest") != 64 || nbtItem.getFloat("floatTest") != 13.37F || nbtItem.getLong("longTest") != 2147483689L || nbtItem.getIntArray("intarrayTest").length != INTARRAY_VALUE.length || nbtItem.getByteArray("bytearrayTest").length != BYTEARRAY_VALUE.length || !nbtItem.getBoolean("booleanTest").equals(true)) {
            Common.log(new String[]{"One key does not equal the original value!"});
            compatible = false;
        }

        nbtItem.setString("stringTest", (String)null);
        if (nbtItem.getKeys().size() != 10) {
            Common.log(new String[]{"Wasn't able to remove a key (Got " + nbtItem.getKeys().size() + " when expecting 10)!"});
            compatible = false;
        }

        comp = nbtItem.getCompound("componentTest");
        if (comp == null) {
            Common.log(new String[]{"Wasn't able to get the NBTCompound!!"});
            compatible = false;
        }

        if (!comp.hasKey("stringTest")) {
            Common.log(new String[]{"Wasn't able to check a compound key!"});
            compatible = false;
        }

        if (!"TestString2".equals(comp.getString("stringTest")) || comp.getInteger("intTest") != 84 || comp.getDouble("doubleTest") != 3.0D || comp.getBoolean("booleanTest")) {
            Common.log(new String[]{"One key does not equal the original compound value!"});
            compatible = false;
        }

        list = comp.getStringList("testlist");
        if (comp.getType("testlist") != NBTType.NBTTagList) {
            Common.log(new String[]{"Wasn't able to get the correct Tag type!"});
            compatible = false;
        }

        if (!list.get(1).equals("test42") || list.size() != 3) {
            Common.log(new String[]{"The List support got an error, and may not work!"});
        }

        taglist = comp.getCompoundList("complist");
        if (taglist.size() == 1) {
            lcomp = taglist.get(0);
            if (lcomp.getKeys().size() != 3) {
                Common.log(new String[]{"Wrong key amount in Taglist (" + lcomp.getKeys().size() + ")!"});
                compatible = false;
            } else if (lcomp.getDouble("double1") != 0.3333D || lcomp.getInteger("int1") != 42 || !lcomp.getString("test2").equals("test2") || lcomp.hasKey("test1")) {
                Common.log(new String[]{"One key in the Taglist changed!"});
                compatible = false;
            }
        } else {
            Common.log(new String[]{"Taglist is empty!"});
            compatible = false;
        }

        if (!compatible && MinecraftVersion.newerThan(V.v1_7)) {
            Common.log(new String[]{"WARNING"});
            Common.log(new String[]{"The NBT library seems to be broken with your"});
            Common.log(new String[]{"Spigot version " + MinecraftVersion.getServerVersion()});
            Common.log(new String[0]);
            Common.log(new String[]{"Please contact the developer of this library."});
        }

        return compatible;
    }
}