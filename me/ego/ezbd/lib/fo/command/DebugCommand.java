package me.ego.ezbd.lib.fo.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.TimeUtil;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Commands;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public final class DebugCommand extends SimpleSubCommand {
    public DebugCommand() {
        super("debug");
        this.setDescription("ZIP your settings for reporting bugs.");
    }

    protected void onCommand() {
        this.tell(new String[]{Commands.DEBUG_PREPARING});
        File debugFolder = FileUtil.getFile("debug");
        List<File> files = this.listFilesRecursively(SimplePlugin.getData(), new ArrayList());
        FileUtil.deleteRecursivelly(debugFolder);
        this.writeDebugInformation();
        this.copyFilesToDebug(files);
        this.zipAndRemoveFolder(debugFolder);
        this.tell(new String[]{Commands.DEBUG_SUCCESS.replace("{amount}", String.valueOf(files.size()))});
    }

    private void writeDebugInformation() {
        FileUtil.write("debug/general.txt", new String[]{Common.consoleLine(), " Debug log generated " + TimeUtil.getFormattedDate(), Common.consoleLine(), "Plugin: " + SimplePlugin.getInstance().getDescription().getFullName(), "Server Version: " + Bukkit.getName() + " " + MinecraftVersion.getServerVersion(), "Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.specification.vendor") + "/" + System.getProperty("java.vm.vendor") + ")", "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"), "Players Online: " + Remain.getOnlinePlayers().size(), "Plugins: " + Common.join(Bukkit.getPluginManager().getPlugins(), ", ", (plugin) -> {
            return plugin.getDescription().getFullName();
        })});
    }

    private void copyFilesToDebug(List<File> files) {
        Iterator var2 = files.iterator();

        while(var2.hasNext()) {
            File file = (File)var2.next();

            try {
                String path = file.getPath().replace("\\", "/").replace("plugins/" + SimplePlugin.getNamed(), "");
                File copy = FileUtil.createIfNotExists("debug/" + path);
                if (!file.getName().endsWith(".yml")) {
                    Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    FileConfiguration config = FileUtil.loadConfigurationStrict(file);
                    FileConfiguration copyConfig = FileUtil.loadConfigurationStrict(copy);
                    Iterator var8 = config.getValues(true).entrySet().iterator();

                    while(var8.hasNext()) {
                        Entry<String, Object> entry = (Entry)var8.next();
                        String key = (String)entry.getKey();
                        if (!key.contains("MySQL")) {
                            copyConfig.set(key, entry.getValue());
                        }
                    }

                    copyConfig.save(copy);
                }
            } catch (Exception var11) {
                var11.printStackTrace();
                this.returnTell(new String[]{Commands.DEBUG_COPY_FAIL.replace("{file}", file.getName())});
            }
        }

    }

    private void zipAndRemoveFolder(File folder) {
        try {
            String path = folder.getPath();
            FileUtil.zip(path, path);
            FileUtil.deleteRecursivelly(folder);
        } catch (IOException var3) {
            var3.printStackTrace();
            this.returnTell(new String[]{Commands.DEBUG_ZIP_FAIL});
        }

    }

    private List<File> listFilesRecursively(File folder, List<File> files) {
        File[] var3 = folder.listFiles();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            if (file.isDirectory()) {
                if (!file.getName().equals("logs") && !file.getName().equals("debug")) {
                    this.listFilesRecursively(file, files);
                }
            } else if (!file.getName().equals("debug.zip") && !file.getName().equals("mysql.yml")) {
                files.add(file);
            }
        }

        return files;
    }

    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}