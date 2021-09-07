package me.ego.ezbd.lib.fo.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

public final class SimpleYaml extends FileConfiguration {
    private static final String COMMENT_PREFIX = "# ";
    private static final String BLANK_CONFIG = "{}\n";
    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml;

    public SimpleYaml() {
        if (ReflectionUtil.isClassAvailable("org.yaml.snakeyaml.LoaderOptions")) {
            Yaml yaml;
            try {
                LoaderOptions loaderOptions = new LoaderOptions();
                loaderOptions.setMaxAliasesForCollections(512);
                yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions, loaderOptions);
            } catch (NoSuchMethodError var3) {
                yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);
            }

            this.yaml = yaml;
        } else {
            this.yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);
        }

    }

    @NotNull
    public String saveToString() {
        return this.saveToString(this.getValues(false));
    }

    public String saveToString(Map<String, Object> values) {
        this.yamlOptions.setIndent(2);
        this.yamlOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
        this.yamlOptions.setWidth(4096);
        this.yamlRepresenter.setDefaultFlowStyle(FlowStyle.BLOCK);
        String header = this.buildHeader();
        String dump = this.yaml.dump(values);
        if (dump.equals("{}\n")) {
            dump = "";
        }

        return header + dump;
    }

    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map input;
        try {
            input = (Map)this.yaml.load(contents);
        } catch (YAMLException var4) {
            throw new InvalidConfigurationException(var4);
        } catch (ClassCastException var5) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        String header = this.parseHeader(contents);
        if (header.length() > 0) {
            this.options().header(header);
        }

        if (input != null) {
            this.convertMapsToSections(input, this);
        }

    }

    protected void convertMapsToSections(@NotNull Map<?, ?> input, @NotNull ConfigurationSection section) {
        Iterator var3 = input.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<?, ?> entry = (Entry)var3.next();
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof Map) {
                this.convertMapsToSections((Map)value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }

    }

    @NotNull
    protected String parseHeader(@NotNull String input) {
        String[] lines = input.split("\r?\n", -1);
        StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        for(int i = 0; i < lines.length && readingHeader; ++i) {
            String line = lines[i];
            if (line.startsWith("# ")) {
                if (i > 0) {
                    result.append("\n");
                }

                if (line.length() > "# ".length()) {
                    result.append(line.substring("# ".length()));
                }

                foundHeader = true;
            } else if (foundHeader && line.length() == 0) {
                result.append("\n");
            } else if (foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    @NotNull
    protected String buildHeader() {
        String header = this.options().header();
        if (this.options().copyHeader()) {
            Configuration def = this.getDefaults();
            if (def != null && def instanceof FileConfiguration) {
                FileConfiguration filedefaults = (FileConfiguration)def;
                String defaultsHeader = (String)ReflectionUtil.invoke("buildHeader", filedefaults, new Object[0]);
                if (defaultsHeader != null && defaultsHeader.length() > 0) {
                    return defaultsHeader;
                }
            }
        }

        if (header == null) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            String[] lines = header.split("\r?\n", -1);
            boolean startedHeader = false;

            for(int i = lines.length - 1; i >= 0; --i) {
                builder.insert(0, "\n");
                if (startedHeader || lines[i].length() != 0) {
                    builder.insert(0, lines[i]);
                    builder.insert(0, "# ");
                    startedHeader = true;
                }
            }

            return builder.toString();
        }
    }

    @NotNull
    public static SimpleYaml loadConfiguration(@NotNull File file) {
        Validate.notNull(file, "File cannot be null");
        SimpleYaml config = new SimpleYaml();

        try {
            config.load(file);
        } catch (FileNotFoundException var3) {
        } catch (IOException var4) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, var4);
        } catch (InvalidConfigurationException var5) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, var5);
        }

        return config;
    }

    @NotNull
    public static SimpleYaml loadConfiguration(@NotNull Reader reader) {
        Validate.notNull(reader, "Stream cannot be null");
        SimpleYaml config = new SimpleYaml();

        try {
            config.load(reader);
        } catch (IOException var3) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", var3);
        } catch (InvalidConfigurationException var4) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", var4);
        }

        return config;
    }
}