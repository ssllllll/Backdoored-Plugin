package me.ego.ezbd.lib.fo.model;

import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator.ItemCreatorBuilder;
import me.ego.ezbd.lib.fo.remain.CompEquipmentSlot;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/** @deprecated */
@Deprecated
public final class SimpleEquipment {
    private final EntityEquipment equipment;

    public SimpleEquipment(LivingEntity entity) {
        this(entity.getEquipment());
    }

    public SimpleEquipment(EntityEquipment equipment) {
        this.equipment = equipment;
    }

    public void set(CompEquipmentSlot slot, CompMaterial material) {
        this.set(slot, material.toItem());
    }

    public void set(CompEquipmentSlot slot, ItemCreatorBuilder builder) {
        this.set(slot, builder.build().make());
    }

    public void set(CompEquipmentSlot slot, ItemStack item) {
        Valid.checkNotNull(item, "Equipment item cannot be null");
        this.set(slot, (ItemStack)item, (Float)null);
    }

    public void set(CompEquipmentSlot slot, float dropChance) {
        this.set(slot, (ItemStack)null, dropChance);
    }

    public void set(CompEquipmentSlot slot, @Nullable CompMaterial material, @Nullable Float dropChance) {
        this.set(slot, material.toItem(), dropChance);
    }

    public void set(CompEquipmentSlot slot, @Nullable ItemCreatorBuilder builder, @Nullable Float dropChance) {
        this.set(slot, builder.build().make(), dropChance);
    }

    public void set(CompEquipmentSlot slot, @Nullable ItemStack item, @Nullable Float dropChance) {
        Valid.checkBoolean(item != null || dropChance != null, "Either item or drop chance must be given!", new Object[0]);
        if (slot.toString().equals("OFF_HAND") && MinecraftVersion.olderThan(V.v1_9)) {
            slot = CompEquipmentSlot.HAND;
        }

        if (slot == CompEquipmentSlot.HEAD) {
            if (item != null) {
                this.equipment.setHelmet(item);
            }

            if (dropChance != null) {
                this.equipment.setHelmetDropChance(dropChance);
            }
        } else if (slot == CompEquipmentSlot.CHEST) {
            if (item != null) {
                this.equipment.setChestplate(item);
            }

            if (dropChance != null) {
                this.equipment.setChestplateDropChance(dropChance);
            }
        } else if (slot == CompEquipmentSlot.LEGS) {
            if (item != null) {
                this.equipment.setLeggings(item);
            }

            if (dropChance != null) {
                this.equipment.setLeggingsDropChance(dropChance);
            }
        } else if (slot == CompEquipmentSlot.FEET) {
            if (item != null) {
                this.equipment.setBoots(item);
            }

            if (dropChance != null) {
                this.equipment.setBootsDropChance(dropChance);
            }
        } else if (slot == CompEquipmentSlot.HAND) {
            if (item != null) {
                this.equipment.setItemInHand(item);
            }

            if (dropChance != null) {
                this.equipment.setItemInHandDropChance(dropChance);
            }
        } else {
            if (!slot.toString().equals("OFF_HAND")) {
                throw new FoException("Does not know how to set " + slot + " to " + item);
            }

            try {
                if (item != null) {
                    this.equipment.setItemInOffHand(item);
                }

                if (dropChance != null) {
                    this.equipment.setItemInOffHandDropChance(dropChance);
                }
            } catch (Throwable var5) {
            }
        }

    }

    public ItemStack[] getArmorContents() {
        return this.equipment.getArmorContents();
    }

    public void setContent(ItemCreatorBuilder helmet, ItemCreatorBuilder chest, ItemCreatorBuilder leggings, ItemCreatorBuilder boots) {
        this.setContent(helmet.build().make(), chest.build().make(), leggings.build().make(), boots.build().make());
    }

    public void setContent(CompMaterial helmet, CompMaterial chest, CompMaterial leggings, CompMaterial boots) {
        this.setContent(helmet.toItem(), chest.toItem(), leggings.toItem(), boots.toItem());
    }

    public void setContent(ItemStack helmet, ItemStack chest, ItemStack leggings, ItemStack boots) {
        this.set(CompEquipmentSlot.HEAD, helmet);
        this.set(CompEquipmentSlot.CHEST, chest);
        this.set(CompEquipmentSlot.FEET, leggings);
        this.set(CompEquipmentSlot.LEGS, boots);
    }

    public void clear() {
        this.equipment.clear();
    }

    public EntityEquipment getEquipment() {
        return this.equipment;
    }
}