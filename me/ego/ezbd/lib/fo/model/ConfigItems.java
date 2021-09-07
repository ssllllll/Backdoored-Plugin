package me.ego.ezbd.lib.fo.model;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.settings.YamlConfig;
import org.apache.commons.lang.WordUtils;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigItems<T extends YamlConfig> {
    private volatile List<T> loadedItems = new ArrayList();
    private final String type;
    private final String folder;
    private final Class<T> prototypeClass;
    private boolean singleFile = false;

    private ConfigItems(String type, String folder, Class<T> prototypeClass, boolean singleFile) {
        this.type = type;
        this.folder = folder;
        this.prototypeClass = prototypeClass;
        this.singleFile = singleFile;
    }

    public static <P extends YamlConfig> ConfigItems<P> fromFolder(String name, String folder, Class<P> prototypeClass) {
        return new ConfigItems(name, folder, prototypeClass, false);
    }

    public static <P extends YamlConfig> ConfigItems<P> fromFile(String path, String file, Class<P> prototypeClass) {
        return new ConfigItems(path, file, prototypeClass, true);
    }

    public void loadItems() {
        this.loadedItems.clear();
        if (this.singleFile) {
            File file = FileUtil.extract(this.folder);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Valid.checkBoolean(config.isSet(this.type), "Unable to locate configuration section " + this.type + " in " + file, new Object[0]);
            Iterator var3 = config.getConfigurationSection(this.type).getKeys(false).iterator();

            while(var3.hasNext()) {
                String name = (String)var3.next();
                this.loadOrCreateItem(name);
            }
        } else {
            if (!FileUtil.getFile(this.folder).exists()) {
                FileUtil.extractFolderFromJar(this.folder + "/", this.folder);
            }

            File[] files = FileUtil.getFiles(this.folder, "yml");
            File[] var8 = files;
            int var9 = files.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                File file = var8[var10];
                String name = FileUtil.getFileName(file);
                this.loadOrCreateItem(name);
            }
        }

    }

    private void loadOrCreateItem(String name) {
        try {
            boolean nameConstructor = true;

            Constructor constructor;
            try {
                constructor = this.prototypeClass.getDeclaredConstructor(String.class);
            } catch (Exception var5) {
                constructor = this.prototypeClass.getDeclaredConstructor();
                nameConstructor = false;
            }

            Valid.checkBoolean(Modifier.isPrivate(constructor.getModifiers()), "Your class " + this.prototypeClass + " must have private constructor taking a String or nothing!", new Object[0]);
            constructor.setAccessible(true);
            YamlConfig item;
            if (nameConstructor) {
                item = (YamlConfig)constructor.newInstance(name);
            } else {
                item = (YamlConfig)constructor.newInstance();
            }

            this.loadedItems.add(item);
        } catch (Throwable var6) {
            Common.throwError(var6, new String[]{"Failed to load" + (this.type == null ? "" : " " + this.type) + " " + name + " from " + this.folder});
        }

    }

    public void removeItem(@NonNull T item) {
        if (item == null) {
            throw new NullPointerException("item is marked non-null but is null");
        } else {
            Valid.checkBoolean(this.isItemLoaded(item.getName()), WordUtils.capitalize(this.type) + " " + item.getName() + " not loaded. Available: " + this.getItemNames(), new Object[0]);
            item.delete();
            this.loadedItems.remove(item);
        }
    }

    public boolean isItemLoaded(String name) {
        return this.findItem(name) != null;
    }

    public T findItem(@NonNull String name) {
        if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
        } else {
            Iterator var2 = this.loadedItems.iterator();

            YamlConfig item;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                item = (YamlConfig)var2.next();
            } while(!item.getName().equalsIgnoreCase(name));

            return item;
        }
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(this.loadedItems);
    }

    public List<String> getItemNames() {
        return Common.convert(this.loadedItems, YamlConfig::getName);
    }
}