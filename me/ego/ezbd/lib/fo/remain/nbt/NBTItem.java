package me.ego.ezbd.lib.fo.remain.nbt;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NBTItem extends NBTCompound {
    private ItemStack bukkitItem;
    private final boolean directApply;
    private ItemStack originalSrcStack;

    public NBTItem(ItemStack item) {
        this(item, false);
    }

    public NBTItem(ItemStack item, boolean directApply) {
        super((NBTCompound)null, (String)null);
        this.originalSrcStack = null;
        if (item != null && item.getType() != Material.AIR) {
            this.directApply = directApply;
            this.bukkitItem = item.clone();
            if (directApply) {
                this.originalSrcStack = item;
            }

        } else {
            throw new NullPointerException("ItemStack can't be null/Air!");
        }
    }

    public Object getCompound() {
        return NBTReflectionUtil.getItemRootNBTTagCompound(WrapperReflection.ITEMSTACK_NMSCOPY.run((Object)null, new Object[]{this.bukkitItem}));
    }

    protected void setCompound(Object compound) {
        Object stack = WrapperReflection.ITEMSTACK_NMSCOPY.run((Object)null, new Object[]{this.bukkitItem});
        WrapperReflection.ITEMSTACK_SET_TAG.run(stack, new Object[]{compound});
        this.bukkitItem = (ItemStack)WrapperReflection.ITEMSTACK_BUKKITMIRROR.run((Object)null, new Object[]{stack});
    }

    public void applyNBT(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbti = new NBTItem(new ItemStack(item.getType()));
            nbti.mergeCompound(this);
            item.setItemMeta(nbti.getItem().getItemMeta());
        } else {
            throw new NullPointerException("ItemStack can't be null/Air!");
        }
    }

    public void mergeNBT(ItemStack item) {
        NBTItem nbti = new NBTItem(item);
        nbti.mergeCompound(this);
        item.setItemMeta(nbti.getItem().getItemMeta());
    }

    public void mergeCustomNBT(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            NBTReflectionUtil.getUnhandledNBTTags(meta).putAll(NBTReflectionUtil.getUnhandledNBTTags(this.bukkitItem.getItemMeta()));
            item.setItemMeta(meta);
        } else {
            throw new NullPointerException("ItemStack can't be null/Air!");
        }
    }

    public void clearCustomNBT() {
        ItemMeta meta = this.bukkitItem.getItemMeta();
        NBTReflectionUtil.getUnhandledNBTTags(meta).clear();
        this.bukkitItem.setItemMeta(meta);
    }

    public ItemStack getItem() {
        return this.bukkitItem;
    }

    protected void setItem(ItemStack item) {
        this.bukkitItem = item;
    }

    public boolean hasNBTData() {
        return this.getCompound() != null;
    }

    public static NBTContainer convertItemtoNBT(ItemStack item) {
        return NBTReflectionUtil.convertNMSItemtoNBTCompound(WrapperReflection.ITEMSTACK_NMSCOPY.run((Object)null, new Object[]{item}));
    }

    public static ItemStack convertNBTtoItem(NBTCompound comp) {
        return (ItemStack)WrapperReflection.ITEMSTACK_BUKKITMIRROR.run((Object)null, new Object[]{NBTReflectionUtil.convertNBTCompoundtoNMSItem(comp)});
    }

    protected void saveCompound() {
        if (this.directApply) {
            this.applyNBT(this.originalSrcStack);
        }

    }
}