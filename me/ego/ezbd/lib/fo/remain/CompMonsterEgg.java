package me.ego.ezbd.lib.fo.remain;

import java.lang.reflect.Method;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.nbt.NBTCompound;
import me.ego.ezbd.lib.fo.remain.nbt.NBTItem;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

public final class CompMonsterEgg {
    private static final String TAG = SimplePlugin.getNamed() + "_NbtTag";
    public static boolean acceptUnsafeEggs = false;

    private CompMonsterEgg() {
    }

    public static ItemStack makeEgg(EntityType type) {
        return makeEgg(type, 1);
    }

    public static ItemStack makeEgg(EntityType type, int count) {
        Valid.checkNotNull(type, "Entity type cannot be null!");
        ItemStack itemStack = new ItemStack(CompMaterial.makeMonsterEgg(type).getMaterial(), count);
        if (itemStack.getType().toString().equals("MONSTER_EGG")) {
            itemStack = setEntity(itemStack, type);
        }

        return itemStack;
    }

    public static EntityType getEntity(ItemStack item) {
        Valid.checkBoolean(CompMaterial.isMonsterEgg(item.getType()), "Item must be a monster egg not " + item, new Object[0]);
        EntityType type = null;
        if (MinecraftVersion.atLeast(V.v1_13)) {
            type = getTypeFromMaterial(item);
        } else if (Remain.hasSpawnEggMeta()) {
            type = getTypeByMeta(item);
        } else {
            type = getTypeByData(item);
        }

        if (type == null) {
            type = getTypeByNbt(item);
        }

        if (type == null && acceptUnsafeEggs) {
            type = EntityType.UNKNOWN;
        }

        Valid.checkNotNull(type, "Could not detect monster type from " + item + " (data = " + item.getData() + ", dura = " + item.getDurability() + ")");
        return type;
    }

    private static EntityType getTypeFromMaterial(ItemStack item) {
        String name = item.getType().toString().replace("_SPAWN_EGG", "");
        EntityType type = null;

        try {
            type = EntityType.valueOf(name);
        } catch (IllegalArgumentException var8) {
            EntityType[] var4 = EntityType.values();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                EntityType all = var4[var6];
                if (all.getName() != null && all.getName().equalsIgnoreCase(name)) {
                    type = all;
                }
            }
        }

        Valid.checkNotNull(type, "Unable to find EntityType from Material." + item.getType());
        return type;
    }

    private static EntityType getTypeByMeta(ItemStack item) {
        ItemMeta m = item.getItemMeta();
        return item.hasItemMeta() && m instanceof SpawnEggMeta ? ((SpawnEggMeta)m).getSpawnedType() : null;
    }

    private static EntityType getTypeByData(ItemStack item) {
        EntityType type = readEntity0(item);
        if (type == null) {
            if (item.getDurability() != 0) {
                type = DataMap.getEntity(item.getDurability());
            }

            if (type == null && item.getData().getData() != 0) {
                type = DataMap.getEntity(item.getData().getData());
            }
        }

        return type;
    }

    private static EntityType readEntity0(ItemStack item) {
        Valid.checkNotNull(item, "Reading entity got null item");
        NBTItem nbt = new NBTItem(item);
        String type = nbt.hasKey(TAG) ? nbt.getCompound(TAG).getString("entity") : null;
        return type != null && !type.isEmpty() ? EntityType.valueOf(type) : null;
    }

    private static EntityType getTypeByNbt(@NonNull ItemStack item) {
        if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
        } else {
            try {
                Class<?> NMSItemStackClass = ReflectionUtil.getNMSClass("ItemStack");
                Class<?> craftItemStackClass = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
                Method asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                Object stack = asNMSCopyMethod.invoke((Object)null, item);
                Object tagCompound = NMSItemStackClass.getMethod("getTag").invoke(stack);
                if (tagCompound == null && acceptUnsafeEggs) {
                    return null;
                } else {
                    Valid.checkNotNull(tagCompound, "Spawn egg lacks tag compound: " + item);
                    Method tagGetCompound = tagCompound.getClass().getMethod("getCompound", String.class);
                    Object entityTag = tagGetCompound.invoke(tagCompound, "EntityTag");
                    Method tagGetString = entityTag.getClass().getMethod("getString", String.class);
                    String idString = (String)tagGetString.invoke(entityTag, "id");
                    if (MinecraftVersion.atLeast(V.v1_11) && idString.startsWith("minecraft:")) {
                        idString = idString.split("minecraft:")[1];
                    }

                    EntityType type = EntityType.fromName(idString);
                    return type;
                }
            } catch (ReflectiveOperationException var11) {
                var11.printStackTrace();
                return null;
            }
        }
    }

    public static ItemStack setEntity(ItemStack item, EntityType type) {
        Valid.checkNotNull(item, "Item == null");
        Valid.checkBoolean(type.isSpawnable(), type + " cannot be spawned and thus set into a monster egg!", new Object[0]);
        if (MinecraftVersion.atLeast(V.v1_13)) {
            item.setType(CompMaterial.makeMonsterEgg(type).getMaterial());
            return item;
        } else {
            Valid.checkBoolean(CompMaterial.isMonsterEgg(item.getType()), "Item must be a monster egg not " + item, new Object[0]);
            if (Remain.hasSpawnEggMeta()) {
                item = setTypeByMeta(item, type);
            } else {
                item = setTypeByData(item, type);
            }

            return item;
        }
    }

    private static ItemStack setTypeByMeta(ItemStack item, EntityType type) {
        SpawnEggMeta m = (SpawnEggMeta)item.getItemMeta();
        m.setSpawnedType(type);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack setTypeByData(ItemStack item, EntityType type) {
        Number data = DataMap.getData(type);
        if (data.intValue() != -1) {
            item.setDurability(data.shortValue());
            item.getData().setData(data.byteValue());
            return writeEntity0(item, type);
        } else if (!acceptUnsafeEggs) {
            throw new FoException("Could not set monster egg to " + type);
        } else {
            return item;
        }
    }

    private static ItemStack writeEntity0(ItemStack item, EntityType type) {
        Valid.checkNotNull(item, "setting nbt got null item");
        Valid.checkNotNull(type, "setting nbt got null entity");
        NBTItem nbt = new NBTItem(item);
        NBTCompound tag = nbt.addCompound(TAG);
        tag.setString("entity", type.toString());
        return nbt.getItem();
    }
}