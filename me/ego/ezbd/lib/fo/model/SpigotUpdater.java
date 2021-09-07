package me.ego.ezbd.lib.fo.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Update;
import org.bukkit.Bukkit;

public class SpigotUpdater implements Runnable {
    private final int resourceId;
    private final boolean download;
    private boolean newVersionAvailable;
    private String newVersion;

    public SpigotUpdater(int resourceId) {
        this(resourceId, false);
    }

    public SpigotUpdater(int resourceId, boolean download) {
        this.newVersionAvailable = false;
        this.newVersion = "";
        this.resourceId = resourceId;
        this.download = download;
    }

    public void run() {
        if (this.resourceId != -1) {
            String currentVersion = SimplePlugin.getVersion();
            if (this.canUpdateFrom(currentVersion)) {
                try {
                    HttpURLConnection connection = (HttpURLConnection)(new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId)).openConnection();
                    connection.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    Throwable var4 = null;

                    try {
                        String line = reader.readLine();
                        this.newVersion = line;
                    } catch (Throwable var16) {
                        var4 = var16;
                        throw var16;
                    } finally {
                        if (reader != null) {
                            if (var4 != null) {
                                try {
                                    reader.close();
                                } catch (Throwable var15) {
                                    var4.addSuppressed(var15);
                                }
                            } else {
                                reader.close();
                            }
                        }

                    }

                    if (this.newVersion.isEmpty()) {
                        return;
                    }

                    if (this.isNewerVersion(currentVersion, this.newVersion) && this.canUpdateTo(this.newVersion)) {
                        this.newVersionAvailable = true;
                        if (this.download) {
                            connection = (HttpURLConnection)(new URL("https://api.spiget.org/v2/resources/" + this.resourceId + "/download")).openConnection();
                            connection.setRequestProperty("User-Agent", SimplePlugin.getNamed());
                            Valid.checkBoolean(connection.getResponseCode() == 200, "Downloading update for " + SimplePlugin.getNamed() + " returned " + connection.getResponseCode() + ", aborting.", new Object[0]);
                            ReadableByteChannel channel = Channels.newChannel(connection.getInputStream());
                            File updateFolder = Bukkit.getUpdateFolderFile();
                            FileUtil.createIfNotExists(updateFolder);
                            File destination = new File(updateFolder, SimplePlugin.getNamed() + "-" + this.newVersion + ".jar");
                            FileOutputStream output = new FileOutputStream(destination);
                            output.getChannel().transferFrom(channel, 0L, 9223372036854775807L);
                            output.flush();
                            output.close();
                            Common.log(new String[]{this.getDownloadMessage()});
                        } else {
                            Common.log(new String[]{this.getNotifyMessage()});
                        }
                    }
                } catch (UnknownHostException var18) {
                    Common.log(new String[]{"Could not check for update from " + var18.getMessage() + "."});
                } catch (IOException var19) {
                    if (!var19.getMessage().startsWith("Server returned HTTP response code: 403")) {
                        if (var19.getMessage().startsWith("Server returned HTTP response code:")) {
                            Common.log(new String[]{"Could not check for update, SpigotMC site appears to be down (or unaccessible): " + var19.getMessage()});
                        } else {
                            Common.error(var19, new String[]{"IOException performing update from SpigotMC.org check for " + SimplePlugin.getNamed()});
                        }
                    }
                } catch (Exception var20) {
                    Common.error(var20, new String[]{"Unknown error performing update from SpigotMC.org check for " + SimplePlugin.getNamed()});
                }

            }
        }
    }

    protected boolean canUpdateFrom(String currentVersion) {
        return !currentVersion.contains("SNAPSHOT") && !currentVersion.contains("DEV");
    }

    protected boolean canUpdateTo(String newVersion) {
        return !newVersion.contains("SNAPSHOT") && !newVersion.contains("DEV");
    }

    private boolean isNewerVersion(String current, String remote) {
        if (remote.contains("-LEGACY")) {
            return false;
        } else {
            String[] currParts = this.removeTagsInNumber(current).split("\\.");
            String[] remoteParts = this.removeTagsInNumber(remote).split("\\.");
            if (currParts.length != remoteParts.length) {
                boolean olderIsLonger = currParts.length > remoteParts.length;
                String[] modifiedParts = new String[olderIsLonger ? currParts.length : remoteParts.length];

                for(int i = 0; i < (olderIsLonger ? currParts.length : remoteParts.length); ++i) {
                    modifiedParts[i] = olderIsLonger ? (remoteParts.length > i ? remoteParts[i] : "0") : (currParts.length > i ? currParts[i] : "0");
                }

                if (olderIsLonger) {
                    remoteParts = modifiedParts;
                } else {
                    currParts = modifiedParts;
                }
            }

            for(int i = 0; i < currParts.length; ++i) {
                if (Integer.parseInt(currParts[i]) > Integer.parseInt(remoteParts[i])) {
                    return false;
                }

                if (Integer.parseInt(remoteParts[i]) > Integer.parseInt(currParts[i])) {
                    return true;
                }
            }

            return false;
        }
    }

    protected String removeTagsInNumber(String raw) {
        return raw.split("\\-")[0];
    }

    public final String getNotifyMessage() {
        return this.replaceVariables(Update.AVAILABLE);
    }

    public final String getDownloadMessage() {
        return this.replaceVariables(Update.DOWNLOADED);
    }

    protected String replaceVariables(String message) {
        return message.replace("{resource_id}", this.resourceId + "").replace("{plugin_name}", SimplePlugin.getNamed()).replace("{new}", this.newVersion).replace("{current}", SimplePlugin.getVersion()).replace("{user_id}", "%%__USER__%%");
    }

    public int getResourceId() {
        return this.resourceId;
    }

    public boolean isNewVersionAvailable() {
        return this.newVersionAvailable;
    }

    public String getNewVersion() {
        return this.newVersion;
    }
}