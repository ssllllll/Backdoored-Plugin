package me.ego.ezbd.lib.fo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.Tuple;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.settings.SimpleYaml;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;

public final class FileUtil {
    public static String getFileName(File file) {
        return getFileName(file.getName());
    }

    public static String getFileName(String path) {
        Valid.checkBoolean(path != null && !path.isEmpty(), "The given path must not be empty!", new Object[0]);
        int pos = path.lastIndexOf("/");
        if (pos > 0) {
            path = path.substring(pos + 1, path.length());
        }

        pos = path.lastIndexOf(".");
        if (pos > 0) {
            path = path.substring(0, pos);
        }

        return path;
    }

    public static File getOrMakeFile(String path) {
        File file = getFile(path);
        return file.exists() ? file : createIfNotExists(path);
    }

    public static File createIfNotExists(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Throwable var2) {
                Common.throwError(var2, new String[]{"Could not create new " + file + " due to " + var2});
            }
        }

        return file;
    }

    public static File createIfNotExists(String path) {
        File datafolder = SimplePlugin.getInstance().getDataFolder();
        int lastIndex = path.lastIndexOf(47);
        File directory = new File(datafolder, path.substring(0, lastIndex >= 0 ? lastIndex : 0));
        directory.mkdirs();
        File destination = new File(datafolder, path);

        try {
            destination.createNewFile();
        } catch (IOException var6) {
            Bukkit.getLogger().severe("Failed to create a new file " + path);
            var6.printStackTrace();
        }

        return destination;
    }

    public static File getFile(String path) {
        return new File(SimplePlugin.getInstance().getDataFolder(), path);
    }

    public static File[] getFiles(@NonNull String directory, @NonNull String extension) {
        if (directory == null) {
            throw new NullPointerException("directory is marked non-null but is null");
        } else if (extension == null) {
            throw new NullPointerException("extension is marked non-null but is null");
        } else {
            if (extension.startsWith(".")) {
                extension = extension.substring(1);
            }

            File dataFolder = new File(SimplePlugin.getData(), directory);
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            return dataFolder.listFiles((file) -> {
                return !file.isDirectory() && file.getName().endsWith("." + extension);
            });
        }
    }

    public static List<String> readLines(File file) {
        Valid.checkNotNull(file, "File cannot be null");
        Valid.checkBoolean(file.exists(), "File: " + file + " does not exists!", new Object[0]);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            Throwable var2 = null;

            try {
                ArrayList lines = new ArrayList();

                String line;
                while((line = br.readLine()) != null) {
                    lines.add(line);
                }

                ArrayList var5 = lines;
                return var5;
            } catch (Throwable var15) {
                var2 = var15;
                throw var15;
            } finally {
                if (br != null) {
                    if (var2 != null) {
                        try {
                            br.close();
                        } catch (Throwable var14) {
                            var2.addSuppressed(var14);
                        }
                    } else {
                        br.close();
                    }
                }

            }
        } catch (IOException var17) {
            throw new FoException(var17, "Could not read lines from " + file.getName());
        }
    }

    public static SimpleYaml loadInternalConfiguration(String internalFileName) {
        InputStream is = getInternalResource(internalFileName);
        Valid.checkNotNull(is, "Failed getting internal configuration from " + internalFileName);
        return Remain.loadConfiguration(is);
    }

    public static SimpleYaml loadConfigurationStrict(File file) throws RuntimeException {
        Valid.checkNotNull(file, "File is null!");
        Valid.checkBoolean(file.exists(), "File " + file.getName() + " does not exists", new Object[0]);
        SimpleYaml conf = new SimpleYaml();

        try {
            if (file.exists()) {
                checkFileForKnownErrors(file);
            }

            conf.load(file);
            return conf;
        } catch (FileNotFoundException var3) {
            throw new IllegalArgumentException("Configuration file missing: " + file.getName(), var3);
        } catch (IOException var4) {
            throw new IllegalArgumentException("IO exception opening " + file.getName(), var4);
        } catch (InvalidConfigurationException var5) {
            throw new IllegalArgumentException("Malformed YAML file " + file.getName() + " - use services like yaml-online-parser.appspot.com to check and fix it", var5);
        } catch (Throwable var6) {
            throw new IllegalArgumentException("Error reading YAML file " + file.getName(), var6);
        }
    }

    private static void checkFileForKnownErrors(File file) throws IllegalArgumentException {
        Iterator var1 = readLines(file).iterator();

        String line;
        do {
            if (!var1.hasNext()) {
                return;
            }

            line = (String)var1.next();
        } while(!line.contains("[*]"));

        throw new IllegalArgumentException("Found [*] in your .yml file " + file + ". Please replace it with ['*'] instead.");
    }

    public static void writeFormatted(String to, String message) {
        writeFormatted(to, (String)null, message);
    }

    public static void writeFormatted(String to, String prefix, String message) {
        message = Common.stripColors(message).trim();
        if (!message.equalsIgnoreCase("none") && !message.isEmpty()) {
            String[] var3 = Common.splitNewline(message);
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String line = var3[var5];
                if (!line.isEmpty()) {
                    write(to, "[" + TimeUtil.getFormattedDate() + "] " + (prefix != null ? prefix + ": " : "") + line);
                }
            }
        }

    }

    public static void write(String to, String... lines) {
        write((String)to, (Collection)Arrays.asList(lines));
    }

    public static void write(File to, String... lines) {
        write(createIfNotExists(to), Arrays.asList(lines), StandardOpenOption.APPEND);
    }

    public static void write(String to, Collection<String> lines) {
        write(getOrMakeFile(to), lines, StandardOpenOption.APPEND);
    }

    public static void write(File to, Collection<String> lines, StandardOpenOption... options) {
        try {
            Path path = Paths.get(to.toURI());

            try {
                if (!to.exists()) {
                    createIfNotExists(to);
                }

                Files.write(path, lines, StandardCharsets.UTF_8, options);
            } catch (ClosedByInterruptException var22) {
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(to, true));
                    Throwable var6 = null;

                    try {
                        Iterator var7 = lines.iterator();

                        while(var7.hasNext()) {
                            String line = (String)var7.next();
                            bw.append(System.lineSeparator() + line);
                        }
                    } catch (Throwable var19) {
                        var6 = var19;
                        throw var19;
                    } finally {
                        if (bw != null) {
                            if (var6 != null) {
                                try {
                                    bw.close();
                                } catch (Throwable var18) {
                                    var6.addSuppressed(var18);
                                }
                            } else {
                                bw.close();
                            }
                        }

                    }
                } catch (IOException var21) {
                    var21.printStackTrace();
                }
            }
        } catch (Exception var23) {
            Bukkit.getLogger().severe("Failed to write to " + to);
            var23.printStackTrace();
        }

    }

    public static File extract(String path) {
        return extract(path, path);
    }

    public static File extract(String from, String to) {
        File file = new File(SimplePlugin.getInstance().getDataFolder(), to);
        InputStream is = getInternalResource("/" + from);
        Valid.checkNotNull(is, "Inbuilt file not found: " + from);
        if (file.exists()) {
            return file;
        } else {
            file = createIfNotExists(to);

            try {
                List<String> lines = new ArrayList();
                String fileName = getFileName(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                Throwable var7 = null;

                try {
                    String line;
                    try {
                        while((line = br.readLine()) != null) {
                            lines.add(replaceVariables(line, fileName));
                        }
                    } catch (Throwable var17) {
                        var7 = var17;
                        throw var17;
                    }
                } finally {
                    if (br != null) {
                        if (var7 != null) {
                            try {
                                br.close();
                            } catch (Throwable var16) {
                                var7.addSuppressed(var16);
                            }
                        } else {
                            br.close();
                        }
                    }

                }

                Files.write(file.toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException var19) {
                Common.error(var19, new String[]{"Failed to extract " + from + " to " + to, "Error: %error"});
            }

            return file;
        }
    }

    public static File extractRaw(String path) {
        File file = new File(SimplePlugin.getInstance().getDataFolder(), path);
        InputStream is = getInternalResource("/" + path);
        Valid.checkNotNull(is, "Inbuilt file not found: " + path);
        if (file.exists()) {
            return file;
        } else {
            file = createIfNotExists(path);

            try {
                Files.copy(is, file.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            } catch (IOException var4) {
                Common.error(var4, new String[]{"Failed to extract " + path, "Error: %error"});
            }

            return file;
        }
    }

    private static String replaceVariables(String line, String fileName) {
        return line.replace("{plugin_name}", SimplePlugin.getNamed().toLowerCase()).replace("{file}", fileName).replace("{file_lowercase}", fileName);
    }

    public static void extractFolderFromJar(String folder, String destination) {
        Valid.checkBoolean(folder.endsWith("/"), "Folder must end with '/'! Given: " + folder, new Object[0]);
        Valid.checkBoolean(!folder.startsWith("/"), "Folder must not start with '/'! Given: " + folder, new Object[0]);
        if (!getFile(folder).exists()) {
            try {
                JarFile jarFile = new JarFile(SimplePlugin.getSource());
                Throwable var3 = null;

                try {
                    Enumeration it = jarFile.entries();

                    while(it.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry)it.nextElement();
                        String entryName = jarEntry.getName();
                        if (entryName.startsWith(folder) && !entryName.equals(folder)) {
                            extract(entryName);
                        }
                    }
                } catch (Throwable var15) {
                    var3 = var15;
                    throw var15;
                } finally {
                    if (jarFile != null) {
                        if (var3 != null) {
                            try {
                                jarFile.close();
                            } catch (Throwable var14) {
                                var3.addSuppressed(var14);
                            }
                        } else {
                            jarFile.close();
                        }
                    }

                }
            } catch (Throwable var17) {
                Common.throwError(var17, new String[]{"Failed to copy folder " + folder + " to " + destination});
            }

        }
    }

    public static InputStream getInternalResource(@NonNull String path) {
        if (path == null) {
            throw new NullPointerException("path is marked non-null but is null");
        } else {
            InputStream is = SimplePlugin.getInstance().getClass().getResourceAsStream(path);
            if (is == null) {
                is = SimplePlugin.getInstance().getResource(path);
            }

            if (is == null) {
                try {
                    JarFile jarFile = new JarFile(SimplePlugin.getSource());
                    Throwable var3 = null;

                    try {
                        JarEntry jarEntry = jarFile.getJarEntry(path);
                        if (jarEntry != null) {
                            is = jarFile.getInputStream(jarEntry);
                        }
                    } catch (Throwable var13) {
                        var3 = var13;
                        throw var13;
                    } finally {
                        if (jarFile != null) {
                            if (var3 != null) {
                                try {
                                    jarFile.close();
                                } catch (Throwable var12) {
                                    var3.addSuppressed(var12);
                                }
                            } else {
                                jarFile.close();
                            }
                        }

                    }
                } catch (IOException var15) {
                    var15.printStackTrace();
                }
            }

            return is;
        }
    }

    public static void deleteRecursivelly(File file) {
        if (file.isDirectory()) {
            File[] var1 = file.listFiles();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                File subfolder = var1[var3];
                deleteRecursivelly(subfolder);
            }
        }

        if (file.exists()) {
            Valid.checkBoolean(file.delete(), "Failed to delete file: " + file, new Object[0]);
        }

    }

    public static Tuple<String, byte[]> compress(@NonNull File file) {
        if (file == null) {
            throw new NullPointerException("file is marked non-null but is null");
        } else {
            Valid.checkBoolean(!file.isDirectory(), "Cannot compress a directory: " + file.getPath(), new Object[0]);
            List<String> lines = new ArrayList(readLines(file));
            List<File> parentDirs = new ArrayList();

            for(File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
                parentDirs.add(parent);
            }

            Collections.reverse(parentDirs);
            String filePath = Common.join(parentDirs, "/", File::getName) + "/" + file.getName();
            lines.add(filePath);
            String joinedLines = String.join("%CMPRSDBF%", lines);
            return new Tuple(filePath, CompressUtil.compress(joinedLines));
        }
    }

    public static File decompressAndWrite(@NonNull byte[] data) {
        if (data == null) {
            throw new NullPointerException("data is marked non-null but is null");
        } else {
            return decompressAndWrite((File)null, data);
        }
    }

    public static File decompressAndWrite(@Nullable File destination, @NonNull byte[] data) {
        if (data == null) {
            throw new NullPointerException("data is marked non-null but is null");
        } else {
            Tuple<File, List<String>> tuple = decompress(data);
            if (destination == null) {
                destination = (File)tuple.getKey();
            }

            List lines = (List)tuple.getValue();

            try {
                createIfNotExists(destination);
                Files.write(destination.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException var5) {
                Common.throwError(var5, new String[]{"Failed to write " + lines.size() + " lines into " + destination});
            }

            return destination;
        }
    }

    public static Tuple<File, List<String>> decompress(@NonNull byte[] data) {
        if (data == null) {
            throw new NullPointerException("data is marked non-null but is null");
        } else {
            String decompressed = CompressUtil.decompress(data);
            String[] linesRaw = decompressed.split("%CMPRSDBF%");
            Valid.checkBoolean(linesRaw.length > 0, "Received empty lines to decompress into a file!", new Object[0]);
            List<String> lines = new ArrayList();

            for(int i = 0; i < linesRaw.length - 1; ++i) {
                lines.add(linesRaw[i]);
            }

            File file = new File(linesRaw[linesRaw.length - 1]);
            return new Tuple(file, lines);
        }
    }

    public static void zip(String sourceDirectory, String to) throws IOException {
        File parent = SimplePlugin.getInstance().getDataFolder().getParentFile().getParentFile();
        File toFile = new File(parent, to + ".zip");
        if (toFile.exists()) {
            Valid.checkBoolean(toFile.delete(), "Failed to delete old file " + toFile, new Object[0]);
        }

        Path pathTo = Files.createFile(Paths.get(toFile.toURI()));
        ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(pathTo));
        Throwable var6 = null;

        try {
            Path pathFrom = Paths.get((new File(parent, sourceDirectory)).toURI());
            Files.walk(pathFrom).filter((path) -> {
                return !Files.isDirectory(path, new LinkOption[0]);
            }).forEach((path) -> {
                ZipEntry zipEntry = new ZipEntry(pathFrom.relativize(path).toString());

                try {
                    zs.putNextEntry(zipEntry);
                    Files.copy(path, zs);
                    zs.closeEntry();
                } catch (IOException var5) {
                    var5.printStackTrace();
                }

            });
        } catch (Throwable var15) {
            var6 = var15;
            throw var15;
        } finally {
            if (zs != null) {
                if (var6 != null) {
                    try {
                        zs.close();
                    } catch (Throwable var14) {
                        var6.addSuppressed(var14);
                    }
                } else {
                    zs.close();
                }
            }

        }

    }

    private FileUtil() {
    }
}