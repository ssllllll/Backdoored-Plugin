package me.ego.ezbd.lib.fo.settings;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.ego.ezbd.lib.fo.Common;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;

final class ConfigInstance {
    private final File file;
    private final SimpleYaml config;
    private final SimpleYaml defaultConfig;
    private final boolean saveComments;
    private final List<String> uncommentedSections;
    private final String commentsFilePath;

    public SimpleYaml getConfig() {
        return this.config;
    }

    protected void save(String[] header) {
        if (header != null) {
            this.config.options().copyHeader(true);
            this.config.options().header(String.join("\n", header));
        }

        if (Bukkit.isPrimaryThread()) {
            this.save0();
        } else {
            Common.runLater(this::save0);
        }

    }

    private void save0() {
        try {
            if (!this.writeComments()) {
                Map values = this.config.getValues(false);

                try {
                    String data = this.config.saveToString(values);
                    Files.createParentDirs(this.file);
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.file), Charsets.UTF_8);

                    try {
                        writer.write(data);
                    } finally {
                        writer.close();
                    }
                } catch (Throwable var9) {
                    Common.error(var9, new String[0]);
                }
            }
        } catch (Exception var10) {
            Common.error(var10, new String[]{"Failed to save " + this.file.getName()});
        }

    }

    public boolean writeComments() throws IOException {
        if (this.commentsFilePath != null && this.saveComments) {
            YamlComments.writeComments(this.commentsFilePath, this.file, (List)Common.getOrDefault(this.uncommentedSections, new ArrayList()));
            return true;
        } else {
            return false;
        }
    }

    protected void reload() throws IOException, InvalidConfigurationException {
        this.config.load(this.file);
    }

    protected void delete() {
        YamlConfig.unregisterLoadedFile(this.file);
        this.file.delete();
    }

    public boolean equals(File file) {
        return this.equals((Object)file);
    }

    public boolean equals(String fileName) {
        return this.equals((Object)fileName);
    }

    public boolean equals(Object obj) {
        return obj instanceof ConfigInstance ? ((ConfigInstance)obj).file.getName().equals(this.file.getName()) : (obj instanceof File ? ((File)obj).getName().equals(this.file.getName()) : (obj instanceof String ? ((String)obj).equals(this.file.getName()) : false));
    }

    public ConfigInstance(File file, SimpleYaml config, SimpleYaml defaultConfig, boolean saveComments, List<String> uncommentedSections, String commentsFilePath) {
        this.file = file;
        this.config = config;
        this.defaultConfig = defaultConfig;
        this.saveComments = saveComments;
        this.uncommentedSections = uncommentedSections;
        this.commentsFilePath = commentsFilePath;
    }

    public File getFile() {
        return this.file;
    }

    public SimpleYaml getDefaultConfig() {
        return this.defaultConfig;
    }
}