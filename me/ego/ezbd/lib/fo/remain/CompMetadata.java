package me.ego.ezbd.lib.fo.remain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import me.ego.ezbd.lib.fo.constants.FoConstants.NBT;
import me.ego.ezbd.lib.fo.model.ConfigSerializable;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.nbt.NBTCompound;
import me.ego.ezbd.lib.fo.remain.nbt.NBTItem;
import me.ego.ezbd.lib.fo.settings.YamlSectionConfig;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataType;

public final class CompMetadata {
    private static final String DELIMITER = "%-%";

    private CompMetadata() {
    }

    public static ItemStack setMetadata(ItemStack item, String key, String value) {
        Valid.checkNotNull(item, "Setting NBT tag got null item");
        NBTItem nbt = new NBTItem(item);
        NBTCompound tag = nbt.addCompound(NBT.TAG);
        tag.setString(key, value);
        return nbt.getItem();
    }

    public static void setMetadata(Entity entity, String tag) {
        setMetadata(entity, tag, tag);
    }

    public static void setMetadata(Entity entity, String key, String value) {
        Valid.checkNotNull(entity);
        String tag = format(key, value);
        if (Remain.hasScoreboardTags()) {
            if (!entity.getScoreboardTags().contains(tag)) {
                entity.addScoreboardTag(tag);
            }
        } else {
            entity.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(), value));
            CompMetadata.MetadataFile.getInstance().addMetadata(entity, key, value);
        }

    }

    private static String format(String key, String value) {
        return SimplePlugin.getNamed() + "%-%" + key + "%-%" + value;
    }

    public static void setMetadata(BlockState tileEntity, String key, String value) {
        Valid.checkNotNull(tileEntity);
        Valid.checkNotNull(key);
        Valid.checkNotNull(value);
        if (MinecraftVersion.atLeast(V.v1_14)) {
            Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity, new Object[0]);
            setNamedspaced((TileState)tileEntity, key, value);
            tileEntity.update();
        } else {
            tileEntity.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(), value));
            tileEntity.update();
            CompMetadata.MetadataFile.getInstance().addMetadata(tileEntity, key, value);
        }

    }

    private static void setNamedspaced(TileState tile, String key, String value) {
        tile.getPersistentDataContainer().set(new NamespacedKey(SimplePlugin.getInstance(), key), PersistentDataType.STRING, value);
    }

    public static String getMetadata(ItemStack item, String key) {
        Valid.checkNotNull(item, "Reading NBT tag got null item");
        if (item != null && !CompMaterial.isAir(item.getType())) {
            String compoundTag = NBT.TAG;
            NBTItem nbt = new NBTItem(item);
            String value = nbt.hasKey(compoundTag) ? nbt.getCompound(compoundTag).getString(key) : null;
            return Common.getOrNull(value);
        } else {
            return null;
        }
    }

    public static String getMetadata(Entity entity, String key) {
        Valid.checkNotNull(entity);
        if (Remain.hasScoreboardTags()) {
            Iterator var2 = entity.getScoreboardTags().iterator();

            while(var2.hasNext()) {
                String line = (String)var2.next();
                String tag = getTag(line, key);
                if (tag != null && !tag.isEmpty()) {
                    return tag;
                }
            }
        }

        String value = entity.hasMetadata(key) ? ((MetadataValue)entity.getMetadata(key).get(0)).asString() : null;
        return Common.getOrNull(value);
    }

    private static String getTag(String raw, String key) {
        String[] parts = raw.split("%-%");
        return parts.length == 3 && parts[0].equals(SimplePlugin.getNamed()) && parts[1].equals(key) ? parts[2] : null;
    }

    public static String getMetadata(BlockState tileEntity, String key) {
        Valid.checkNotNull(tileEntity);
        Valid.checkNotNull(key);
        if (MinecraftVersion.atLeast(V.v1_14)) {
            Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity, new Object[0]);
            return getNamedspaced((TileState)tileEntity, key);
        } else {
            String value = tileEntity.hasMetadata(key) ? ((MetadataValue)tileEntity.getMetadata(key).get(0)).asString() : null;
            return Common.getOrNull(value);
        }
    }

    private static String getNamedspaced(TileState tile, String key) {
        String value = (String)tile.getPersistentDataContainer().get(new NamespacedKey(SimplePlugin.getInstance(), key), PersistentDataType.STRING);
        return Common.getOrNull(value);
    }

    public static boolean hasMetadata(ItemStack item, String key) {
        Valid.checkBoolean(MinecraftVersion.atLeast(V.v1_7), "NBT ItemStack tags only support MC 1.7.10+", new Object[0]);
        Valid.checkNotNull(item);
        if (CompMaterial.isAir(item.getType())) {
            return false;
        } else {
            NBTItem nbt = new NBTItem(item);
            NBTCompound tag = nbt.getCompound(NBT.TAG);
            return tag != null && tag.hasKey(key);
        }
    }

    public static boolean hasMetadata(Entity entity, String key) {
        Valid.checkNotNull(entity);
        if (Remain.hasScoreboardTags()) {
            Iterator var2 = entity.getScoreboardTags().iterator();

            while(var2.hasNext()) {
                String line = (String)var2.next();
                if (hasTag(line, key)) {
                    return true;
                }
            }
        }

        return entity.hasMetadata(key);
    }

    public static boolean hasMetadata(BlockState tileEntity, String key) {
        Valid.checkNotNull(tileEntity);
        Valid.checkNotNull(key);
        if (MinecraftVersion.atLeast(V.v1_14)) {
            Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity, new Object[0]);
            return hasNamedspaced((TileState)tileEntity, key);
        } else {
            return tileEntity.hasMetadata(key);
        }
    }

    private static boolean hasNamedspaced(TileState tile, String key) {
        return tile.getPersistentDataContainer().has(new NamespacedKey(SimplePlugin.getInstance(), key), PersistentDataType.STRING);
    }

    private static boolean hasTag(String raw, String tag) {
        String[] parts = raw.split("%-%");
        return parts.length == 3 && parts[0].equals(SimplePlugin.getNamed()) && parts[1].equals(tag);
    }

    public static void setTempMetadata(Entity entity, String tag) {
        entity.setMetadata(createTempMetadataKey(tag), new FixedMetadataValue(SimplePlugin.getInstance(), tag));
    }

    public static void setTempMetadata(Entity entity, String tag, Object key) {
        entity.setMetadata(createTempMetadataKey(tag), new FixedMetadataValue(SimplePlugin.getInstance(), key));
    }

    public static MetadataValue getTempMetadata(Entity entity, String tag) {
        String key = createTempMetadataKey(tag);
        return entity.hasMetadata(key) ? (MetadataValue)entity.getMetadata(key).get(0) : null;
    }

    public static boolean hasTempMetadata(Entity player, String tag) {
        return player.hasMetadata(createTempMetadataKey(tag));
    }

    public static void removeTempMetadata(Entity player, String tag) {
        String key = createTempMetadataKey(tag);
        if (player.hasMetadata(key)) {
            player.removeMetadata(key, SimplePlugin.getInstance());
        }

    }

    private static String createTempMetadataKey(String tag) {
        return SimplePlugin.getNamed() + "_" + tag;
    }

    public static final class MetadataFile extends YamlSectionConfig {
        private static volatile Object LOCK = new Object();
        private static volatile CompMetadata.MetadataFile instance = new CompMetadata.MetadataFile();
        private final StrictMap<UUID, List<String>> entityMetadataMap = new StrictMap();
        private final StrictMap<Location, CompMetadata.MetadataFile.BlockCache> blockMetadataMap = new StrictMap();

        private MetadataFile() {
            super("Metadata");
            this.loadConfiguration(NO_DEFAULT, "data.db");
        }

        protected void onLoadFinish() {
            synchronized(LOCK) {
                this.loadEntities();
                this.loadBlockStates();
                this.save();
            }
        }

        private void loadEntities() {
            synchronized(LOCK) {
                this.entityMetadataMap.clear();
                Iterator var2 = this.getMap("Entity").keySet().iterator();

                while(var2.hasNext()) {
                    String uuidName = (String)var2.next();
                    UUID uuid = UUID.fromString(uuidName);
                    if (!(this.getObject("Entity." + uuidName) instanceof List)) {
                        this.setNoSave("Entity." + uuidName, (Object)null);
                    } else {
                        List<String> metadata = this.getStringList("Entity." + uuidName);
                        Entity entity = Remain.getEntity(uuid);
                        if (!metadata.isEmpty() && entity != null && entity.isValid() && !entity.isDead()) {
                            this.entityMetadataMap.put(uuid, metadata);
                            this.applySavedMetadata(metadata, entity);
                        }
                    }
                }

                this.save("Entity", this.entityMetadataMap);
            }
        }

        private void loadBlockStates() {
            synchronized(LOCK) {
                this.blockMetadataMap.clear();
                Iterator var2 = this.getMap("Block").keySet().iterator();

                while(var2.hasNext()) {
                    String locationRaw = (String)var2.next();
                    Location location = SerializeUtil.deserializeLocation(locationRaw);
                    CompMetadata.MetadataFile.BlockCache blockCache = (CompMetadata.MetadataFile.BlockCache)this.get("Block." + locationRaw, CompMetadata.MetadataFile.BlockCache.class);
                    Block block = location.getBlock();
                    if (!CompMaterial.isAir(block) && CompMaterial.fromBlock(block) == blockCache.getType()) {
                        this.blockMetadataMap.put(location, blockCache);
                        this.applySavedMetadata(blockCache.getMetadata(), block);
                    }
                }

                this.save("Block", this.blockMetadataMap);
            }
        }

        private void applySavedMetadata(List<String> metadata, Metadatable entity) {
            synchronized(LOCK) {
                Iterator var4 = metadata.iterator();

                while(var4.hasNext()) {
                    String metadataLine = (String)var4.next();
                    if (!metadataLine.isEmpty()) {
                        String[] lines = metadataLine.split("%-%");
                        Valid.checkBoolean(lines.length == 3, "Malformed metadata line for " + entity + ". Length 3 != " + lines.length + ". Data: " + metadataLine, new Object[0]);
                        String key = lines[1];
                        String value = lines[2];
                        entity.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(), value));
                    }
                }

            }
        }

        protected void addMetadata(Entity entity, @NonNull String key, String value) {
            if (key == null) {
                throw new NullPointerException("key is marked non-null but is null");
            } else {
                synchronized(LOCK) {
                    List<String> metadata = (List)this.entityMetadataMap.getOrPut(entity.getUniqueId(), new ArrayList());
                    Iterator i = metadata.iterator();

                    while(i.hasNext()) {
                        String meta = (String)i.next();
                        if (CompMetadata.getTag(meta, key) != null) {
                            i.remove();
                        }
                    }

                    if (value != null && !value.isEmpty()) {
                        String formatted = CompMetadata.format(key, value);
                        metadata.add(formatted);
                    }

                    this.save("Entity", this.entityMetadataMap);
                }
            }
        }

        protected void addMetadata(BlockState blockState, String key, String value) {
            synchronized(LOCK) {
                CompMetadata.MetadataFile.BlockCache blockCache = (CompMetadata.MetadataFile.BlockCache)this.blockMetadataMap.getOrPut(blockState.getLocation(), new CompMetadata.MetadataFile.BlockCache(CompMaterial.fromBlock(blockState.getBlock()), new ArrayList()));
                Iterator i = blockCache.getMetadata().iterator();

                while(i.hasNext()) {
                    String meta = (String)i.next();
                    if (CompMetadata.getTag(meta, key) != null) {
                        i.remove();
                    }
                }

                if (value != null && !value.isEmpty()) {
                    String formatted = CompMetadata.format(key, value);
                    blockCache.getMetadata().add(formatted);
                }

                i = this.blockMetadataMap.entrySet().iterator();

                while(i.hasNext()) {
                    Entry<Location, CompMetadata.MetadataFile.BlockCache> entry = (Entry)i.next();
                    this.setNoSave("Block." + SerializeUtil.serializeLoc((Location)entry.getKey()), ((CompMetadata.MetadataFile.BlockCache)entry.getValue()).serialize());
                }

                this.save();
            }
        }

        public static void onReload() {
            instance = new CompMetadata.MetadataFile();
        }

        public static CompMetadata.MetadataFile getInstance() {
            return instance;
        }

        public static final class BlockCache implements ConfigSerializable {
            private final CompMaterial type;
            private final List<String> metadata;

            public static CompMetadata.MetadataFile.BlockCache deserialize(SerializedMap map) {
                CompMaterial type = map.getMaterial("Type");
                List<String> metadata = map.getStringList("Metadata");
                return new CompMetadata.MetadataFile.BlockCache(type, metadata);
            }

            public SerializedMap serialize() {
                SerializedMap map = new SerializedMap();
                map.put("Type", this.type.toString());
                map.put("Metadata", this.metadata);
                return map;
            }

            public CompMaterial getType() {
                return this.type;
            }

            public List<String> getMetadata() {
                return this.metadata;
            }

            public BlockCache(CompMaterial type, List<String> metadata) {
                this.type = type;
                this.metadata = metadata;
            }
        }
    }
}