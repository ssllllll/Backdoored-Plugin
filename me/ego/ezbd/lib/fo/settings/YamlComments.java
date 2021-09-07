package me.ego.ezbd.lib.fo.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public final class YamlComments {
    public YamlComments() {
    }

    public static void writeComments(@NonNull String jarPath, @NonNull File diskFile) {
        if (jarPath == null) {
            throw new NullPointerException("jarPath is marked non-null but is null");
        } else if (diskFile == null) {
            throw new NullPointerException("diskFile is marked non-null but is null");
        } else {
            try {
                writeComments(jarPath, diskFile, new ArrayList());
            } catch (IOException var3) {
                Common.error(var3, new String[]{"Failed writing comments!", "Path in plugin jar wherefrom comments are fetched: " + jarPath, "Disk file where comments are written: " + diskFile});
            }

        }
    }

    public static void writeComments(@NonNull String jarPath, @NonNull File diskFile, @NonNull List<String> ignoredSections) throws IOException {
        if (jarPath == null) {
            throw new NullPointerException("jarPath is marked non-null but is null");
        } else if (diskFile == null) {
            throw new NullPointerException("diskFile is marked non-null but is null");
        } else if (ignoredSections == null) {
            throw new NullPointerException("ignoredSections is marked non-null but is null");
        } else {
            InputStream internalResource = FileUtil.getInternalResource(jarPath);
            Valid.checkNotNull(internalResource, "Failed getting internal resource: " + jarPath);
            BufferedReader newReader = new BufferedReader(new InputStreamReader(internalResource, StandardCharsets.UTF_8));
            List<String> newLines = (List)newReader.lines().collect(Collectors.toList());
            newReader.close();
            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(diskFile);
            FileConfiguration newConfig = Remain.loadConfiguration(FileUtil.getInternalResource(jarPath));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(diskFile), StandardCharsets.UTF_8));
            Iterator var9 = ignoredSections.iterator();

            while(var9.hasNext()) {
                String ignoredSection = (String)var9.next();
                if (newConfig.isSet(ignoredSection)) {
                    Valid.checkBoolean(newConfig.isConfigurationSection(ignoredSection), "Can only ignore config sections in " + jarPath + " (file " + diskFile + ") not '" + ignoredSection + "' that is " + newConfig.get(ignoredSection), new Object[0]);
                }
            }

            Set<String> newKeys = newConfig.getKeys(true);
            Map<String, Object> removedKeys = new HashMap();
            Iterator var11 = oldConfig.getValues(true).entrySet().iterator();

            while(true) {
                label55:
                while(var11.hasNext()) {
                    Entry<String, Object> oldEntry = (Entry)var11.next();
                    String oldKey = (String)oldEntry.getKey();
                    Iterator var14 = ignoredSections.iterator();

                    while(var14.hasNext()) {
                        String ignoredKey = (String)var14.next();
                        if (oldKey.startsWith(ignoredKey)) {
                            continue label55;
                        }
                    }

                    if (!newKeys.contains(oldKey)) {
                        removedKeys.put(oldKey, oldEntry.getValue());
                    }
                }

                if (!removedKeys.isEmpty()) {
                    File backupFile = FileUtil.getOrMakeFile("unused/" + diskFile.getName());
                    FileConfiguration backupConfig = YamlConfiguration.loadConfiguration(backupFile);
                    Iterator var22 = removedKeys.entrySet().iterator();

                    while(var22.hasNext()) {
                        Entry<String, Object> entry = (Entry)var22.next();
                        backupConfig.set((String)entry.getKey(), entry.getValue());
                    }

                    backupConfig.save(backupFile);
                    Common.log(new String[]{"&cWarning: &fThe following entries in " + diskFile.getName() + " are unused and were moved into " + backupFile.getName() + ": " + removedKeys.keySet()});
                }

                DumperOptions dumperOptions = new DumperOptions();
                dumperOptions.setWidth(4096);
                Yaml yaml = new Yaml(dumperOptions);
                Map<String, String> comments = parseComments(newLines, ignoredSections, oldConfig, yaml);
                write(newConfig, oldConfig, comments, ignoredSections, writer, yaml);
                return;
            }
        }
    }

    private static void write(FileConfiguration newConfig, FileConfiguration oldConfig, Map<String, String> comments, List<String> ignoredSections, BufferedWriter writer, Yaml yaml) throws IOException {
        Set<String> copyAllowed = new HashSet();
        Set<String> reverseCopy = new HashSet();
        Iterator var8 = newConfig.getKeys(true).iterator();

        while(true) {
            label63:
            while(var8.hasNext()) {
                String key = (String)var8.next();
                Iterator var10 = copyAllowed.iterator();

                String ignoredSection;
                label60:
                do {
                    if (!var10.hasNext()) {
                        var10 = reverseCopy.iterator();

                        while(var10.hasNext()) {
                            ignoredSection = (String)var10.next();
                            if (key.startsWith(ignoredSection)) {
                                continue label63;
                            }
                        }

                        var10 = ignoredSections.iterator();

                        while(true) {
                            if (!var10.hasNext()) {
                                break label60;
                            }

                            ignoredSection = (String)var10.next();
                            if (key.equals(ignoredSection)) {
                                if (oldConfig.isSet(ignoredSection) && !oldConfig.getConfigurationSection(ignoredSection).getKeys(false).isEmpty()) {
                                    write0(key, true, newConfig, oldConfig, comments, ignoredSections, writer, yaml);
                                    Iterator var12 = oldConfig.getConfigurationSection(ignoredSection).getKeys(true).iterator();

                                    while(var12.hasNext()) {
                                        String oldKey = (String)var12.next();
                                        write0(ignoredSection + "." + oldKey, true, oldConfig, newConfig, comments, ignoredSections, writer, yaml);
                                    }

                                    reverseCopy.add(ignoredSection);
                                    continue label63;
                                }

                                copyAllowed.add(ignoredSection);
                                break label60;
                            }

                            if (key.startsWith(ignoredSection)) {
                                continue label63;
                            }
                        }
                    }

                    ignoredSection = (String)var10.next();
                } while(!key.startsWith(ignoredSection));

                write0(key, false, newConfig, oldConfig, comments, ignoredSections, writer, yaml);
            }

            String danglingComments = (String)comments.get((Object)null);
            if (danglingComments != null) {
                writer.write(danglingComments);
            }

            writer.close();
            return;
        }
    }

    private static void write0(String key, boolean forceNew, FileConfiguration newConfig, FileConfiguration oldConfig, Map<String, String> comments, List<String> ignoredSections, BufferedWriter writer, Yaml yaml) throws IOException {
        String[] keys = key.split("\\.");
        String actualKey = keys[keys.length - 1];
        String comment = (String)comments.remove(key);
        StringBuilder prefixBuilder = new StringBuilder();
        int indents = keys.length - 1;
        appendPrefixSpaces(prefixBuilder, indents);
        String prefixSpaces = prefixBuilder.toString();
        if (comment != null) {
            writer.write(comment);
        }

        Object newObj = newConfig.get(key);
        Object oldObj = oldConfig.get(key);
        if (newObj instanceof ConfigurationSection && !forceNew && oldObj instanceof ConfigurationSection) {
            writeSection(writer, actualKey, prefixSpaces, (ConfigurationSection)oldObj);
        } else if (newObj instanceof ConfigurationSection) {
            writeSection(writer, actualKey, prefixSpaces, (ConfigurationSection)newObj);
        } else if (oldObj != null && !forceNew) {
            write(oldObj, actualKey, prefixSpaces, yaml, writer);
        } else {
            write(newObj, actualKey, prefixSpaces, yaml, writer);
        }

    }

    private static void write(Object obj, String actualKey, String prefixSpaces, Yaml yaml, BufferedWriter writer) throws IOException {
        if (obj instanceof ConfigurationSerializable) {
            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(((ConfigurationSerializable)obj).serialize()));
        } else if (!(obj instanceof String) && !(obj instanceof Character)) {
            if (obj instanceof List) {
                writeList((List)obj, actualKey, prefixSpaces, yaml, writer);
            } else {
                writer.write(prefixSpaces + actualKey + ": " + yaml.dump(obj));
            }
        } else {
            if (obj instanceof String) {
                String string = (String)obj;
                if (string.contains("\n")) {
                    writer.write(prefixSpaces + actualKey + ": |-\n");
                    String[] var6 = string.split("\n");
                    int var7 = var6.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        String line = var6[var8];
                        writer.write(prefixSpaces + "    " + line + "\n");
                    }

                    return;
                }
            }

            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(obj));
        }

    }

    private static void writeSection(BufferedWriter writer, String actualKey, String prefixSpaces, ConfigurationSection section) throws IOException {
        if (section.getKeys(false).isEmpty()) {
            writer.write(prefixSpaces + actualKey + ":");
        } else {
            writer.write(prefixSpaces + actualKey + ":");
        }

        writer.write("\n");
    }

    private static void writeList(List<?> list, String actualKey, String prefixSpaces, Yaml yaml, BufferedWriter writer) throws IOException {
        writer.write(getListAsString(list, actualKey, prefixSpaces, yaml));
    }

    private static String getListAsString(List<?> list, String actualKey, String prefixSpaces, Yaml yaml) {
        StringBuilder builder = (new StringBuilder(prefixSpaces)).append(actualKey).append(":");
        if (list.isEmpty()) {
            builder.append(" []\n");
            return builder.toString();
        } else {
            builder.append("\n");

            for(int i = 0; i < list.size(); ++i) {
                Object o = list.get(i);
                if (!(o instanceof String) && !(o instanceof Character)) {
                    if (o instanceof List) {
                        builder.append(prefixSpaces).append("- ").append(yaml.dump(o));
                    } else {
                        builder.append(prefixSpaces).append("- ").append(o);
                    }
                } else {
                    builder.append(prefixSpaces).append("- '").append(o.toString().replace("'", "''")).append("'");
                }

                if (i != list.size()) {
                    builder.append("\n");
                }
            }

            return builder.toString();
        }
    }

    private static Map<String, String> parseComments(List<String> lines, List<String> ignoredSections, FileConfiguration oldConfig, Yaml yaml) {
        Map<String, String> comments = new HashMap();
        StringBuilder builder = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        int lastLineIndentCount = 0;
        Iterator var8 = lines.iterator();

        while(true) {
            while(true) {
                String line;
                do {
                    if (!var8.hasNext()) {
                        if (builder.length() > 0) {
                            comments.put((Object)null, builder.toString());
                        }

                        return comments;
                    }

                    line = (String)var8.next();
                } while(line != null && line.trim().startsWith("-"));

                if (line != null && !line.trim().equals("") && !line.trim().startsWith("#")) {
                    lastLineIndentCount = setFullKey(keyBuilder, line, lastLineIndentCount);
                    if (keyBuilder.length() > 0) {
                        comments.put(keyBuilder.toString(), builder.toString());
                        builder.setLength(0);
                    }
                } else {
                    builder.append(line).append("\n");
                }
            }
        }
    }

    private static int countIndents(String s) {
        int spaces = 0;
        char[] var2 = s.toCharArray();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            char c = var2[var4];
            if (c != ' ') {
                break;
            }

            ++spaces;
        }

        return spaces / 2;
    }

    private static void removeLastKey(StringBuilder keyBuilder) {
        String temp = keyBuilder.toString();
        String[] keys = temp.split("\\.");
        if (keys.length == 1) {
            keyBuilder.setLength(0);
        } else {
            temp = temp.substring(0, temp.length() - keys[keys.length - 1].length() - 1);
            keyBuilder.setLength(temp.length());
        }
    }

    private static int setFullKey(StringBuilder keyBuilder, String configLine, int lastLineIndentCount) {
        int currentIndents = countIndents(configLine);
        String key = configLine.trim().split(":")[0];
        if (keyBuilder.length() == 0) {
            keyBuilder.append(key);
        } else if (currentIndents == lastLineIndentCount) {
            removeLastKey(keyBuilder);
            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }

            keyBuilder.append(key);
        } else if (currentIndents > lastLineIndentCount) {
            keyBuilder.append(".").append(key);
        } else {
            int difference = lastLineIndentCount - currentIndents;

            for(int i = 0; i < difference + 1; ++i) {
                removeLastKey(keyBuilder);
            }

            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }

            keyBuilder.append(key);
        }

        return currentIndents;
    }

    private static String getPrefixSpaces(int indents) {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < indents; ++i) {
            builder.append("  ");
        }

        return builder.toString();
    }

    private static void appendPrefixSpaces(StringBuilder builder, int indents) {
        builder.append(getPrefixSpaces(indents));
    }
}