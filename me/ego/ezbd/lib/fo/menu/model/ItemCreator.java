package me.ego.ezbd.lib.fo.menu.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.menu.button.Button;
import me.ego.ezbd.lib.fo.menu.button.Button.DummyButton;
import me.ego.ezbd.lib.fo.model.SimpleEnchant;
import me.ego.ezbd.lib.fo.model.SimpleEnchantment;
import me.ego.ezbd.lib.fo.remain.CompColor;
import me.ego.ezbd.lib.fo.remain.CompItemFlag;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.CompMetadata;
import me.ego.ezbd.lib.fo.remain.CompMonsterEgg;
import me.ego.ezbd.lib.fo.remain.CompProperty;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

public final class ItemCreator {
    private final ItemStack item;
    private final CompMaterial material;
    private final int amount;
    private final int damage;
    private final String name;
    private final List<String> lores;
    private final List<SimpleEnchant> enchants;
    private List<CompItemFlag> flags;
    private Boolean unbreakable;
    private final CompColor color;
    private boolean hideTags;
    private final boolean glow;
    private final String skullOwner;
    private final Map<String, String> tags;
    private final ItemMeta meta;

    public void give(Player player) {
        player.getInventory().addItem(new ItemStack[]{this.makeSurvival()});
    }

    public DummyButton makeButton() {
        return Button.makeDummy(this);
    }

    public ItemStack makeMenuTool() {
        this.unbreakable = true;
        this.hideTags = true;
        return this.make();
    }

    public ItemStack makeSurvival() {
        this.hideTags = false;
        return this.make();
    }

    public ItemStack makeSkull(String owner) {
        ItemStack item = this.make();
        Valid.checkBoolean(item.getItemMeta() instanceof SkullMeta, "makeSkull can only be used on skulls", new Object[0]);
        return SkullCreator.itemWithName(item, owner);
    }

    public ItemCreator removeEnchants() {
        if (this.item != null) {
            Iterator var1 = this.item.getEnchantments().keySet().iterator();

            while(var1.hasNext()) {
                Enchantment enchant = (Enchantment)var1.next();
                this.item.removeEnchantment(enchant);
            }
        }

        return this;
    }

    public ItemStack make() {
        Valid.checkBoolean(this.material != null || this.item != null, "Material or item must be set!", new Object[0]);
        ItemStack is = this.item != null ? this.item.clone() : new ItemStack(this.material.getMaterial(), this.amount);
        ItemMeta itemMeta = this.meta != null ? this.meta.clone() : is.getItemMeta();
        if (CompMaterial.isAir(is.getType())) {
            return is;
        } else {
            if (this.material != null) {
                is.setType(this.material.getMaterial());
            }

            if (MinecraftVersion.atLeast(V.v1_12) && this.color != null && !is.getType().toString().contains("LEATHER")) {
                String dye = this.color.getDye().toString();
                List<String> colorableMaterials = Arrays.asList("BANNER", "BED", "CARPET", "CONCRETE", "GLAZED_TERRACOTTA", "SHULKER_BOX", "STAINED_GLASS", "STAINED_GLASS_PANE", "TERRACOTTA", "WALL_BANNER", "WOOL");
                Iterator var5 = colorableMaterials.iterator();

                while(true) {
                    if (!var5.hasNext()) {
                        if (MinecraftVersion.atLeast(V.v1_13)) {
                            is.setType(Material.valueOf(dye + "_WOOL"));
                        } else {
                            this.applyColors0(itemMeta, this.color, this.material, is);
                        }
                        break;
                    }

                    String colorable = (String)var5.next();
                    String suffix = "_" + colorable;
                    if (is.getType().toString().endsWith(suffix)) {
                        is.setType(Material.valueOf(dye + suffix));
                        break;
                    }
                }
            } else {
                this.applyColors0(itemMeta, this.color, this.material, is);
            }

            String lore;
            if (is.getType().toString().endsWith("SPAWN_EGG")) {
                EntityType entity = null;
                if (MinecraftVersion.olderThan(V.v1_13)) {
                    CompMonsterEgg.acceptUnsafeEggs = true;
                    EntityType pre = CompMonsterEgg.getEntity(is);
                    CompMonsterEgg.acceptUnsafeEggs = false;
                    if (pre != null && pre != EntityType.UNKNOWN) {
                        entity = pre;
                    }
                }

                if (entity == null) {
                    String itemName = is.getType().toString();
                    lore = itemName.replace("_SPAWN_EGG", "");
                    if ("MOOSHROOM".equals(lore)) {
                        lore = "MUSHROOM_COW";
                    } else if ("ZOMBIE_PIGMAN".equals(lore)) {
                        lore = "PIG_ZOMBIE";
                    }

                    try {
                        entity = EntityType.valueOf(lore);
                    } catch (Throwable var11) {
                        Common.log(new String[]{"The following item could not be transformed into " + lore + " egg, item: " + is});
                    }
                }

                if (entity != null) {
                    is = CompMonsterEgg.setEntity(is, entity);
                }
            }

            this.flags = new ArrayList((Collection)Common.getOrDefault(this.flags, new ArrayList()));
            if (this.damage != -1) {
                try {
                    ReflectionUtil.invoke("setDurability", is, new Object[]{(short)this.damage});
                } catch (Throwable var10) {
                }

                try {
                    if (itemMeta instanceof Damageable) {
                        ((Damageable)itemMeta).setDamage(this.damage);
                    }
                } catch (Throwable var9) {
                }
            }

            if (this.color != null && is.getType().toString().contains("LEATHER")) {
                ((LeatherArmorMeta)itemMeta).setColor(this.color.getColor());
            }

            if (this.skullOwner != null && itemMeta instanceof SkullMeta) {
                ((SkullMeta)itemMeta).setOwner(this.skullOwner);
            }

            if (this.glow) {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                this.flags.add(CompItemFlag.HIDE_ENCHANTS);
            }

            Iterator var15;
            if (this.enchants != null) {
                var15 = this.enchants.iterator();

                while(var15.hasNext()) {
                    SimpleEnchant ench = (SimpleEnchant)var15.next();
                    if (itemMeta instanceof EnchantmentStorageMeta) {
                        ((EnchantmentStorageMeta)itemMeta).addStoredEnchant(ench.getEnchant(), ench.getLevel(), true);
                    } else {
                        itemMeta.addEnchant(ench.getEnchant(), ench.getLevel(), true);
                    }
                }
            }

            if (this.name != null && !"".equals(this.name)) {
                itemMeta.setDisplayName(Common.colorize("&r" + this.name));
            }

            Iterator var19;
            if (this.lores != null && !this.lores.isEmpty()) {
                List<String> coloredLores = new ArrayList();
                var19 = this.lores.iterator();

                while(var19.hasNext()) {
                    lore = (String)var19.next();
                    coloredLores.add(Common.colorize("&7" + lore));
                }

                itemMeta.setLore(coloredLores);
            }

            if (this.unbreakable != null) {
                this.flags.add(CompItemFlag.HIDE_ATTRIBUTES);
                this.flags.add(CompItemFlag.HIDE_UNBREAKABLE);
                CompProperty.UNBREAKABLE.apply(itemMeta, true);
            }

            if (this.hideTags) {
                CompItemFlag[] var20 = CompItemFlag.values();
                int var21 = var20.length;

                for(int var25 = 0; var25 < var21; ++var25) {
                    CompItemFlag f = var20[var25];
                    if (!this.flags.contains(f)) {
                        this.flags.add(f);
                    }
                }
            }

            var15 = this.flags.iterator();

            while(var15.hasNext()) {
                CompItemFlag flag = (CompItemFlag)var15.next();

                try {
                    itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.valueOf(flag.toString())});
                } catch (Throwable var8) {
                }
            }

            is.setItemMeta(itemMeta);
            ItemStack enchantedIs = SimpleEnchantment.addEnchantmentLores(is);
            if (enchantedIs != null) {
                is = enchantedIs;
            }

            if (this.tags != null) {
                Entry entry;
                if (MinecraftVersion.atLeast(V.v1_8)) {
                    for(var19 = this.tags.entrySet().iterator(); var19.hasNext(); is = CompMetadata.setMetadata(is, (String)entry.getKey(), (String)entry.getValue())) {
                        entry = (Entry)var19.next();
                    }
                } else if (!this.tags.isEmpty() && this.item != null) {
                    Common.log(new String[]{"Item had unsupported tags " + this.tags + " that are not supported on MC " + MinecraftVersion.getServerVersion() + " Item: " + is});
                }
            }

            return is;
        }
    }

    private void applyColors0(ItemMeta itemMeta, CompColor color, CompMaterial material, ItemStack is) {
        int dataValue = material != null ? material.getData() : is.getData().getData();
        if (!is.getType().toString().contains("LEATHER") && color != null) {
            dataValue = color.getDye().getWoolData();
        }

        if (MinecraftVersion.newerThan(V.v1_8) && CompMaterial.isMonsterEgg(is.getType())) {
            dataValue = 0;
        }

        is.setData(new MaterialData(is.getType(), (byte)dataValue));
        if (MinecraftVersion.olderThan(V.v1_13)) {
            is.setDurability((short)dataValue);
        }

        if (itemMeta instanceof LeatherArmorMeta && color != null) {
            ((LeatherArmorMeta)itemMeta).setColor(color.getColor());
        }

    }

    public static ItemCreator.ItemCreatorBuilder of(CompMaterial material, String name, @NonNull Collection<String> lore) {
        if (lore == null) {
            throw new NullPointerException("lore is marked non-null but is null");
        } else {
            return of(material, name, (String[])lore.toArray(new String[0]));
        }
    }

    public static ItemCreator.ItemCreatorBuilder of(String material, String name, @NonNull Collection<String> lore) {
        if (lore == null) {
            throw new NullPointerException("lore is marked non-null but is null");
        } else {
            return of(CompMaterial.valueOf(material), name, (String[])lore.toArray(new String[0]));
        }
    }

    public static ItemCreator.ItemCreatorBuilder of(CompMaterial material, String name, @NonNull String... lore) {
        if (lore == null) {
            throw new NullPointerException("lore is marked non-null but is null");
        } else {
            return builder().material(material).name("&r" + name).lores(Arrays.asList(lore)).hideTags(true);
        }
    }

    public static ItemCreator.ItemCreatorBuilder of(String material, String name, @NonNull String... lore) {
        if (lore == null) {
            throw new NullPointerException("lore is marked non-null but is null");
        } else {
            return builder().material(CompMaterial.valueOf(material)).name("&r" + name).lores(Arrays.asList(lore)).hideTags(true);
        }
    }

    public static ItemCreator.ItemCreatorBuilder ofWool(CompColor color) {
        return of(CompMaterial.makeWool(color, 1)).color(color);
    }

    public static ItemCreator.ItemCreatorBuilder of(ItemStack item) {
        ItemCreator.ItemCreatorBuilder builder = builder();
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            builder.lores(meta.getLore());
        }

        return builder.item(item);
    }

    public static ItemCreator.ItemCreatorBuilder of(CompMaterial mat) {
        Valid.checkNotNull(mat, "Material cannot be null!");
        return builder().material(mat);
    }

    private static int $default$amount() {
        return 1;
    }

    private static int $default$damage() {
        return -1;
    }

    private static boolean $default$hideTags() {
        return false;
    }

    ItemCreator(ItemStack item, CompMaterial material, int amount, int damage, String name, List<String> lores, List<SimpleEnchant> enchants, List<CompItemFlag> flags, Boolean unbreakable, CompColor color, boolean hideTags, boolean glow, String skullOwner, Map<String, String> tags, ItemMeta meta) {
        this.item = item;
        this.material = material;
        this.amount = amount;
        this.damage = damage;
        this.name = name;
        this.lores = lores;
        this.enchants = enchants;
        this.flags = flags;
        this.unbreakable = unbreakable;
        this.color = color;
        this.hideTags = hideTags;
        this.glow = glow;
        this.skullOwner = skullOwner;
        this.tags = tags;
        this.meta = meta;
    }

    public static ItemCreator.ItemCreatorBuilder builder() {
        return new ItemCreator.ItemCreatorBuilder();
    }

    public static class ItemCreatorBuilder {
        private ItemStack item;
        private CompMaterial material;
        private boolean amount$set;
        private int amount$value;
        private boolean damage$set;
        private int damage$value;
        private String name;
        private ArrayList<String> lores;
        private ArrayList<SimpleEnchant> enchants;
        private ArrayList<CompItemFlag> flags;
        private Boolean unbreakable;
        private CompColor color;
        private boolean hideTags$set;
        private boolean hideTags$value;
        private boolean glow;
        private String skullOwner;
        private ArrayList<String> tags$key;
        private ArrayList<String> tags$value;
        private ItemMeta meta;

        ItemCreatorBuilder() {
        }

        public ItemCreator.ItemCreatorBuilder item(ItemStack item) {
            this.item = item;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder material(CompMaterial material) {
            this.material = material;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder amount(int amount) {
            this.amount$value = amount;
            this.amount$set = true;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder damage(int damage) {
            this.damage$value = damage;
            this.damage$set = true;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder lore(String lore) {
            if (this.lores == null) {
                this.lores = new ArrayList();
            }

            this.lores.add(lore);
            return this;
        }

        public ItemCreator.ItemCreatorBuilder lores(Collection<? extends String> lores) {
            if (lores == null) {
                throw new NullPointerException("lores cannot be null");
            } else {
                if (this.lores == null) {
                    this.lores = new ArrayList();
                }

                this.lores.addAll(lores);
                return this;
            }
        }

        public ItemCreator.ItemCreatorBuilder clearLores() {
            if (this.lores != null) {
                this.lores.clear();
            }

            return this;
        }

        public ItemCreator.ItemCreatorBuilder enchant(SimpleEnchant enchant) {
            if (this.enchants == null) {
                this.enchants = new ArrayList();
            }

            this.enchants.add(enchant);
            return this;
        }

        public ItemCreator.ItemCreatorBuilder enchants(Collection<? extends SimpleEnchant> enchants) {
            if (enchants == null) {
                throw new NullPointerException("enchants cannot be null");
            } else {
                if (this.enchants == null) {
                    this.enchants = new ArrayList();
                }

                this.enchants.addAll(enchants);
                return this;
            }
        }

        public ItemCreator.ItemCreatorBuilder clearEnchants() {
            if (this.enchants != null) {
                this.enchants.clear();
            }

            return this;
        }

        public ItemCreator.ItemCreatorBuilder flag(CompItemFlag flag) {
            if (this.flags == null) {
                this.flags = new ArrayList();
            }

            this.flags.add(flag);
            return this;
        }

        public ItemCreator.ItemCreatorBuilder flags(Collection<? extends CompItemFlag> flags) {
            if (flags == null) {
                throw new NullPointerException("flags cannot be null");
            } else {
                if (this.flags == null) {
                    this.flags = new ArrayList();
                }

                this.flags.addAll(flags);
                return this;
            }
        }

        public ItemCreator.ItemCreatorBuilder clearFlags() {
            if (this.flags != null) {
                this.flags.clear();
            }

            return this;
        }

        public ItemCreator.ItemCreatorBuilder unbreakable(Boolean unbreakable) {
            this.unbreakable = unbreakable;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder color(CompColor color) {
            this.color = color;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder hideTags(boolean hideTags) {
            this.hideTags$value = hideTags;
            this.hideTags$set = true;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder glow(boolean glow) {
            this.glow = glow;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder skullOwner(String skullOwner) {
            this.skullOwner = skullOwner;
            return this;
        }

        public ItemCreator.ItemCreatorBuilder tag(String tagKey, String tagValue) {
            if (this.tags$key == null) {
                this.tags$key = new ArrayList();
                this.tags$value = new ArrayList();
            }

            this.tags$key.add(tagKey);
            this.tags$value.add(tagValue);
            return this;
        }

        public ItemCreator.ItemCreatorBuilder tags(Map<? extends String, ? extends String> tags) {
            if (tags == null) {
                throw new NullPointerException("tags cannot be null");
            } else {
                if (this.tags$key == null) {
                    this.tags$key = new ArrayList();
                    this.tags$value = new ArrayList();
                }

                Iterator var2 = tags.entrySet().iterator();

                while(var2.hasNext()) {
                    Entry<? extends String, ? extends String> $lombokEntry = (Entry)var2.next();
                    this.tags$key.add($lombokEntry.getKey());
                    this.tags$value.add($lombokEntry.getValue());
                }

                return this;
            }
        }

        public ItemCreator.ItemCreatorBuilder clearTags() {
            if (this.tags$key != null) {
                this.tags$key.clear();
                this.tags$value.clear();
            }

            return this;
        }

        public ItemCreator.ItemCreatorBuilder meta(ItemMeta meta) {
            this.meta = meta;
            return this;
        }

        public ItemCreator build() {
            List lores;
            switch(this.lores == null ? 0 : this.lores.size()) {
            case 0:
                lores = Collections.emptyList();
                break;
            case 1:
                lores = Collections.singletonList(this.lores.get(0));
                break;
            default:
                lores = Collections.unmodifiableList(new ArrayList(this.lores));
            }

            List enchants;
            switch(this.enchants == null ? 0 : this.enchants.size()) {
            case 0:
                enchants = Collections.emptyList();
                break;
            case 1:
                enchants = Collections.singletonList(this.enchants.get(0));
                break;
            default:
                enchants = Collections.unmodifiableList(new ArrayList(this.enchants));
            }

            List flags;
            switch(this.flags == null ? 0 : this.flags.size()) {
            case 0:
                flags = Collections.emptyList();
                break;
            case 1:
                flags = Collections.singletonList(this.flags.get(0));
                break;
            default:
                flags = Collections.unmodifiableList(new ArrayList(this.flags));
            }

            Map tags;
            int amount$value;
            switch(this.tags$key == null ? 0 : this.tags$key.size()) {
            case 0:
                tags = Collections.emptyMap();
                break;
            case 1:
                tags = Collections.singletonMap(this.tags$key.get(0), this.tags$value.get(0));
                break;
            default:
                Map<String, String> tags = new LinkedHashMap(this.tags$key.size() < 1073741824 ? 1 + this.tags$key.size() + (this.tags$key.size() - 3) / 3 : 2147483647);

                for(amount$value = 0; amount$value < this.tags$key.size(); ++amount$value) {
                    tags.put(this.tags$key.get(amount$value), (String)this.tags$value.get(amount$value));
                }

                tags = Collections.unmodifiableMap(tags);
            }

            amount$value = this.amount$value;
            if (!this.amount$set) {
                amount$value = ItemCreator.$default$amount();
            }

            int damage$value = this.damage$value;
            if (!this.damage$set) {
                damage$value = ItemCreator.$default$damage();
            }

            boolean hideTags$value = this.hideTags$value;
            if (!this.hideTags$set) {
                hideTags$value = ItemCreator.$default$hideTags();
            }

            return new ItemCreator(this.item, this.material, amount$value, damage$value, this.name, lores, enchants, flags, this.unbreakable, this.color, hideTags$value, this.glow, this.skullOwner, tags, this.meta);
        }

        public String toString() {
            return "ItemCreator.ItemCreatorBuilder(item=" + this.item + ", material=" + this.material + ", amount$value=" + this.amount$value + ", damage$value=" + this.damage$value + ", name=" + this.name + ", lores=" + this.lores + ", enchants=" + this.enchants + ", flags=" + this.flags + ", unbreakable=" + this.unbreakable + ", color=" + this.color + ", hideTags$value=" + this.hideTags$value + ", glow=" + this.glow + ", skullOwner=" + this.skullOwner + ", tags$key=" + this.tags$key + ", tags$value=" + this.tags$value + ", meta=" + this.meta + ")";
        }
    }
}