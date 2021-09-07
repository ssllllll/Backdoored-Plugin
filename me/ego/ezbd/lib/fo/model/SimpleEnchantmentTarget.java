package me.ego.ezbd.lib.fo.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum SimpleEnchantmentTarget {
    ARMOR {
        public boolean includes(@NotNull Material item) {
            return ARMOR_FEET.includes(item) || ARMOR_LEGS.includes(item) || ARMOR_HEAD.includes(item) || ARMOR_TORSO.includes(item);
        }
    },
    ARMOR_FEET {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_BOOTS) || item.equals(Material.CHAINMAIL_BOOTS) || item.equals(Material.IRON_BOOTS) || item.equals(Material.DIAMOND_BOOTS) || item.equals(Material.GOLDEN_BOOTS) || item.equals(Material.NETHERITE_BOOTS);
        }
    },
    ARMOR_LEGS {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_LEGGINGS) || item.equals(Material.CHAINMAIL_LEGGINGS) || item.equals(Material.IRON_LEGGINGS) || item.equals(Material.DIAMOND_LEGGINGS) || item.equals(Material.GOLDEN_LEGGINGS) || item.equals(Material.NETHERITE_LEGGINGS);
        }
    },
    ARMOR_TORSO {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_CHESTPLATE) || item.equals(Material.CHAINMAIL_CHESTPLATE) || item.equals(Material.IRON_CHESTPLATE) || item.equals(Material.DIAMOND_CHESTPLATE) || item.equals(Material.GOLDEN_CHESTPLATE) || item.equals(Material.NETHERITE_CHESTPLATE);
        }
    },
    ARMOR_HEAD {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_HELMET) || item.equals(Material.CHAINMAIL_HELMET) || item.equals(Material.DIAMOND_HELMET) || item.equals(Material.IRON_HELMET) || item.equals(Material.GOLDEN_HELMET) || item.equals(Material.TURTLE_HELMET) || item.equals(Material.NETHERITE_HELMET);
        }
    },
    WEAPON {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_SWORD) || item.equals(Material.STONE_SWORD) || item.equals(Material.IRON_SWORD) || item.equals(Material.DIAMOND_SWORD) || item.equals(Material.GOLDEN_SWORD) || item.equals(Material.NETHERITE_SWORD);
        }
    },
    TOOL {
        public boolean includes(@NotNull Material item) {
            return SHOVEL.includes(item) || PICKAXE.includes(item) || AXE.includes(item) || HOE.includes(item);
        }
    },
    SHOVEL {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_SHOVEL) || item.equals(Material.STONE_SHOVEL) || item.equals(Material.IRON_SHOVEL) || item.equals(Material.DIAMOND_SHOVEL) || item.equals(Material.GOLDEN_SHOVEL) || item.equals(Material.NETHERITE_SHOVEL);
        }
    },
    PICKAXE {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_PICKAXE) || item.equals(Material.STONE_PICKAXE) || item.equals(Material.IRON_PICKAXE) || item.equals(Material.DIAMOND_PICKAXE) || item.equals(Material.GOLDEN_PICKAXE) || item.equals(Material.NETHERITE_PICKAXE);
        }
    },
    AXE {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_AXE) || item.equals(Material.STONE_AXE) || item.equals(Material.IRON_AXE) || item.equals(Material.DIAMOND_AXE) || item.equals(Material.GOLDEN_AXE) || item.equals(Material.NETHERITE_AXE);
        }
    },
    HOE {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_HOE) || item.equals(Material.STONE_HOE) || item.equals(Material.IRON_HOE) || item.equals(Material.DIAMOND_HOE) || item.equals(Material.GOLDEN_HOE) || item.equals(Material.NETHERITE_HOE);
        }
    },
    BOW {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.BOW);
        }
    },
    FISHING_ROD {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.FISHING_ROD);
        }
    },
    BREAKABLE {
        public boolean includes(@NotNull Material item) {
            return item.getMaxDurability() > 0 && item.getMaxStackSize() == 1;
        }
    },
    WEARABLE {
        public boolean includes(@NotNull Material item) {
            return ARMOR.includes(item) || ELYTRA.includes(item) || item.equals(Material.CARVED_PUMPKIN) || item.equals(Material.JACK_O_LANTERN) || item.equals(Material.SKELETON_SKULL) || item.equals(Material.WITHER_SKELETON_SKULL) || item.equals(Material.ZOMBIE_HEAD) || item.equals(Material.PLAYER_HEAD) || item.equals(Material.CREEPER_HEAD) || item.equals(Material.DRAGON_HEAD);
        }
    },
    ELYTRA {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.ELYTRA);
        }
    },
    TRIDENT {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.TRIDENT);
        }
    },
    CROSSBOW {
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.CROSSBOW);
        }
    },
    VANISHABLE {
        public boolean includes(@NotNull Material item) {
            return BREAKABLE.includes(item) || WEARABLE.includes(item) && !item.equals(Material.ELYTRA) || item.equals(Material.COMPASS);
        }
    };

    private SimpleEnchantmentTarget() {
    }

    public abstract boolean includes(@NotNull Material var1);

    public boolean includes(@NotNull ItemStack item) {
        return this.includes(item.getType());
    }
}