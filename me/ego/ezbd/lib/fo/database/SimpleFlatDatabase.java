package me.ego.ezbd.lib.fo.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MathUtil;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.debug.LagCatcher;
import me.ego.ezbd.lib.fo.settings.SimpleSettings;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;

public abstract class SimpleFlatDatabase<T> extends SimpleDatabase {
    private boolean isQuerying = false;

    public SimpleFlatDatabase() {
    }

    protected final void onConnected() {
        this.update("CREATE TABLE IF NOT EXISTS {table}(UUID varchar(64), Name text, Data text, Updated bigint)");
        this.removeOldEntries();
        this.onConnectFinish();
    }

    protected void onConnectFinish() {
    }

    private void removeOldEntries() {
        long threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis((long)this.getExpirationDays());
        this.update("DELETE FROM {table} WHERE Updated < " + threshold + "");
    }

    protected int getExpirationDays() {
        return 90;
    }

    public final void load(UUID uuid, T cache) {
        if (this.isLoaded() && !this.isQuerying) {
            try {
                LagCatcher.start("mysql");
                this.isQuerying = true;
                Debugger.debug("mysql", new String[]{"---------------- MySQL - Loading data for " + uuid});
                ResultSet resultSet = this.query("SELECT * FROM {table} WHERE UUID='" + uuid + "'");
                String dataRaw = resultSet.next() ? resultSet.getString("Data") : "{}";
                Debugger.debug("mysql", new String[]{"JSON: " + dataRaw});
                SerializedMap data = SerializedMap.fromJson(dataRaw);
                Debugger.debug("mysql", new String[]{"Deserialized data: " + data});
                this.onLoad(data, cache);
                resultSet.close();
            } catch (Throwable var9) {
                Common.error(var9, new String[]{"Failed to load data from MySQL!", "UUID: " + uuid, "Error: %error"});
            } finally {
                this.isQuerying = false;
                this.logPerformance("loading");
            }

        }
    }

    protected abstract void onLoad(SerializedMap var1, T var2);

    public final void save(String name, UUID uuid, T cache) {
        if (this.isLoaded() && !this.isQuerying) {
            try {
                LagCatcher.start("mysql");
                this.isQuerying = true;
                SerializedMap data = this.onSave(cache);
                Debugger.debug("mysql", new String[]{"---------------- MySQL - Saving data for " + uuid});
                Debugger.debug("mysql", new String[]{"Raw data: " + data});
                Debugger.debug("mysql", new String[]{"JSON: " + (data == null ? "null" : data.toJson())});
                if (data != null && !data.isEmpty()) {
                    if (this.isStored(uuid)) {
                        this.update("UPDATE {table} SET Data='" + data.toJson() + "', Updated='" + System.currentTimeMillis() + "' WHERE UUID='" + uuid + "';");
                    } else {
                        this.update("INSERT INTO {table}(UUID, Name, Data, Updated) VALUES ('" + uuid + "', '" + name + "', '" + data.toJson() + "', '" + System.currentTimeMillis() + "');");
                    }
                } else {
                    this.update("DELETE FROM {table} WHERE UUID= '" + uuid + "';");
                    if (Debugger.isDebugged("mysql")) {
                        Debugger.debug("mysql", new String[]{"Data was empty, row has been removed."});
                    }
                }
            } catch (Throwable var8) {
                Common.error(var8, new String[]{"Failed to save data to MySQL!", "UUID: " + uuid, "Error: %error"});
            } finally {
                this.isQuerying = false;
                this.logPerformance("saving");
            }

        }
    }

    private void logPerformance(String operation) {
        boolean isMainThread = Bukkit.isPrimaryThread();
        LagCatcher.end("mysql", isMainThread ? 10 : MathUtil.atLeast(200, SimpleSettings.LAG_THRESHOLD_MILLIS), WordUtils.capitalize(operation) + " data to MySQL took {time} ms" + (isMainThread ? " - To prevent slowing the server, " + operation + " can be made async (carefully)" : ""));
    }

    private boolean isStored(@NonNull UUID uuid) throws SQLException {
        if (uuid == null) {
            throw new NullPointerException("uuid is marked non-null but is null");
        } else {
            ResultSet resultSet = this.query("SELECT * FROM {table} WHERE UUID= '" + uuid.toString() + "'");
            if (resultSet == null) {
                return false;
            } else if (resultSet.next()) {
                return resultSet.getString("UUID") != null;
            } else {
                return false;
            }
        }
    }

    protected abstract SerializedMap onSave(T var1);
}
