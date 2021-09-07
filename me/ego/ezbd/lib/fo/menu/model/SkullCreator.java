package me.ego.ezbd.lib.fo.menu.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.UUID;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullCreator {
    private static Field blockProfileField;
    private static Method metaSetProfileMethod;
    private static Field metaProfileField;

    public static ItemStack createSkull() {
        try {
            return new ItemStack(Material.valueOf("PLAYER_HEAD"));
        } catch (IllegalArgumentException var1) {
            return new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short)3);
        }
    }

    public static ItemStack itemFromName(String name) {
        return itemWithName(createSkull(), name);
    }

    public static ItemStack itemFromUuid(UUID id) {
        return itemWithUuid(createSkull(), id);
    }

    public static ItemStack itemFromUrl(String url) {
        return itemWithUrl(createSkull(), url);
    }

    public static ItemStack itemFromBase64(String base64) {
        return itemWithBase64(createSkull(), base64);
    }

    public static ItemStack itemWithName(@NonNull ItemStack item, @NonNull String name) {
        if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
        } else if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
        } else {
            SkullMeta meta = (SkullMeta)item.getItemMeta();
            meta.setOwner(name);
            item.setItemMeta(meta);
            return item;
        }
    }

    public static ItemStack itemWithUuid(@NonNull ItemStack item, @NonNull UUID id) {
        if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
        } else if (id == null) {
            throw new NullPointerException("id is marked non-null but is null");
        } else {
            SkullMeta meta = (SkullMeta)item.getItemMeta();

            try {
                meta.setOwningPlayer(Remain.getOfflinePlayerByUUID(id));
            } catch (Throwable var4) {
                meta.setOwner(Remain.getOfflinePlayerByUUID(id).getName());
            }

            item.setItemMeta(meta);
            return item;
        }
    }

    public static ItemStack itemWithUrl(@NonNull ItemStack item, @NonNull String url) {
        if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
        } else if (url == null) {
            throw new NullPointerException("url is marked non-null but is null");
        } else {
            return itemWithBase64(item, urlToBase64(url));
        }
    }

    public static ItemStack itemWithBase64(@NonNull ItemStack item, @NonNull String base64) {
        if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
        } else if (base64 == null) {
            throw new NullPointerException("base64 is marked non-null but is null");
        } else if (!(item.getItemMeta() instanceof SkullMeta)) {
            return null;
        } else {
            SkullMeta meta = (SkullMeta)item.getItemMeta();
            mutateItemMeta(meta, base64);
            item.setItemMeta(meta);
            return item;
        }
    }

    public static void blockWithUuid(@NonNull Block block, @NonNull UUID id) {
        if (block == null) {
            throw new NullPointerException("block is marked non-null but is null");
        } else if (id == null) {
            throw new NullPointerException("id is marked non-null but is null");
        } else {
            setToSkull(block);
            Skull state = (Skull)block.getState();

            try {
                state.setOwningPlayer(Remain.getOfflinePlayerByUUID(id));
            } catch (Throwable var4) {
                state.setOwner(Remain.getOfflinePlayerByUUID(id).getName());
            }

            state.update(false, false);
        }
    }

    public static void blockWithUrl(@NonNull Block block, @NonNull String url) {
        if (block == null) {
            throw new NullPointerException("block is marked non-null but is null");
        } else if (url == null) {
            throw new NullPointerException("url is marked non-null but is null");
        } else {
            blockWithBase64(block, urlToBase64(url));
        }
    }

    public static void blockWithBase64(@NonNull Block block, @NonNull String base64) {
        if (block == null) {
            throw new NullPointerException("block is marked non-null but is null");
        } else if (base64 == null) {
            throw new NullPointerException("base64 is marked non-null but is null");
        } else {
            setToSkull(block);
            Skull state = (Skull)block.getState();
            mutateBlockState(state, base64);
            state.update(false, false);
        }
    }

    private static void setToSkull(Block block) {
        try {
            block.setType(Material.valueOf("PLAYER_HEAD"), false);
        } catch (IllegalArgumentException var3) {
            block.setType(Material.valueOf("SKULL"), false);
            Skull state = (Skull)block.getState();
            state.setSkullType(SkullType.PLAYER);
            state.update(false, false);
        }

    }

    private static String urlToBase64(String url) {
        URI actualUrl;
        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException var3) {
            throw new RuntimeException(var3);
        }

        String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl.toString() + "\"}}}";
        return Base64.getEncoder().encodeToString(toEncode.getBytes());
    }

    private static Object makeProfile(String b64) {
        UUID id = new UUID((long)b64.substring(b64.length() - 20).hashCode(), (long)b64.substring(b64.length() - 10).hashCode());

        try {
            Class<?> gameProfileClass = ReflectionUtil.lookupClass("com.mojang.authlib.GameProfile");
            Object profile = ReflectionUtil.instantiate(gameProfileClass.getConstructor(UUID.class, String.class), new Object[]{id, "aaaaa"});
            Class<?> propertyClass = ReflectionUtil.lookupClass("com.mojang.authlib.properties.Property");
            Object property = ReflectionUtil.instantiate(propertyClass.getConstructor(String.class, String.class), new Object[]{"textures", b64});
            Object propertyMap = ReflectionUtil.invoke("getProperties", profile, new Object[0]);
            ReflectionUtil.invoke("put", propertyMap, new Object[]{"textures", property});
            return profile;
        } catch (ReflectiveOperationException var7) {
            Common.throwError(var7, new String[0]);
            return null;
        }
    }

    private static void mutateBlockState(Skull block, String b64) {
        try {
            if (blockProfileField == null) {
                blockProfileField = block.getClass().getDeclaredField("profile");
                blockProfileField.setAccessible(true);
            }

            blockProfileField.set(block, makeProfile(b64));
        } catch (IllegalAccessException | NoSuchFieldException var3) {
            var3.printStackTrace();
        }

    }

    private static void mutateItemMeta(SkullMeta meta, String b64) {
        try {
            if (metaSetProfileMethod == null) {
                metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", ReflectionUtil.lookupClass("com.mojang.authlib.GameProfile"));
                metaSetProfileMethod.setAccessible(true);
            }

            metaSetProfileMethod.invoke(meta, makeProfile(b64));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var5) {
            try {
                if (metaProfileField == null) {
                    metaProfileField = meta.getClass().getDeclaredField("profile");
                    metaProfileField.setAccessible(true);
                }

                metaProfileField.set(meta, makeProfile(b64));
            } catch (IllegalAccessException | NoSuchFieldException var4) {
                var4.printStackTrace();
            }
        }

    }

    private SkullCreator() {
    }
}