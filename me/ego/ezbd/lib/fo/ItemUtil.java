package me.ego.ezbd.lib.fo;

import java.util.ArrayList;
import java.util.List;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompChatColor;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.nbt.NBTItem;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

public final class ItemUtil {
    private static final boolean LEGACY_MATERIALS;

    public static PotionEffectType findPotion(String name) {
        name = PotionWrapper.getBukkitName(name);
        PotionEffectType potion = PotionEffectType.getByName(name);
        Valid.checkNotNull(potion, "Invalid potion '" + name + "'! For valid names, see: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
        return potion;
    }

    public static Enchantment findEnchantment(String name) {
        Enchantment enchant = Enchantment.getByName(name);
        if (enchant == null) {
            enchant = Enchantment.getByName(name.toLowerCase());
        }

        if (enchant == null) {
            name = EnchantmentWrapper.toBukkit(name);
            enchant = Enchantment.getByName(name.toLowerCase());
            if (enchant == null) {
                enchant = Enchantment.getByName(name);
            }
        }

        Valid.checkNotNull(enchant, "Invalid enchantment '" + name + "'! For valid names, see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");
        return enchant;
    }

    public static String bountifyCapitalized(CompChatColor color) {
        return bountifyCapitalized(color.getName());
    }

    public static String bountifyCapitalized(Enum<?> enumeration) {
        return WordUtils.capitalizeFully(bountify(enumeration.toString().toLowerCase()));
    }

    public static String bountifyCapitalized(String name) {
        return WordUtils.capitalizeFully(bountify(name));
    }

    public static String bountify(Enum<?> enumeration) {
        return bountify(enumeration.toString());
    }

    public static String bountify(String name) {
        return name.toLowerCase().replace("_", " ");
    }

    public static String bountify(PotionEffectType enumeration) {
        return PotionWrapper.getLocalizedName(enumeration.getName());
    }

    public static String bountify(Enchantment enchant) {
        return EnchantmentWrapper.toMinecraft(enchant.getName());
    }

    public static boolean isSimilar(ItemStack first, ItemStack second) {
        if (first != null && second != null) {
            boolean firstAir = CompMaterial.isAir(first.getType());
            boolean secondAir = CompMaterial.isAir(second.getType());
            if (firstAir && !secondAir || !firstAir && secondAir) {
                return false;
            } else if (firstAir && secondAir) {
                return true;
            } else {
                boolean idMatch = first.getType() == second.getType();
                boolean dataMatch = !LEGACY_MATERIALS || first.getData().getData() == second.getData().getData();
                boolean metaMatch = first.hasItemMeta() == second.hasItemMeta();
                if (idMatch && metaMatch && (dataMatch || first.getType() == Material.BOW)) {
                    ItemMeta f = first.getItemMeta();
                    ItemMeta s = second.getItemMeta();
                    if ((f != null || s == null) && (s != null || f == null)) {
                        String fName = f == null ? "" : Common.stripColors(Common.getOrEmpty(f.getDisplayName()));
                        String sName = s == null ? "" : Common.stripColors(Common.getOrEmpty(s.getDisplayName()));
                        if ((fName == null || fName.equalsIgnoreCase(sName)) && Valid.listEquals((List)(f == null ? new ArrayList() : f.getLore()), (List)(s == null ? new ArrayList() : s.getLore()))) {
                            if (!MinecraftVersion.atLeast(V.v1_7)) {
                                return true;
                            } else {
                                NBTItem firstNbt = new NBTItem(first);
                                NBTItem secondNbt = new NBTItem(second);
                                return matchNbt(SimplePlugin.getNamed(), firstNbt, secondNbt) && matchNbt(SimplePlugin.getNamed() + "_Item", firstNbt, secondNbt);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private static boolean matchNbt(String key, NBTItem firstNbt, NBTItem secondNbt) {
        boolean firstHas = firstNbt.hasKey(key);
        boolean secondHas = secondNbt.hasKey(key);
        if (!firstHas && !secondHas) {
            return true;
        } else {
            return (!firstHas || secondHas) && (firstHas || !secondHas) ? firstNbt.getString(key).equals(secondNbt.getString(key)) : false;
        }
    }

    private ItemUtil() {
    }

    static {
        LEGACY_MATERIALS = MinecraftVersion.olderThan(V.v1_13);
    }
}