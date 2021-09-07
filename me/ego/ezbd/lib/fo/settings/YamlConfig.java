package me.ego.ezbd.lib.fo.settings;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.ItemUtil;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.ReflectionUtil.MissingEnumException;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.collection.StrictSet;
import me.ego.ezbd.lib.fo.constants.FoConstants.Header;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.BoxedMessage;
import me.ego.ezbd.lib.fo.model.Replacer;
import me.ego.ezbd.lib.fo.model.SimpleSound;
import me.ego.ezbd.lib.fo.model.SimpleTime;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class YamlConfig {
    public static final String NO_DEFAULT = null;
    private static volatile StrictSet<ConfigInstance> loadedFiles = new StrictSet();
    private ConfigInstance instance;
    private String[] header;
    private String pathPrefix = null;
    private boolean save = false;
    private boolean useDefaults = true;
    private boolean loading = false;
    private boolean loaded = false;
    private final boolean checkAssignables = true;

    protected YamlConfig() {
    }

    public static final void clearLoadedFiles() {
        synchronized(loadedFiles) {
            loadedFiles.clear();
        }
    }

    public static final void unregisterLoadedFile(File file) {
        synchronized(loadedFiles) {
            Iterator var2 = loadedFiles.iterator();

            while(var2.hasNext()) {
                ConfigInstance instance = (ConfigInstance)var2.next();
                if (instance.equals(file)) {
                    loadedFiles.remove(instance);
                    break;
                }
            }

        }
    }

    protected static final ConfigInstance findInstance(String fileName) {
        synchronized(loadedFiles) {
            Iterator var2 = loadedFiles.iterator();

            ConfigInstance instance;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                instance = (ConfigInstance)var2.next();
            } while(!instance.equals(fileName));

            return instance;
        }
    }

    private static void addConfig(ConfigInstance instance, YamlConfig config) {
        synchronized(loadedFiles) {
            Valid.checkBoolean(!config.loaded, "Config " + config.getClass() + " for file " + instance.getFile() + " has already been loaded: " + Debugger.traceRoute(true), new Object[0]);
            loadedFiles.add(instance);
        }
    }

    protected final void loadLocalization(String localePrefix) throws Exception {
        synchronized(loadedFiles) {
            Valid.checkNotNull(localePrefix, "locale cannot be null!");

            try {
                this.loading = true;
                String localePath = "localization/messages_" + localePrefix + ".yml";
                InputStream is = FileUtil.getInternalResource(localePath);
                Valid.checkNotNull(is, SimplePlugin.getNamed() + " does not support the localization: messages_" + localePrefix + ".yml (For custom locale, set the Locale to 'en' and edit your English file instead)");
                File file = new File(SimplePlugin.getData(), localePath);
                ConfigInstance instance = findInstance(file.getName());
                if (instance == null) {
                    if (!file.exists()) {
                        FileUtil.extract(localePath);
                        if (this.saveComments()) {
                            this.save = true;
                        }
                    }

                    SimpleYaml config = FileUtil.loadConfigurationStrict(file);
                    SimpleYaml defaultsConfig = Remain.loadConfiguration(is);
                    Valid.checkBoolean(file != null && file.exists(), "Failed to load " + localePath + " from " + file, new Object[0]);
                    instance = new ConfigInstance(file, config, defaultsConfig, this.saveComments(), this.getUncommentedSections(), localePath);
                    addConfig(instance, this);
                }

                this.instance = instance;
                this.onLoadFinish();
                this.loaded = true;
            } finally {
                this.loading = false;
            }

            this.saveIfNecessary0();
        }
    }

    protected final void loadConfiguration(String file) {
        this.loadConfiguration(file, file);
    }

    public final void loadConfiguration(String from, String to) {
        synchronized(loadedFiles) {
            Valid.checkBoolean(!this.loading, "Duplicate call to loadConfiguration (already loading)", new Object[0]);
            Valid.checkNotNull(to, "File to path cannot be null!");
            Valid.checkBoolean(to.contains("."), "To path must contain file extension: " + to, new Object[0]);
            if (from != null) {
                Valid.checkBoolean(from.contains("."), "From path must contain file extension: " + from, new Object[0]);
            } else {
                this.useDefaults = false;
            }

            try {
                this.loading = true;
                ConfigInstance instance = findInstance(to);
                if (instance == null) {
                    SimpleYaml defaultsConfig = null;
                    if (!(new File(SimplePlugin.getInstance().getDataFolder(), to)).exists() && this.saveComments()) {
                        this.save = true;
                    }

                    File file;
                    if (from != null) {
                        InputStream is = FileUtil.getInternalResource(from);
                        Valid.checkNotNull(is, "Inbuilt resource not found: " + from);
                        defaultsConfig = Remain.loadConfiguration(is);
                        file = FileUtil.extract(from, to);
                    } else {
                        file = FileUtil.getOrMakeFile(to);
                    }

                    Valid.checkNotNull(file, "Failed to " + (from != null ? "copy settings from " + from + " to " : "read settings from ") + to);
                    SimpleYaml config = FileUtil.loadConfigurationStrict(file);
                    instance = new ConfigInstance(file, config, defaultsConfig, this.saveComments(), this.getUncommentedSections(), from == null ? to : from);
                    addConfig(instance, this);
                }

                this.instance = instance;

                try {
                    this.onLoadFinish();
                } catch (Exception var14) {
                    Common.throwError(var14, new String[]{"Error loading configuration in " + this.getFileName() + "!", "Problematic section: " + (String)Common.getOrDefault(this.getPathPrefix(), "''"), "Problem: " + var14 + " (see below for more)"});
                }

                this.loaded = true;
            } finally {
                this.loading = false;
            }

            this.saveIfNecessary0();
        }
    }

    private void saveIfNecessary0() {
        if (this.save || this.saveComments()) {
            this.save();
            this.save = false;
        }

    }

    protected void onLoadFinish() {
    }

    protected final SimpleYaml getConfig() {
        Valid.checkNotNull(this.instance, "Cannot call getConfig when no instance is set!");
        return this.instance.getConfig();
    }

    @Nullable
    protected final SimpleYaml getDefaults() {
        Valid.checkNotNull(this.instance, "Cannot call getDefaults when no instance is set!");
        return this.instance.getDefaultConfig();
    }

    protected final String getFileName() {
        Valid.checkNotNull(this.instance, "Instance for " + this.getClass() + " is null");
        Valid.checkNotNull(this.instance.getFile(), "Instance file in " + this.getClass() + " is null");
        return this.instance.getFile().getName();
    }

    protected final void setHeader(String... header) {
        this.header = header;
    }

    public String getName() {
        return FileUtil.getFileName(this.instance.getFile());
    }

    public final File getFile() {
        return this.instance.getFile();
    }

    public void save() {
        if (this.loading) {
            this.save = true;
        } else {
            this.onSave();
            SerializedMap map = this.serialize();
            if (map != null) {
                Iterator var2 = map.entrySet().iterator();

                while(var2.hasNext()) {
                    Entry<String, Object> entry = (Entry)var2.next();
                    this.setNoSave((String)entry.getKey(), entry.getValue());
                }
            }

            this.instance.save(this.header != null ? this.header : (this.getFileName().equals("data.db") ? Header.DATA_FILE : Header.UPDATED_FILE));
        }
    }

    protected void onSave() {
    }

    protected SerializedMap serialize() {
        return null;
    }

    public final void delete() {
        this.instance.delete();
    }

    public final void reload() {
        try {
            this.instance.reload();
            this.save = true;
            this.onLoadFinish();
            this.saveIfNecessary0();
        } catch (Exception var2) {
            Common.error(var2, new String[]{"Failed to reload " + this.getFileName()});
        }

    }

    protected boolean saveComments() {
        return false;
    }

    protected List<String> getUncommentedSections() {
        return null;
    }

    private <T> T getT(String path, Class<T> type) {
        Debugger.debug("config", new String[]{"Called get() '" + path + "' = '" + this.getConfig().get(path) + "' " + (this.getDefaults() != null ? "vs def = '" + this.getDefaults().get(path) + "'" : "no defaults") + ". Disk config contains: " + this.getConfig().getValues(true)});
        Valid.checkNotNull(path, "Path cannot be null");
        path = this.formPathPrefix(path);
        Valid.checkBoolean(!path.endsWith("."), "Path must not end with '.': " + path, new Object[0]);
        this.addDefaultIfNotExist(path, type);
        Object raw = this.getConfig().get(path);
        if (this.useDefaults && this.getDefaults() != null) {
            Valid.checkNotNull(raw, "Failed to insert value at '" + path + "' from default config");
        }

        if (raw != null) {
            if (raw.equals("[]") && type == List.class) {
                raw = new ArrayList();
            }

            if (type == Long.class && raw instanceof Integer) {
                raw = (Long)raw;
            }

            this.checkAssignable(false, path, raw, type);
        }

        return raw;
    }

    protected final <T> T get(String path, Class<T> type) {
        return this.get(path, type, (Object)null);
    }

    protected final <T> T get(String path, Class<T> type, T def) {
        Object object = this.getT(path, Object.class);
        return object != null ? SerializeUtil.deserialize(type, object) : def;
    }

    protected final <T> T getWithData(String path, Class<T> type, Object... deserializeArguments) {
        Object object = this.getT(path, Object.class);
        return object != null ? SerializeUtil.deserialize(type, object, deserializeArguments) : null;
    }

    protected final Object getObject(String path, Object def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getObject(path) : def;
    }

    protected final Object getObject(String path) {
        return this.getT(path, Object.class);
    }

    /** @deprecated */
    @Deprecated
    protected final <T> T getEnum(String path, Class<T> type) {
        return this.get(path, type);
    }

    protected final Boolean getBoolean(String path, boolean def) {
        this.forceSingleDefaults(path);
        boolean set = this.isSet(path);
        Debugger.debug("config", new String[]{"\tGetting Boolean at '" + path + "', " + (set ? "set to = " + this.getBoolean(path) : "not set, returning default " + def)});
        return this.isSet(path) ? this.getBoolean(path) : def;
    }

    protected final Boolean getBoolean(String path) {
        return (Boolean)this.getT(path, Boolean.class);
    }

    protected final String getString(String path, String def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getString(path) : def;
    }

    protected final String getString(String path) {
        Object object = this.getObject(path);
        if (object == null) {
            return null;
        } else if (object instanceof List) {
            return Common.join((List)object, "\n");
        } else if (object instanceof String[]) {
            return Common.join(Arrays.asList((String[])((String[])object)), "\n");
        } else if (!(object instanceof Boolean) && !(object instanceof Integer) && !(object instanceof Long) && !(object instanceof Double) && !(object instanceof Float)) {
            if (object instanceof String) {
                return (String)object;
            } else {
                throw new FoException("Excepted string at '" + path + "' in " + this.getFileName() + ", got (" + object.getClass() + "): " + object);
            }
        } else {
            return Objects.toString(object);
        }
    }

    protected final Long getLong(String path, Long def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getLong(path) : def;
    }

    protected final Long getLong(String path) {
        return (Long)this.getT(path, Long.class);
    }

    protected final Integer getInteger(String path, Integer def) {
        this.forceSingleDefaults(path);
        boolean set = this.isSet(path);
        Debugger.debug("config", new String[]{"\tGetting Integer at '" + path + "', " + (set ? "set to = " + this.getInteger(path) : "not set, returning default " + def)});
        return this.isSet(path) ? this.getInteger(path) : def;
    }

    protected final Integer getInteger(String path) {
        return (Integer)this.getT(path, Integer.class);
    }

    /** @deprecated */
    @Deprecated
    protected final Double getDoubleSafe(String path) {
        return this.getDouble(path);
    }

    protected final Double getDouble(String path, Double def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getDouble(path) : def;
    }

    protected final Double getDouble(String path) {
        Object raw = this.getObject(path);
        return raw != null ? Double.parseDouble(raw.toString()) : null;
    }

    protected final Replacer getReplacer(String path, String def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getReplacer(path) : Replacer.of(new String[]{def});
    }

    protected final Replacer getReplacer(String path) {
        return Replacer.of(new String[]{this.getString(path)});
    }

    protected final YamlConfig.LocationList getLocations(String path) {
        return new YamlConfig.LocationList(this, this.getList(path, Location.class));
    }

    protected final Location getLocation(String path, Location def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getLocation(path) : def;
    }

    protected final Location getLocation(String path) {
        return (Location)this.get(path, Location.class);
    }

    protected final SimpleSound getSound(String path, SimpleSound def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getSound(path) : def;
    }

    protected final SimpleSound getSound(String path) {
        return new SimpleSound(this.getString(path));
    }

    protected final YamlConfig.CasusHelper getCasus(String path, YamlConfig.CasusHelper def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getCasus(path) : def;
    }

    protected final YamlConfig.CasusHelper getCasus(String path) {
        return new YamlConfig.CasusHelper(this.getString(path));
    }

    protected final YamlConfig.TitleHelper getTitle(String path, String defTitle, String defSubtitle) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getTitle(path) : new YamlConfig.TitleHelper(defTitle, defSubtitle);
    }

    protected final YamlConfig.TitleHelper getTitle(String path) {
        return new YamlConfig.TitleHelper(path);
    }

    protected final <T extends SimpleTime> T getTime(String path, String def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getTime(path) : (def != null ? SimpleTime.from(def) : null);
    }

    protected final <T extends SimpleTime> T getTime(String path) {
        Object obj = this.getObject(path);
        return obj != null ? SimpleTime.from(obj.toString()) : null;
    }

    protected final BoxedMessage getBoxedMessage(String path, String def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getBoxedMessage(path) : new BoxedMessage(new String[]{def});
    }

    protected final BoxedMessage getBoxedMessage(String path) {
        return new BoxedMessage(new String[]{this.getString(path)});
    }

    protected final CompMaterial getMaterial(String path, CompMaterial def) {
        this.forceSingleDefaults(path);
        return this.isSet(path) ? this.getMaterial(path) : def;
    }

    protected final CompMaterial getMaterial(String path) {
        String name = this.getString(path);
        return name == null ? null : CompMaterial.fromStringStrict(name);
    }

    protected final List<Object> getList(String path) {
        List<Object> list = (List)this.getT(path, List.class);
        return (List)Common.getOrDefault(list, new ArrayList());
    }

    protected final List<SerializedMap> getMapList(String path) {
        return this.getList(path, SerializedMap.class);
    }

    /** @deprecated */
    @Deprecated
    protected final <T> Set<T> getSetSafe(String key, Class<T> type) {
        return this.getSet(key, type);
    }

    protected final <T> Set<T> getSet(String key, Class<T> type) {
        List<T> list = this.getList(key, type);
        return list == null ? new HashSet() : new HashSet(list);
    }

    protected final <T> List<T> getList(String path, Class<T> type) {
        return this.getList(path, type, (Object[])null);
    }

    protected final <T> List<T> getList(String path, Class<T> type, Object... deserializeParameters) {
        List<T> list = new ArrayList();
        List<Object> objects = this.getList(path);
        if (objects != null) {
            Iterator var6 = objects.iterator();

            while(var6.hasNext()) {
                Object object = var6.next();
                list.add(object != null ? SerializeUtil.deserialize(type, object, deserializeParameters) : null);
            }
        }

        return list;
    }

    /** @deprecated */
    @Deprecated
    protected final <T extends Enum<T>> List<T> getCompatibleEnumList(String path, Class<T> type) {
        StrictList<T> list = new StrictList();
        List<String> enumNames = this.getStringList(path);
        if (enumNames.size() == 1 && "*".equals(enumNames.get(0))) {
            return list.getSource();
        } else {
            if (enumNames != null) {
                Iterator var5 = enumNames.iterator();

                while(var5.hasNext()) {
                    String enumName = (String)var5.next();
                    Enum parsedEnum = null;

                    try {
                        parsedEnum = ReflectionUtil.lookupEnumSilent(type, enumName);
                    } catch (MissingEnumException | IllegalArgumentException var9) {
                        if (!LegacyEnum.isIncompatible(type, enumName)) {
                            throw var9;
                        }
                    }

                    if (parsedEnum != null) {
                        list.add(parsedEnum);
                    }
                }
            }

            return list.getSource();
        }
    }

    protected final String[] getStringArray(String path) {
        Object array = this.getObject(path);
        if (array == null) {
            return new String[0];
        } else if (array instanceof String) {
            return ((String)array).split("\n");
        } else if (array instanceof List) {
            return Common.join((List)array, "\n").split("\n");
        } else if (array instanceof String[]) {
            return (String[])((String[])array);
        } else {
            throw new FoException("Excepted string or string list at '" + path + "' in " + this.getFileName() + ", got (" + array.getClass() + "): " + array);
        }
    }

    protected final List<String> getStringList(String path) {
        Object raw = this.getObject(path);
        if (raw == null) {
            return new ArrayList();
        } else if (!(raw instanceof String)) {
            if (raw instanceof List) {
                return this.fixYamlBooleansInList((List)raw);
            } else {
                throw new FoException("Excepted a list at '" + path + "' in " + this.getFileName() + ", got (" + raw.getClass() + "): " + raw);
            }
        } else {
            String output = (String)raw;
            return (List)(!"'[]'".equals(output) && !"[]".equals(output) ? Arrays.asList(output) : new ArrayList());
        }
    }

    private List<String> fixYamlBooleansInList(@NonNull Iterable<Object> list) {
        if (list == null) {
            throw new NullPointerException("list is marked non-null but is null");
        } else {
            List<String> newList = new ArrayList();
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
                Object obj = var3.next();
                if (obj != null) {
                    newList.add(obj.toString());
                }
            }

            return newList;
        }
    }

    protected final StrictList<String> getCommandList(String path) {
        List<String> list = this.getStringList(path);
        Valid.checkBoolean(!list.isEmpty(), "Please set at least one command alias in '" + path + "' (" + this.getFileName() + ") for this will be used as your main command!", new Object[0]);

        for(int i = 0; i < list.size(); ++i) {
            String command = (String)list.get(i);
            command = command.startsWith("/") ? command.substring(1) : command;
            list.set(i, command);
        }

        return new StrictList(list);
    }

    protected final StrictList<CompMaterial> getMaterialList(String path) {
        StrictList<CompMaterial> list = new StrictList();
        Iterator var3 = this.getStringList(path).iterator();

        while(var3.hasNext()) {
            String raw = (String)var3.next();
            CompMaterial mat = CompMaterial.fromStringCompat(raw);
            if (mat != null) {
                list.add(mat);
            }
        }

        return list;
    }

    protected final StrictList<Enchantment> getEnchants(String path) {
        StrictList<Enchantment> list = new StrictList();
        Iterator var3 = this.getStringList(path).iterator();

        while(var3.hasNext()) {
            String name = (String)var3.next();
            list.add(ItemUtil.findEnchantment(name));
        }

        return list;
    }

    protected final SerializedMap getMap(String path) {
        LinkedHashMap<?, ?> map = this.getMap(path, Object.class, Object.class);
        return SerializedMap.of(map);
    }

    protected final <Key, Value> LinkedHashMap<Key, Value> getMap(@NonNull String path, Class<Key> keyType, Class<Value> valueType) {
        if (path == null) {
            throw new NullPointerException("path is marked non-null but is null");
        } else {
            LinkedHashMap<Key, Value> map = new LinkedHashMap();
            SimpleYaml config = this.getConfig();
            SimpleYaml defaults = this.getDefaults();
            path = this.formPathPrefix(path);
            if (defaults != null && !config.isSet(path)) {
                Valid.checkBoolean(defaults.isSet(path), "Default '" + this.getFileName() + "' lacks a map at " + path, new Object[0]);
                Iterator var7 = defaults.getConfigurationSection(path).getKeys(false).iterator();

                while(var7.hasNext()) {
                    String key = (String)var7.next();
                    this.addDefaultIfNotExist(path + "." + key, valueType);
                }
            }

            ConfigurationSection configSection = config.getConfigurationSection(path);
            if (configSection != null) {
                Iterator var13 = configSection.getValues(false).entrySet().iterator();

                while(var13.hasNext()) {
                    Entry<String, Object> entry = (Entry)var13.next();
                    Key key = SerializeUtil.deserialize(keyType, entry.getKey());
                    Value value = SerializeUtil.deserialize(valueType, entry.getValue());
                    this.checkAssignable(false, path, key, keyType);
                    this.checkAssignable(false, path, value, valueType);
                    map.put(key, value);
                }
            }

            return map;
        }
    }

    protected final <Key, Value> LinkedHashMap<Key, Set<Value>> getMapSet(@NonNull String path, Class<Key> keyType, Class<Value> setType) {
        if (path == null) {
            throw new NullPointerException("path is marked non-null but is null");
        } else {
            LinkedHashMap<Key, Set<Value>> map = new LinkedHashMap();
            SimpleYaml config = this.getConfig();
            SimpleYaml defaults = this.getDefaults();
            path = this.formPathPrefix(path);
            if (defaults != null && !config.isSet(path)) {
                Valid.checkBoolean(defaults.isSet(path), "Default '" + this.getFileName() + "' lacks a map at " + path, new Object[0]);
                Iterator var7 = defaults.getConfigurationSection(path).getKeys(false).iterator();

                while(var7.hasNext()) {
                    String key = (String)var7.next();
                    this.addDefaultIfNotExist(path + "." + key, setType);
                }
            }

            ConfigurationSection configSection = config.getConfigurationSection(path);
            Object key;
            List value;
            if (configSection != null) {
                for(Iterator var15 = configSection.getValues(false).entrySet().iterator(); var15.hasNext(); map.put(key, new HashSet(value))) {
                    Entry<String, Object> entry = (Entry)var15.next();
                    key = SerializeUtil.deserialize(keyType, entry.getKey());
                    value = (List)SerializeUtil.deserialize(List.class, entry.getValue());
                    this.checkAssignable(false, path, key, keyType);
                    if (!value.isEmpty()) {
                        Iterator var12 = value.iterator();

                        while(var12.hasNext()) {
                            Value item = var12.next();
                            this.checkAssignable(false, path, item, setType);
                        }
                    }
                }
            }

            return map;
        }
    }

    protected final void save(String path, Object value) {
        this.setNoSave(path, value);
        this.save();
    }

    protected final void setIfNotExist(String path, Object value) {
        if (!this.isSet(path)) {
            this.setNoSave(path, value);
        }

    }

    protected final void setNoSave(String path, Object value) {
        path = this.formPathPrefix(path);
        value = SerializeUtil.serialize(value);
        this.getConfig().set(path, value);
        this.save = true;
    }

    protected final void move(String fromRelative, String toAbsolute) {
        this.move(this.getObject(fromRelative), fromRelative, toAbsolute);
    }

    protected final void move(Object value, String fromPathRel, String toPathAbs) {
        String oldPathPrefix = this.pathPrefix;
        fromPathRel = this.formPathPrefix(fromPathRel);
        this.getConfig().set(fromPathRel, (Object)null);
        this.pathPrefix = oldPathPrefix;
        this.checkAndFlagForSave(toPathAbs, value, false);
        this.getConfig().set(toPathAbs, value);
        Common.log(new String[]{"&7Update " + this.getFileName() + ". Move &b'&f" + fromPathRel + "&b' &7(was '" + value + "&7') to &b'&f" + toPathAbs + "&b'&r"});
        this.pathPrefix = oldPathPrefix;
    }

    protected final <O, N> void convertMapList(String path, String mapSection, Class<O> from, Class<N> to, Function<O, N> converter) {
        List<SerializedMap> list = new ArrayList();
        Iterator var7 = this.getMapList(path).iterator();

        while(var7.hasNext()) {
            SerializedMap classMap = (SerializedMap)var7.next();
            classMap.convert(mapSection, from, to, converter);
            list.add(classMap);
        }

        this.save(path, list);
    }

    protected final <O, N> void convert(String path, Class<O> from, Class<N> to, Function<O, N> converter) {
        Object old = this.getObject(path);
        if (old != null) {
            if (old instanceof Collection) {
                Collection<?> collection = (Collection)old;
                if (collection.isEmpty() || !from.isAssignableFrom(collection.iterator().next().getClass())) {
                    return;
                }

                List<N> newCollection = new ArrayList();
                Iterator var8 = collection.iterator();

                while(var8.hasNext()) {
                    O oldItem = var8.next();
                    newCollection.add(converter.apply(oldItem));
                }

                this.save(path, newCollection);
                Common.log(new String[]{"&7Converted '" + path + "' from " + from.getSimpleName() + "[] to " + to.getSimpleName() + "[]"});
            } else if (from.isAssignableFrom(old.getClass())) {
                this.save(path, converter.apply(old));
                Common.log(new String[]{"&7Converted '" + path + "' from '" + from.getSimpleName() + "' to '" + to.getSimpleName() + "'"});
            }
        }

    }

    protected final <T> T getOrSetDefault(String path, T defaultValue) {
        if (this.isSet(path)) {
            return defaultValue instanceof Replacer ? Replacer.of(new String[]{this.getString(path)}) : this.get(path, defaultValue.getClass());
        } else {
            this.save(path, defaultValue);
            return defaultValue;
        }
    }

    protected final boolean isSet(String path) {
        return this.isSetAbsolute(this.formPathPrefix(path));
    }

    protected final boolean isSetAbsolute(String path) {
        return this.getConfig().isSet(path);
    }

    protected final boolean isSetDefault(String path) {
        return this.isSetDefaultAbsolute(this.formPathPrefix(path));
    }

    protected final boolean isSetDefaultAbsolute(String path) {
        return this.getDefaults() != null && this.getDefaults().isSet(path);
    }

    protected final void addDefaultIfNotExist(String pathAbs) {
        this.addDefaultIfNotExist(pathAbs, Object.class);
    }

    protected void addDefaultIfNotExist(String pathAbs, Class<?> type) {
        if (this.useDefaults && this.getDefaults() != null && !this.isSetAbsolute(pathAbs)) {
            Object object = this.getDefaults().get(pathAbs);
            Valid.checkNotNull(object, "Default '" + this.getFileName() + "' lacks " + Common.article(type.getSimpleName()) + " at '" + pathAbs + "'");
            this.checkAssignable(true, pathAbs, object, type);
            this.checkAndFlagForSave(pathAbs, object);
            this.getConfig().set(pathAbs, object);
        }

    }

    private void forceSingleDefaults(String path) {
        if (this.useDefaults && this.getDefaults() != null) {
            throw new FoException("Cannot use get method with default when getting " + this.formPathPrefix(path) + " and using a default config for " + this.getFileName());
        }
    }

    private <T> void checkAndFlagForSave(String path, T def) {
        this.checkAndFlagForSave(path, def, true);
    }

    private <T> void checkAndFlagForSave(String path, T def, boolean logUpdate) {
        Valid.checkBoolean(this.instance.getFile() != null && this.instance.getFile().exists() && this.instance.getConfig() != null, "Inbuilt file or config is null! File: " + this.instance.getFile() + ", config: " + this.instance.getConfig(), new Object[0]);
        if (this.getDefaults() != null) {
            Valid.checkNotNull(def, "Inbuilt config " + this.getFileName() + " lacks " + (def == null ? "key" : def.getClass().getSimpleName()) + " at \"" + path + "\". Is it outdated?");
        }

        if (logUpdate) {
            Common.log(new String[]{"&7Update " + this.getFileName() + " at &b'&f" + path + "&b' &7-> " + (def == null ? "&ckey removed" : "&b'&f" + def + "&b'") + "&r"});
        }

        this.save = true;
    }

    private void checkAssignable(boolean fromDefault, String path, Object value, Class<?> clazz) {
        if (!clazz.isAssignableFrom(value.getClass()) && !clazz.getSimpleName().equals(value.getClass().getSimpleName())) {
            throw new FoException("Malformed configuration! Key '" + path + "' in " + (fromDefault ? "inbuilt " : "") + this.getFileName() + " must be " + clazz.getSimpleName() + " but got " + value.getClass().getSimpleName() + ": '" + value + "'");
        }
    }

    protected String formPathPrefix(@NonNull String path) {
        if (path == null) {
            throw new NullPointerException("path is marked non-null but is null");
        } else {
            String prefixed = this.pathPrefix != null ? this.pathPrefix + (!path.isEmpty() ? "." + path : "") : path;
            return prefixed.endsWith(".") ? prefixed.substring(0, prefixed.length() - 1) : prefixed;
        }
    }

    protected void pathPrefix(String pathPrefix) {
        if (pathPrefix != null) {
            Valid.checkBoolean(!pathPrefix.endsWith("."), "Path prefix must not end with a dot: " + pathPrefix, new Object[0]);
            Valid.checkBoolean(!pathPrefix.endsWith(".yml"), "Path prefix must not end with .yml!", new Object[0]);
        }

        this.pathPrefix = pathPrefix != null && !pathPrefix.isEmpty() ? pathPrefix : null;
    }

    protected final String getPathPrefix() {
        return this.pathPrefix;
    }

    /** @deprecated */
    @Deprecated
    protected final LinkedHashMap<String, LinkedHashMap<String, Object>> getValuesAndKeys_OLD(String path) {
        Valid.checkNotNull(path, "Path cannot be null");
        path = this.formPathPrefix(path);
        if (this.getDefaults() != null && !this.getConfig().isSet(path)) {
            Valid.checkBoolean(this.getDefaults().isSet(path), "Default '" + this.getFileName() + "' lacks a section at " + path, new Object[0]);
            Iterator var2 = this.getDefaults().getConfigurationSection(path).getKeys(false).iterator();

            while(var2.hasNext()) {
                String name = (String)var2.next();
                Iterator var4 = this.getDefaults().getConfigurationSection(path + "." + name).getKeys(false).iterator();

                while(var4.hasNext()) {
                    String setting = (String)var4.next();
                    this.addDefaultIfNotExist(path + "." + name + "." + setting, Object.class);
                }
            }
        }

        Valid.checkBoolean(this.getConfig().isSet(path), "Malfunction copying default section to " + path, new Object[0]);
        TreeMap<String, LinkedHashMap<String, Object>> groups = new TreeMap();
        Iterator var7 = this.getConfig().getConfigurationSection(path).getKeys(false).iterator();

        while(var7.hasNext()) {
            String name = (String)var7.next();
            LinkedHashMap<String, Object> valuesRaw = this.getMap(path + "." + name, String.class, Object.class);
            groups.put(name, valuesRaw);
        }

        return new LinkedHashMap(groups);
    }

    public String toString() {
        return "YamlConfig{file=" + this.getFileName() + ", path prefix=" + this.pathPrefix + "}";
    }

    public boolean equals(Object obj) {
        throw new RuntimeException("Please implement your own equals() method for " + this.getClass());
    }

    public final class TitleHelper {
        private final String title;
        private final String subtitle;

        private TitleHelper(String path) {
            this(YamlConfig.this.getString(path + ".Title"), (String)YamlConfig.this.getString(path + ".Subtitle"));
        }

        private TitleHelper(String title, String subtitle) {
            this.title = Common.colorize(title);
            this.subtitle = Common.colorize(subtitle);
        }

        public void playLong(Player player, Function<String, String> replacer) {
            this.play(player, 5, 80, 15, replacer);
        }

        public void playShort(Player player, Function<String, String> replacer) {
            this.play(player, 3, 40, 5, replacer);
        }

        public void play(Player player, int fadeIn, int stay, int fadeOut) {
            this.play(player, fadeIn, stay, fadeOut, (Function)null);
        }

        public void play(Player player, int fadeIn, int stay, int fadeOut, @Nullable Function<String, String> replacer) {
            Remain.sendTitle(player, fadeIn, stay, fadeOut, replacer != null ? (String)replacer.apply(this.title) : this.title, replacer != null ? (String)replacer.apply(this.subtitle) : this.subtitle);
        }
    }

    public final class CasusHelper {
        private final String akuzativSg;
        private final String akuzativPl;
        private final String genitivPl;

        private CasusHelper(String raw) {
            String[] values = raw.split(", ");
            if (values.length == 2) {
                this.akuzativSg = values[0];
                this.akuzativPl = values[1];
                this.genitivPl = this.akuzativPl;
            } else if (values.length != 3) {
                throw new FoException("Malformed type, use format: 'second, seconds' OR 'sekundu, sekundy, sekund' (if your language has it)");
            } else {
                this.akuzativSg = values[0];
                this.akuzativPl = values[1];
                this.genitivPl = values[2];
            }
        }

        public String getPlural() {
            return this.genitivPl;
        }

        public String formatWithCount(long count) {
            return count + " " + this.formatWithoutCount(count);
        }

        public String formatWithoutCount(long count) {
            if (count == 1L) {
                return this.akuzativSg;
            } else {
                return count > 1L && count < 5L ? this.akuzativPl : this.genitivPl;
            }
        }
    }

    public static final class LocationList implements Iterable<Location> {
        private final YamlConfig settings;
        private final List<Location> points;

        private LocationList(YamlConfig settings, List<Location> points) {
            this.settings = settings;
            this.points = points;
        }

        public boolean toggle(Location location) {
            Iterator var2 = this.points.iterator();

            Location point;
            do {
                if (!var2.hasNext()) {
                    this.points.add(location);
                    this.settings.save();
                    return true;
                }

                point = (Location)var2.next();
            } while(!Valid.locationEquals(point, location));

            this.points.remove(point);
            this.settings.save();
            return false;
        }

        public void add(Location location) {
            Valid.checkBoolean(!this.hasLocation(location), "Location at " + location + " already exists!", new Object[0]);
            this.points.add(location);
            this.settings.save();
        }

        public void remove(Location location) {
            Location point = this.find(location);
            Valid.checkNotNull(point, "Location at " + location + " does not exist!");
            this.points.remove(point);
            this.settings.save();
        }

        public boolean hasLocation(Location location) {
            return this.find(location) != null;
        }

        public Location find(Location location) {
            Iterator var2 = this.points.iterator();

            Location entrance;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                entrance = (Location)var2.next();
            } while(!Valid.locationEquals(entrance, location));

            return entrance;
        }

        public List<Location> getLocations() {
            return Collections.unmodifiableList(this.points);
        }

        public Iterator<Location> iterator() {
            return this.points.iterator();
        }

        public int size() {
            return this.points.size();
        }
    }

    /** @deprecated */
    @Deprecated
    public static final class TimeHelper extends SimpleTime {
        protected TimeHelper(String time) {
            super(time);
        }

        /** @deprecated */
        @Deprecated
        public static YamlConfig.TimeHelper from(String time) {
            return new YamlConfig.TimeHelper(time);
        }
    }
}