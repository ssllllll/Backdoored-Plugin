package me.ego.ezbd.lib.fo.remain.nbt;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.inventory.ItemStack;

enum WrapperReflection {
    COMPOUND_SET_FLOAT(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Float.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setFloat")}),
    COMPOUND_SET_STRING(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setString")}),
    COMPOUND_SET_INT(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Integer.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setInt")}),
    COMPOUND_SET_BYTEARRAY(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, byte[].class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setByteArray")}),
    COMPOUND_SET_INTARRAY(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, int[].class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setIntArray")}),
    COMPOUND_SET_LONG(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Long.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setLong")}),
    COMPOUND_SET_SHORT(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Short.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setShort")}),
    COMPOUND_SET_BYTE(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Byte.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setByte")}),
    COMPOUND_SET_DOUBLE(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Double.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setDouble")}),
    COMPOUND_SET_BOOLEAN(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Boolean.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setBoolean")}),
    COMPOUND_SET_UUID(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, UUID.class}, WrapperVersion.MC1_16_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_16_R1, "a")}),
    COMPOUND_MERGE(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "a")}),
    COMPOUND_SET(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, WrapperClass.NMS_NBTBASE.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "set")}),
    COMPOUND_GET(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "get")}),
    COMPOUND_GET_LIST(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class, Integer.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getList")}),
    COMPOUND_OWN_TYPE(WrapperClass.NMS_NBTBASE.getClazz(), new Class[0], WrapperVersion.MC1_7_R4, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getTypeId")}),
    COMPOUND_GET_FLOAT(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getFloat")}),
    COMPOUND_GET_STRING(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getString")}),
    COMPOUND_GET_INT(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getInt")}),
    COMPOUND_GET_BYTEARRAY(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getByteArray")}),
    COMPOUND_GET_INTARRAY(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getIntArray")}),
    COMPOUND_GET_LONG(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getLong")}),
    COMPOUND_GET_SHORT(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getShort")}),
    COMPOUND_GET_BYTE(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getByte")}),
    COMPOUND_GET_DOUBLE(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getDouble")}),
    COMPOUND_GET_BOOLEAN(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getBoolean")}),
    COMPOUND_GET_UUID(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_16_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_16_R1, "a")}),
    COMPOUND_GET_COMPOUND(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getCompound")}),
    NMSITEM_GETTAG(WrapperClass.NMS_ITEMSTACK.getClazz(), new Class[0], WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getTag")}),
    NMSITEM_SAVE(WrapperClass.NMS_ITEMSTACK.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "save")}),
    NMSITEM_CREATESTACK(WrapperClass.NMS_ITEMSTACK.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, WrapperVersion.MC1_10_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "createStack")}),
    COMPOUND_REMOVE_KEY(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "remove")}),
    COMPOUND_HAS_KEY(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "hasKey")}),
    COMPOUND_GET_TYPE(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "b"), new WrapperReflection.Since(WrapperVersion.MC1_9_R1, "d"), new WrapperReflection.Since(WrapperVersion.MC1_15_R1, "e"), new WrapperReflection.Since(WrapperVersion.MC1_16_R1, "d")}),
    COMPOUND_GET_KEYS(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[0], WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "c"), new WrapperReflection.Since(WrapperVersion.MC1_13_R1, "getKeys")}),
    LISTCOMPOUND_GET_KEYS(WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), new Class[0], WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "c"), new WrapperReflection.Since(WrapperVersion.MC1_13_R1, "getKeys")}),
    LIST_REMOVE_KEY(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[]{Integer.TYPE}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "a"), new WrapperReflection.Since(WrapperVersion.MC1_9_R1, "remove")}),
    LIST_SIZE(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[0], WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "size")}),
    LIST_SET(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[]{Integer.TYPE, WrapperClass.NMS_NBTBASE.getClazz()}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "a"), new WrapperReflection.Since(WrapperVersion.MC1_13_R1, "set")}),
    LEGACY_LIST_ADD(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[]{WrapperClass.NMS_NBTBASE.getClazz()}, WrapperVersion.MC1_7_R4, WrapperVersion.MC1_13_R2, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "add")}),
    LIST_ADD(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[]{Integer.TYPE, WrapperClass.NMS_NBTBASE.getClazz()}, WrapperVersion.MC1_14_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_14_R1, "add")}),
    LIST_GET_STRING(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[]{Integer.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getString")}),
    LIST_GET_COMPOUND(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[]{Integer.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "get")}),
    LIST_GET(WrapperClass.NMS_NBTTAGLIST.getClazz(), new Class[]{Integer.TYPE}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "get"), new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "g"), new WrapperReflection.Since(WrapperVersion.MC1_9_R1, "h"), new WrapperReflection.Since(WrapperVersion.MC1_12_R1, "i"), new WrapperReflection.Since(WrapperVersion.MC1_13_R1, "get")}),
    ITEMSTACK_SET_TAG(WrapperClass.NMS_ITEMSTACK.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "setTag")}),
    ITEMSTACK_NMSCOPY(WrapperClass.CRAFT_ITEMSTACK.getClazz(), new Class[]{ItemStack.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "asNMSCopy")}),
    ITEMSTACK_BUKKITMIRROR(WrapperClass.CRAFT_ITEMSTACK.getClazz(), new Class[]{WrapperClass.NMS_ITEMSTACK.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "asCraftMirror")}),
    CRAFT_WORLD_GET_HANDLE(WrapperClass.CRAFT_WORLD.getClazz(), new Class[0], WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getHandle")}),
    NMS_WORLD_GET_TILEENTITY(WrapperClass.NMS_WORLDSERVER.getClazz(), new Class[]{WrapperClass.NMS_BLOCKPOSITION.getClazz()}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "getTileEntity")}),
    NMS_WORLD_SET_TILEENTITY(WrapperClass.NMS_WORLDSERVER.getClazz(), new Class[]{WrapperClass.NMS_BLOCKPOSITION.getClazz(), WrapperClass.NMS_TILEENTITY.getClazz()}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "setTileEntity")}),
    NMS_WORLD_REMOVE_TILEENTITY(WrapperClass.NMS_WORLDSERVER.getClazz(), new Class[]{WrapperClass.NMS_BLOCKPOSITION.getClazz()}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "t"), new WrapperReflection.Since(WrapperVersion.MC1_9_R1, "s"), new WrapperReflection.Since(WrapperVersion.MC1_13_R1, "n"), new WrapperReflection.Since(WrapperVersion.MC1_14_R1, "removeTileEntity")}),
    NMS_WORLD_GET_TILEENTITY_1_7_10(WrapperClass.NMS_WORLDSERVER.getClazz(), new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE}, WrapperVersion.MC1_7_R4, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getTileEntity")}),
    TILEENTITY_LOAD_LEGACY191(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_MINECRAFTSERVER.getClazz(), WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_9_R1, WrapperVersion.MC1_9_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_9_R1, "a")}),
    TILEENTITY_LOAD_LEGACY183(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_8_R3, WrapperVersion.MC1_9_R2, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "c"), new WrapperReflection.Since(WrapperVersion.MC1_9_R1, "a"), new WrapperReflection.Since(WrapperVersion.MC1_9_R2, "c")}),
    TILEENTITY_LOAD_LEGACY1121(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_WORLD.getClazz(), WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_10_R1, WrapperVersion.MC1_12_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_10_R1, "a"), new WrapperReflection.Since(WrapperVersion.MC1_12_R1, "create")}),
    TILEENTITY_LOAD_LEGACY1151(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_13_R1, WrapperVersion.MC1_15_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_12_R1, "create")}),
    TILEENTITY_LOAD(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_IBLOCKDATA.getClazz(), WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_16_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_16_R1, "create")}),
    TILEENTITY_GET_NBT(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "b"), new WrapperReflection.Since(WrapperVersion.MC1_9_R1, "save")}),
    TILEENTITY_SET_NBT_LEGACY1151(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, WrapperVersion.MC1_15_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "a"), new WrapperReflection.Since(WrapperVersion.MC1_12_R1, "load")}),
    TILEENTITY_SET_NBT(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[]{WrapperClass.NMS_IBLOCKDATA.getClazz(), WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_16_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_16_R1, "load")}),
    TILEENTITY_GET_BLOCKDATA(WrapperClass.NMS_TILEENTITY.getClazz(), new Class[0], WrapperVersion.MC1_16_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_16_R1, "getBlock")}),
    CRAFT_ENTITY_GET_HANDLE(WrapperClass.CRAFT_ENTITY.getClazz(), new Class[0], WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "getHandle")}),
    NMS_ENTITY_SET_NBT(WrapperClass.NMS_ENTITY.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "f"), new WrapperReflection.Since(WrapperVersion.MC1_16_R1, "load")}),
    NMS_ENTITY_GET_NBT(WrapperClass.NMS_ENTITY.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "e"), new WrapperReflection.Since(WrapperVersion.MC1_12_R1, "save")}),
    NMS_ENTITY_GETSAVEID(WrapperClass.NMS_ENTITY.getClazz(), new Class[0], WrapperVersion.MC1_14_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_14_R1, "getSaveID")}),
    NBTFILE_READ(WrapperClass.NMS_NBTCOMPRESSEDSTREAMTOOLS.getClazz(), new Class[]{InputStream.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "a")}),
    NBTFILE_WRITE(WrapperClass.NMS_NBTCOMPRESSEDSTREAMTOOLS.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), OutputStream.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "a")}),
    PARSE_NBT(WrapperClass.NMS_MOJANGSONPARSER.getClazz(), new Class[]{String.class}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "parse")}),
    REGISTRY_KEYSET(WrapperClass.NMS_REGISTRYSIMPLE.getClazz(), new Class[0], WrapperVersion.MC1_11_R1, WrapperVersion.MC1_13_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_11_R1, "keySet")}),
    REGISTRY_GET(WrapperClass.NMS_REGISTRYSIMPLE.getClazz(), new Class[]{Object.class}, WrapperVersion.MC1_11_R1, WrapperVersion.MC1_13_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_11_R1, "get")}),
    REGISTRY_SET(WrapperClass.NMS_REGISTRYSIMPLE.getClazz(), new Class[]{Object.class, Object.class}, WrapperVersion.MC1_11_R1, WrapperVersion.MC1_13_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_11_R1, "a")}),
    REGISTRY_GET_INVERSE(WrapperClass.NMS_REGISTRYMATERIALS.getClazz(), new Class[]{Object.class}, WrapperVersion.MC1_11_R1, WrapperVersion.MC1_13_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_11_R1, "b")}),
    REGISTRYMATERIALS_KEYSET(WrapperClass.NMS_REGISTRYMATERIALS.getClazz(), new Class[0], WrapperVersion.MC1_13_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_13_R1, "keySet")}),
    REGISTRYMATERIALS_GET(WrapperClass.NMS_REGISTRYMATERIALS.getClazz(), new Class[]{WrapperClass.NMS_MINECRAFTKEY.getClazz()}, WrapperVersion.MC1_13_R1, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_13_R1, "get")}),
    REGISTRYMATERIALS_GETKEY(WrapperClass.NMS_REGISTRYMATERIALS.getClazz(), new Class[]{Object.class}, WrapperVersion.MC1_13_R2, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_13_R2, "getKey")}),
    GAMEPROFILE_DESERIALIZE(WrapperClass.NMS_GAMEPROFILESERIALIZER.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz()}, WrapperVersion.MC1_7_R4, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_7_R4, "deserialize")}),
    GAMEPROFILE_SERIALIZE(WrapperClass.NMS_GAMEPROFILESERIALIZER.getClazz(), new Class[]{WrapperClass.NMS_NBTTAGCOMPOUND.getClazz(), WrapperClass.GAMEPROFILE.getClazz()}, WrapperVersion.MC1_8_R3, new WrapperReflection.Since[]{new WrapperReflection.Since(WrapperVersion.MC1_8_R3, "serialize")});

    private WrapperVersion removedAfter;
    private WrapperReflection.Since targetVersion;
    private Method method;
    private boolean loaded;
    private boolean compatible;
    private String methodName;

    private WrapperReflection(Class<?> targetClass, Class<?>[] args, WrapperVersion addedSince, WrapperVersion removedAfter, WrapperReflection.Since... methodnames) {
        this.loaded = false;
        this.compatible = false;
        this.methodName = null;
        this.removedAfter = removedAfter;
        if (WrapperVersion.isAtLeastVersion(addedSince) && (this.removedAfter == null || !WrapperVersion.isNewerThan(removedAfter))) {
            this.compatible = true;
            WrapperVersion server = WrapperVersion.getVersion();
            WrapperReflection.Since target = methodnames[0];
            WrapperReflection.Since[] var10 = methodnames;
            int var11 = methodnames.length;

            for(int var12 = 0; var12 < var11; ++var12) {
                WrapperReflection.Since s = var10[var12];
                if (s.version.getVersionId() <= server.getVersionId() && target.version.getVersionId() < s.version.getVersionId()) {
                    target = s;
                }
            }

            this.targetVersion = target;

            try {
                this.method = targetClass.getMethod(this.targetVersion.name, args);
                this.method.setAccessible(true);
                this.loaded = true;
                this.methodName = this.targetVersion.name;
            } catch (NoSuchMethodException | SecurityException | NullPointerException var14) {
                Common.log(new String[]{"[NBTAPI] Unable to find the method '" + this.targetVersion.name + "' in '" + (targetClass == null ? "null" : targetClass.getSimpleName()) + "' Enum: " + this});
            }

        }
    }

    private WrapperReflection(Class<?> targetClass, Class<?>[] args, WrapperVersion addedSince, WrapperReflection.Since... methodnames) {
        this(targetClass, args, addedSince, (WrapperVersion)null, methodnames);
    }

    public Object run(Object target, Object... args) {
        if (this.method == null) {
            throw new FoException("Method not loaded! '" + this + "'");
        } else {
            try {
                return this.method.invoke(target, args);
            } catch (Exception var4) {
                throw new FoException(var4, "Error while calling the method '" + this.methodName + "', loaded: " + this.loaded + ", Enum: " + this);
            }
        }
    }

    public String getMethodName() {
        return this.methodName;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean isCompatible() {
        return this.compatible;
    }

    protected static class Since {
        public final WrapperVersion version;
        public final String name;

        public Since(WrapperVersion version, String name) {
            this.version = version;
            this.name = name;
        }
    }
}