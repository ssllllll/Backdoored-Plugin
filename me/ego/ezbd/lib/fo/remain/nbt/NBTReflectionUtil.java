package me.ego.ezbd.lib.fo.remain.nbt;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;

public class NBTReflectionUtil {
    private static final Gson gson = new Gson();
    private static Field field_unhandledTags = null;

    public static Object getNMSEntity(Entity entity) {
        try {
            return WrapperReflection.CRAFT_ENTITY_GET_HANDLE.run(WrapperClass.CRAFT_ENTITY.getClazz().cast(entity), new Object[0]);
        } catch (Exception var2) {
            throw new FoException(var2, "Exception while getting the NMS Entity from a Bukkit Entity!");
        }
    }

    public static Object readNBT(InputStream stream) {
        try {
            return WrapperReflection.NBTFILE_READ.run((Object)null, new Object[]{stream});
        } catch (Exception var2) {
            throw new FoException(var2, "Exception while reading a NBT File!");
        }
    }

    public static Object writeNBT(Object nbt, OutputStream stream) {
        try {
            return WrapperReflection.NBTFILE_WRITE.run((Object)null, new Object[]{nbt, stream});
        } catch (Exception var3) {
            throw new FoException(var3, "Exception while writing NBT!");
        }
    }

    public static void writeApiNBT(NBTCompound comp, OutputStream stream) {
        try {
            Object nbttag = comp.getCompound();
            if (nbttag == null) {
                nbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
            }

            if (valideCompound(comp)) {
                Object workingtag = gettoCompount(nbttag, comp);
                WrapperReflection.NBTFILE_WRITE.run((Object)null, new Object[]{workingtag, stream});
            }
        } catch (Exception var4) {
            throw new FoException(var4, "Exception while writing NBT!");
        }
    }

    public static Object getItemRootNBTTagCompound(Object nmsitem) {
        try {
            Object answer = WrapperReflection.NMSITEM_GETTAG.run(nmsitem, new Object[0]);
            return answer != null ? answer : WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        } catch (Exception var2) {
            throw new FoException(var2, "Exception while getting an Itemstack's NBTCompound!");
        }
    }

    public static Object convertNBTCompoundtoNMSItem(NBTCompound nbtcompound) {
        try {
            Object nmsComp = gettoCompount(nbtcompound.getCompound(), nbtcompound);
            return WrapperVersion.getVersion().getVersionId() >= WrapperVersion.MC1_11_R1.getVersionId() ? WrapperObject.NMS_COMPOUNDFROMITEM.getInstance(new Object[]{nmsComp}) : WrapperReflection.NMSITEM_CREATESTACK.run((Object)null, new Object[]{nmsComp});
        } catch (Exception var2) {
            throw new FoException(var2, "Exception while converting NBTCompound to NMS ItemStack!");
        }
    }

    public static NBTContainer convertNMSItemtoNBTCompound(Object nmsitem) {
        try {
            Object answer = WrapperReflection.NMSITEM_SAVE.run(nmsitem, new Object[]{WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0])});
            return new NBTContainer(answer);
        } catch (Exception var2) {
            throw new FoException(var2, "Exception while converting NMS ItemStack to NBTCompound!");
        }
    }

    public static Map<String, Object> getUnhandledNBTTags(ItemMeta meta) {
        try {
            return (Map)field_unhandledTags.get(meta);
        } catch (Exception var2) {
            throw new FoException(var2, "Exception while getting unhandled tags from ItemMeta!");
        }
    }

    public static Object getEntityNBTTagCompound(Object nmsEntity) {
        try {
            Object nbt = WrapperClass.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            Object answer = WrapperReflection.NMS_ENTITY_GET_NBT.run(nmsEntity, new Object[]{nbt});
            if (answer == null) {
                answer = nbt;
            }

            return answer;
        } catch (Exception var3) {
            throw new FoException(var3, "Exception while getting NBTCompound from NMS Entity!");
        }
    }

    public static Object setEntityNBTTag(Object nbtTag, Object nmsEntity) {
        try {
            WrapperReflection.NMS_ENTITY_SET_NBT.run(nmsEntity, new Object[]{nbtTag});
            return nmsEntity;
        } catch (Exception var3) {
            throw new FoException("Exception while setting the NBTCompound of an Entity");
        }
    }

    public static Object getTileEntityNBTTagCompound(BlockState tile) {
        try {
            Object cworld = WrapperClass.CRAFT_WORLD.getClazz().cast(tile.getWorld());
            Object nmsworld = WrapperReflection.CRAFT_WORLD_GET_HANDLE.run(cworld, new Object[0]);
            Object o = null;
            Object tag;
            if (WrapperVersion.getVersion() == WrapperVersion.MC1_7_R4) {
                o = WrapperReflection.NMS_WORLD_GET_TILEENTITY_1_7_10.run(nmsworld, new Object[]{tile.getX(), tile.getY(), tile.getZ()});
            } else {
                tag = WrapperObject.NMS_BLOCKPOSITION.getInstance(new Object[]{tile.getX(), tile.getY(), tile.getZ()});
                o = WrapperReflection.NMS_WORLD_GET_TILEENTITY.run(nmsworld, new Object[]{tag});
            }

            tag = WrapperClass.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            Object answer = WrapperReflection.TILEENTITY_GET_NBT.run(o, new Object[]{tag});
            if (answer == null) {
                answer = tag;
            }

            return answer;
        } catch (Exception var6) {
            throw new FoException(var6, "Exception while getting NBTCompound from TileEntity!");
        }
    }

    public static void setTileEntityNBTTagCompound(BlockState tile, Object comp) {
        try {
            Object cworld = WrapperClass.CRAFT_WORLD.getClazz().cast(tile.getWorld());
            Object nmsworld = WrapperReflection.CRAFT_WORLD_GET_HANDLE.run(cworld, new Object[0]);
            Object o = null;
            Object blockData;
            if (WrapperVersion.getVersion() == WrapperVersion.MC1_7_R4) {
                o = WrapperReflection.NMS_WORLD_GET_TILEENTITY_1_7_10.run(nmsworld, new Object[]{tile.getX(), tile.getY(), tile.getZ()});
            } else {
                blockData = WrapperObject.NMS_BLOCKPOSITION.getInstance(new Object[]{tile.getX(), tile.getY(), tile.getZ()});
                o = WrapperReflection.NMS_WORLD_GET_TILEENTITY.run(nmsworld, new Object[]{blockData});
            }

            if (WrapperVersion.getVersion().getVersionId() >= WrapperVersion.MC1_16_R1.getVersionId()) {
                blockData = WrapperReflection.TILEENTITY_GET_BLOCKDATA.run(o, new Object[0]);
                WrapperReflection.TILEENTITY_SET_NBT.run(o, new Object[]{blockData, comp});
            } else {
                WrapperReflection.TILEENTITY_SET_NBT_LEGACY1151.run(o, new Object[]{comp});
            }

        } catch (Exception var6) {
            throw new FoException(var6, "Exception while setting NBTData for a TileEntity!");
        }
    }

    public static Object getSubNBTTagCompound(Object compound, String name) {
        try {
            if ((Boolean)WrapperReflection.COMPOUND_HAS_KEY.run(compound, new Object[]{name})) {
                return WrapperReflection.COMPOUND_GET_COMPOUND.run(compound, new Object[]{name});
            } else {
                throw new FoException("Tried getting invalide compound '" + name + "' from '" + compound + "'!");
            }
        } catch (Exception var3) {
            throw new FoException(var3, "Exception while getting NBT subcompounds!");
        }
    }

    public static void addNBTTagCompound(NBTCompound comp, String name) {
        if (name == null) {
            remove(comp, name);
        } else {
            Object nbttag = comp.getCompound();
            if (nbttag == null) {
                nbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
            }

            if (valideCompound(comp)) {
                Object workingtag = gettoCompount(nbttag, comp);

                try {
                    WrapperReflection.COMPOUND_SET.run(workingtag, new Object[]{name, WrapperClass.NMS_NBTTAGCOMPOUND.getClazz().newInstance()});
                    comp.setCompound(nbttag);
                } catch (Exception var5) {
                    throw new FoException(var5, "Exception while adding a Compound!");
                }
            }
        }
    }

    public static Boolean valideCompound(NBTCompound comp) {
        Object root = comp.getCompound();
        if (root == null) {
            root = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }

        return gettoCompount(root, comp) != null;
    }

    protected static Object gettoCompount(Object nbttag, NBTCompound comp) {
        ArrayDeque structure;
        for(structure = new ArrayDeque(); comp.getParent() != null; comp = comp.getParent()) {
            structure.add(comp.getName());
        }

        String target;
        do {
            if (structure.isEmpty()) {
                return nbttag;
            }

            target = (String)structure.pollLast();
            nbttag = getSubNBTTagCompound(nbttag, target);
        } while(nbttag != null);

        throw new FoException("Unable to find tag '" + target + "' in " + nbttag);
    }

    public static void mergeOtherNBTCompound(NBTCompound comp, NBTCompound nbtcompoundSrc) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }

        if (!valideCompound(comp)) {
            throw new FoException("The Compound wasn't able to be linked back to the root!");
        } else {
            Object workingtag = gettoCompount(rootnbttag, comp);
            Object rootnbttagSrc = nbtcompoundSrc.getCompound();
            if (rootnbttagSrc == null) {
                rootnbttagSrc = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
            }

            if (!valideCompound(nbtcompoundSrc)) {
                throw new FoException("The Compound wasn't able to be linked back to the root!");
            } else {
                Object workingtagSrc = gettoCompount(rootnbttagSrc, nbtcompoundSrc);

                try {
                    WrapperReflection.COMPOUND_MERGE.run(workingtag, new Object[]{workingtagSrc});
                    comp.setCompound(rootnbttag);
                } catch (Exception var7) {
                    throw new FoException(var7, "Exception while merging two NBTCompounds!");
                }
            }
        }
    }

    public static String getContent(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }

        if (!valideCompound(comp)) {
            throw new FoException("The Compound wasn't able to be linked back to the root!");
        } else {
            Object workingtag = gettoCompount(rootnbttag, comp);

            try {
                return WrapperReflection.COMPOUND_GET.run(workingtag, new Object[]{key}).toString();
            } catch (Exception var5) {
                throw new FoException(var5, "Exception while getting the Content for key '" + key + "'!");
            }
        }
    }

    public static void set(NBTCompound comp, String key, Object val) {
        if (val == null) {
            remove(comp, key);
        } else {
            Object rootnbttag = comp.getCompound();
            if (rootnbttag == null) {
                rootnbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
            }

            if (!valideCompound(comp)) {
                throw new FoException("The Compound wasn't able to be linked back to the root!");
            } else {
                Object workingtag = gettoCompount(rootnbttag, comp);

                try {
                    WrapperReflection.COMPOUND_SET.run(workingtag, new Object[]{key, val});
                    comp.setCompound(rootnbttag);
                } catch (Exception var6) {
                    throw new FoException(var6, "Exception while setting key '" + key + "' to '" + val + "'!");
                }
            }
        }
    }

    public static <T> NBTList<T> getList(NBTCompound comp, String key, NBTType type, Class<T> clazz) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }

        if (!valideCompound(comp)) {
            return null;
        } else {
            Object workingtag = gettoCompount(rootnbttag, comp);

            try {
                Object nbt = WrapperReflection.COMPOUND_GET_LIST.run(workingtag, new Object[]{key, type.getId()});
                if (clazz == String.class) {
                    return new NBTStringList(comp, key, type, nbt);
                } else if (clazz == NBTListCompound.class) {
                    return new NBTCompoundList(comp, key, type, nbt);
                } else if (clazz == Integer.class) {
                    return new NBTIntegerList(comp, key, type, nbt);
                } else if (clazz == Float.class) {
                    return new NBTFloatList(comp, key, type, nbt);
                } else if (clazz == Double.class) {
                    return new NBTDoubleList(comp, key, type, nbt);
                } else {
                    return clazz == Long.class ? new NBTLongList(comp, key, type, nbt) : null;
                }
            } catch (Exception var7) {
                throw new FoException("Exception while getting a list with the type '" + type + "'!");
            }
        }
    }

    public static void setObject(NBTCompound comp, String key, Object value) {
        try {
            String json = getJsonString(value);
            setData(comp, WrapperReflection.COMPOUND_SET_STRING, key, json);
        } catch (Exception var4) {
            throw new FoException(var4, "Exception while setting the Object '" + value + "'!");
        }
    }

    public static <T> T getObject(NBTCompound comp, String key, Class<T> type) {
        String json = (String)getData(comp, WrapperReflection.COMPOUND_GET_STRING, key);
        return json == null ? null : deserializeJson(json, type);
    }

    public static void remove(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }

        if (valideCompound(comp)) {
            Object workingtag = gettoCompount(rootnbttag, comp);
            WrapperReflection.COMPOUND_REMOVE_KEY.run(workingtag, new Object[]{key});
            comp.setCompound(rootnbttag);
        }
    }

    public static Set<String> getKeys(NBTCompound comp) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }

        if (!valideCompound(comp)) {
            throw new FoException("The Compound wasn't able to be linked back to the root!");
        } else {
            Object workingtag = gettoCompount(rootnbttag, comp);
            return (Set)WrapperReflection.COMPOUND_GET_KEYS.run(workingtag, new Object[0]);
        }
    }

    public static void setData(NBTCompound comp, WrapperReflection type, String key, Object data) {
        if (data == null) {
            remove(comp, key);
        } else {
            Object rootnbttag = comp.getCompound();
            if (rootnbttag == null) {
                rootnbttag = WrapperObject.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
            }

            if (!valideCompound(comp)) {
                throw new FoException("The Compound wasn't able to be linked back to the root!");
            } else {
                Object workingtag = gettoCompount(rootnbttag, comp);
                type.run(workingtag, new Object[]{key, data});
                comp.setCompound(rootnbttag);
            }
        }
    }

    public static Object getData(NBTCompound comp, WrapperReflection type, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            return null;
        } else if (!valideCompound(comp)) {
            throw new FoException("The Compound wasn't able to be linked back to the root!");
        } else {
            Object workingtag = gettoCompount(rootnbttag, comp);
            return type.run(workingtag, new Object[]{key});
        }
    }

    private static String getJsonString(Object obj) {
        return gson.toJson(obj);
    }

    private static <T> T deserializeJson(String json, Class<T> type) {
        try {
            if (json == null) {
                return null;
            } else {
                T obj = gson.fromJson(json, type);
                return type.cast(obj);
            }
        } catch (Exception var3) {
            throw new FoException("Error while converting json to " + type.getName());
        }
    }

    private NBTReflectionUtil() {
    }

    static {
        try {
            field_unhandledTags = WrapperClass.CRAFT_METAITEM.getClazz().getDeclaredField("unhandledTags");
            field_unhandledTags.setAccessible(true);
        } catch (NoSuchFieldException var1) {
        }

    }
}