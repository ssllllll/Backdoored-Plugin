package me.ego.ezbd.lib.fo.settings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.constants.FoConstants.Header;
import me.ego.ezbd.lib.fo.model.BoxedMessage;
import me.ego.ezbd.lib.fo.model.Replacer;
import me.ego.ezbd.lib.fo.model.SimpleSound;
import me.ego.ezbd.lib.fo.model.SimpleTime;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.settings.YamlConfig.CasusHelper;
import me.ego.ezbd.lib.fo.settings.YamlConfig.TitleHelper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

public abstract class YamlStaticConfig {
    private static YamlConfig TEMPORARY_INSTANCE;

    protected YamlStaticConfig() {
        TEMPORARY_INSTANCE = new YamlConfig() {
            {
                YamlStaticConfig.this.beforeLoad();
            }

            protected boolean saveComments() {
                return YamlStaticConfig.this.saveComments();
            }

            protected List<String> getUncommentedSections() {
                return YamlStaticConfig.this.getUncommentedSections();
            }

            protected void onLoadFinish() {
                YamlStaticConfig.this.loadViaReflection();
            }
        };
        TEMPORARY_INSTANCE.setHeader(this.getHeader());
    }

    public static final void load(List<Class<? extends YamlStaticConfig>> classes) throws Exception {
        if (classes != null) {
            for(Iterator var1 = classes.iterator(); var1.hasNext(); TEMPORARY_INSTANCE = null) {
                Class<? extends YamlStaticConfig> clazz = (Class)var1.next();
                YamlStaticConfig config = (YamlStaticConfig)clazz.newInstance();
                config.load();
            }

        }
    }

    protected String[] getHeader() {
        return Header.UPDATED_FILE;
    }

    protected boolean saveComments() {
        return false;
    }

    protected List<String> getUncommentedSections() {
        return null;
    }

    protected void beforeLoad() {
    }

    protected void preLoad() {
    }

    protected abstract void load() throws Exception;

    private final void loadViaReflection() {
        Valid.checkNotNull(TEMPORARY_INSTANCE, "Instance cannot be null " + getFileName());
        Valid.checkNotNull(TEMPORARY_INSTANCE.getConfig(), "Config cannot be null for " + getFileName());
        Valid.checkNotNull(TEMPORARY_INSTANCE.getDefaults(), "Default config cannot be null for " + getFileName());

        try {
            this.preLoad();
            if (YamlStaticConfig.class.isAssignableFrom(this.getClass().getSuperclass())) {
                Class<?> superClass = this.getClass().getSuperclass();
                this.invokeAll(superClass);
            }

            this.invokeAll(this.getClass());
        } catch (Throwable var2) {
            Throwable t = var2;
            if (var2 instanceof InvocationTargetException && var2.getCause() != null) {
                t = var2.getCause();
            }

            Remain.sneaky(t);
        }

    }

    private void invokeAll(Class<?> clazz) throws Exception {
        this.invokeMethodsIn(clazz);
        Class[] var2 = clazz.getDeclaredClasses();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Class<?> subClazz = var2[var4];
            this.invokeMethodsIn(subClazz);
            Class[] var6 = subClazz.getDeclaredClasses();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                Class<?> subSubClazz = var6[var8];
                this.invokeMethodsIn(subSubClazz);
            }
        }

    }

    private void invokeMethodsIn(Class<?> clazz) throws Exception {
        Method[] var2 = clazz.getDeclaredMethods();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Method m = var2[var4];
            if (!SimplePlugin.getInstance().isEnabled()) {
                return;
            }

            int mod = m.getModifiers();
            if (m.getName().equals("init")) {
                Valid.checkBoolean(Modifier.isPrivate(mod) && Modifier.isStatic(mod) && m.getReturnType() == Void.TYPE && m.getParameterTypes().length == 0, "Method '" + m.getName() + "' in " + clazz + " must be 'private static void init()'", new Object[0]);
                m.setAccessible(true);
                m.invoke((Object)null);
            }
        }

        this.checkFields(clazz);
    }

    private void checkFields(Class<?> clazz) throws Exception {
        Field[] var2 = clazz.getDeclaredFields();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Field f = var2[var4];
            f.setAccessible(true);
            if (Modifier.isPublic(f.getModifiers())) {
                Valid.checkBoolean(!f.getType().isPrimitive(), "Field '" + f.getName() + "' in " + clazz + " must not be primitive!", new Object[0]);
            }

            Object result = null;

            try {
                result = f.get((Object)null);
            } catch (NullPointerException var8) {
            }

            Valid.checkNotNull(result, "Null " + f.getType().getSimpleName() + " field '" + f.getName() + "' in " + clazz);
        }

    }

    protected final void createLocalizationFile(String localePrefix) throws Exception {
        TEMPORARY_INSTANCE.loadLocalization(localePrefix);
    }

    protected final void createFileAndLoad(String path) throws Exception {
        TEMPORARY_INSTANCE.loadConfiguration(path, path);
    }

    protected static final void set(String path, Object value) {
        TEMPORARY_INSTANCE.setNoSave(path, value);
    }

    protected static final boolean isSetAbsolute(String path) {
        return TEMPORARY_INSTANCE.isSetAbsolute(path);
    }

    protected static final boolean isSet(String path) {
        return TEMPORARY_INSTANCE.isSet(path);
    }

    protected static final boolean isSetDefault(String path) {
        return TEMPORARY_INSTANCE.isSetDefault(path);
    }

    protected static final boolean isSetDefaultAbsolute(String path) {
        return TEMPORARY_INSTANCE.isSetDefaultAbsolute(path);
    }

    protected static final void move(String fromRelative, String toAbsolute) {
        TEMPORARY_INSTANCE.move(fromRelative, toAbsolute);
    }

    protected static final void move(Object value, String fromPath, String toPath) {
        TEMPORARY_INSTANCE.move(value, fromPath, toPath);
    }

    protected static final String formPathPrefix(String path) {
        return TEMPORARY_INSTANCE.formPathPrefix(path);
    }

    protected static final void pathPrefix(String pathPrefix) {
        TEMPORARY_INSTANCE.pathPrefix(pathPrefix);
    }

    protected static final String getPathPrefix() {
        return TEMPORARY_INSTANCE.getPathPrefix();
    }

    protected static final void addDefaultIfNotExist(String path) {
        TEMPORARY_INSTANCE.addDefaultIfNotExist(path);
    }

    protected static final String getFileName() {
        return TEMPORARY_INSTANCE.getFileName();
    }

    protected static final FileConfiguration getConfig() {
        return TEMPORARY_INSTANCE.getConfig();
    }

    protected static final FileConfiguration getDefaults() {
        return TEMPORARY_INSTANCE.getDefaults();
    }

    protected static final StrictList<Enchantment> getEnchantments(String path) {
        return TEMPORARY_INSTANCE.getEnchants(path);
    }

    protected static final StrictList<CompMaterial> getMaterialList(String path) {
        return TEMPORARY_INSTANCE.getMaterialList(path);
    }

    protected static final StrictList<String> getCommandList(String path) {
        return TEMPORARY_INSTANCE.getCommandList(path);
    }

    protected static final List<String> getStringList(String path) {
        return TEMPORARY_INSTANCE.getStringList(path);
    }

    protected static final <E> Set<E> getSet(String path, Class<E> typeOf) {
        return TEMPORARY_INSTANCE.getSet(path, typeOf);
    }

    protected static final <E> List<E> getList(String path, Class<E> listType) {
        return TEMPORARY_INSTANCE.getList(path, listType);
    }

    protected static final <E extends Enum<E>> List<E> getCompatibleEnumList(String path, Class<E> listType) {
        return TEMPORARY_INSTANCE.getCompatibleEnumList(path, listType);
    }

    protected static final boolean getBoolean(String path) {
        return TEMPORARY_INSTANCE.getBoolean(path);
    }

    protected static final String[] getStringArray(String path) {
        return TEMPORARY_INSTANCE.getStringArray(path);
    }

    protected static final String getString(String path) {
        return TEMPORARY_INSTANCE.getString(path);
    }

    protected static final Replacer getReplacer(String path) {
        return TEMPORARY_INSTANCE.getReplacer(path);
    }

    protected static final int getInteger(String path) {
        return TEMPORARY_INSTANCE.getInteger(path);
    }

    /** @deprecated */
    @Deprecated
    protected static final double getDoubleSafe(String path) {
        return TEMPORARY_INSTANCE.getDoubleSafe(path);
    }

    protected static final double getDouble(String path) {
        return TEMPORARY_INSTANCE.getDouble(path);
    }

    protected static final SimpleSound getSound(String path) {
        return TEMPORARY_INSTANCE.getSound(path);
    }

    protected static final CasusHelper getCasus(String path) {
        return TEMPORARY_INSTANCE.getCasus(path);
    }

    protected static final TitleHelper getTitle(String path) {
        return TEMPORARY_INSTANCE.getTitle(path);
    }

    protected static final <T extends SimpleTime> T getTime(String path) {
        return TEMPORARY_INSTANCE.getTime(path);
    }

    protected static final CompMaterial getMaterial(String path) {
        return TEMPORARY_INSTANCE.getMaterial(path);
    }

    protected static final BoxedMessage getBoxedMessage(String path) {
        return TEMPORARY_INSTANCE.getBoxedMessage(path);
    }

    protected static final <E> E get(String path, Class<E> typeOf) {
        return TEMPORARY_INSTANCE.get(path, typeOf);
    }

    protected static final <E> E getWithData(String path, Class<E> typeOf, Object... deserializeArguments) {
        return TEMPORARY_INSTANCE.getWithData(path, typeOf, deserializeArguments);
    }

    protected static final Object getObject(String path) {
        return TEMPORARY_INSTANCE.getObject(path);
    }

    protected static final <T> T getOrSetDefault(String path, T defaultValue) {
        return TEMPORARY_INSTANCE.getOrSetDefault(path, defaultValue);
    }

    protected static final SerializedMap getMap(String path) {
        return TEMPORARY_INSTANCE.getMap(path);
    }

    protected static final <Key, Value> LinkedHashMap<Key, Value> getMap(String path, Class<Key> keyType, Class<Value> valueType) {
        return TEMPORARY_INSTANCE.getMap(path, keyType, valueType);
    }

    protected static LinkedHashMap<String, LinkedHashMap<String, Object>> getValuesAndKeys(String path) {
        Valid.checkNotNull(path, "Path cannot be null");
        String name;
        Iterator var3;
        String setting;
        if (getDefaults() != null && !getConfig().isSet(path)) {
            Valid.checkBoolean(getDefaults().isSet(path), "Default '" + getFileName() + "' lacks a section at " + path, new Object[0]);
            Iterator var1 = getDefaults().getConfigurationSection(path).getKeys(false).iterator();

            while(var1.hasNext()) {
                name = (String)var1.next();
                var3 = getDefaults().getConfigurationSection(path + "." + name).getKeys(false).iterator();

                while(var3.hasNext()) {
                    setting = (String)var3.next();
                    TEMPORARY_INSTANCE.addDefaultIfNotExist(path + "." + name + "." + setting, Object.class);
                }
            }
        }

        Valid.checkBoolean(getConfig().isSet(path), "Malfunction copying default section to " + path, new Object[0]);
        TreeMap<String, LinkedHashMap<String, Object>> groups = new TreeMap();
        name = TEMPORARY_INSTANCE.getPathPrefix();
        TEMPORARY_INSTANCE.pathPrefix((String)null);
        var3 = getConfig().getConfigurationSection(path).getKeys(false).iterator();

        while(var3.hasNext()) {
            setting = (String)var3.next();
            LinkedHashMap<String, Object> valuesRaw = getMap(path + "." + setting, String.class, Object.class);
            groups.put(setting, valuesRaw);
        }

        TEMPORARY_INSTANCE.pathPrefix(name);
        return new LinkedHashMap(groups);
    }
}