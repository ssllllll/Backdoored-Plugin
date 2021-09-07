package me.ego.ezbd.lib.fo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.RandomUtil;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.remain.Remain;

public class SimpleDatabase {
    private Connection connection;
    private SimpleDatabase.LastCredentials lastCredentials;
    private final StrictMap<String, String> sqlVariables = new StrictMap();
    private boolean batchUpdateGoingOn = false;

    public SimpleDatabase() {
    }

    public final void connect(String host, int port, String database, String user, String password) {
        this.connect(host, port, database, user, password, (String)null);
    }

    public final void connect(String host, int port, String database, String user, String password, String table) {
        this.connect(host, port, database, user, password, table, true);
    }

    public final void connect(String host, int port, String database, String user, String password, String table, boolean autoReconnect) {
        this.connect("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + autoReconnect, user, password, table);
    }

    public final void connect(String url, String user, String password) {
        this.connect(url, user, password, (String)null);
    }

    public final void connect(String url, String user, String password, String table) {
        this.close();

        try {
            if (!ReflectionUtil.isClassAvailable("com.mysql.cj.jdbc.Driver")) {
                Class.forName("com.mysql.jdbc.Driver");
            }

            this.lastCredentials = new SimpleDatabase.LastCredentials(url, user, password, table);
            this.connection = DriverManager.getConnection(url, user, password);
            this.onConnected();
        } catch (Exception var6) {
            if (Common.getOrEmpty(var6.getMessage()).contains("No suitable driver found")) {
                Common.logFramed(true, new String[]{"Failed to look up MySQL driver", "If you had MySQL disabled, then enabled it and reload,", "this is normal - just restart.", "", "You have have access to your server machine, try installing", "https://dev.mysql.com/downloads/connector/j/5.1.html#downloads", "", "If this problem persists after a restart, please contact", "your hosting provider."});
            } else {
                Common.logFramed(true, new String[]{"Failed to connect to MySQL database", "URL: " + url, "Error: " + var6.getMessage()});
            }

            Remain.sneaky(var6);
        }

    }

    private final void connectUsingLastCredentials() {
        if (this.lastCredentials != null) {
            this.connect(this.lastCredentials.url, this.lastCredentials.user, this.lastCredentials.password, this.lastCredentials.table);
        }

    }

    protected void onConnected() {
    }

    public final void close() {
        if (this.connection != null) {
            synchronized(this.connection) {
                try {
                    this.connection.close();
                } catch (SQLException var4) {
                    Common.error(var4, new String[]{"Error closing MySQL connection!"});
                }
            }
        }

    }

    protected final void insert(@NonNull SerializedMap columsAndValues) {
        if (columsAndValues == null) {
            throw new NullPointerException("columsAndValues is marked non-null but is null");
        } else {
            this.insert("{table}", columsAndValues);
        }
    }

    protected final void insert(String table, @NonNull SerializedMap columsAndValues) {
        if (columsAndValues == null) {
            throw new NullPointerException("columsAndValues is marked non-null but is null");
        } else {
            String columns = Common.join(columsAndValues.keySet());
            String values = Common.join(columsAndValues.values(), ", ", (value) -> {
                return value != null && !value.equals("NULL") ? "'" + SerializeUtil.serialize(value).toString() + "'" : "NULL";
            });
            String duplicateUpdate = Common.join(columsAndValues.entrySet(), ", ", (entry) -> {
                return (String)entry.getKey() + "=VALUES(" + (String)entry.getKey() + ")";
            });
            this.update("INSERT INTO " + this.replaceVariables(table) + " (" + columns + ") VALUES (" + values + ") ON DUPLICATE KEY UPDATE " + duplicateUpdate + ";");
        }
    }

    protected final void insertBatch(@NonNull List<SerializedMap> maps) {
        if (maps == null) {
            throw new NullPointerException("maps is marked non-null but is null");
        } else {
            this.insertBatch("{table}", maps);
        }
    }

    protected final void insertBatch(String table, @NonNull List<SerializedMap> maps) {
        if (maps == null) {
            throw new NullPointerException("maps is marked non-null but is null");
        } else {
            List<String> sqls = new ArrayList();
            Iterator var4 = maps.iterator();

            while(var4.hasNext()) {
                SerializedMap map = (SerializedMap)var4.next();
                String columns = Common.join(map.keySet());
                String values = Common.join(map.values(), ", ", (value) -> {
                    return this.parseValue(value);
                });
                String duplicateUpdate = Common.join(map.entrySet(), ", ", (entry) -> {
                    return (String)entry.getKey() + "=VALUES(" + (String)entry.getKey() + ")";
                });
                sqls.add("INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ") ON DUPLICATE KEY UPDATE " + duplicateUpdate + ";");
            }

            this.batchUpdate(sqls);
        }
    }

    private final String parseValue(Object value) {
        return value != null && !value.equals("NULL") ? "'" + SerializeUtil.serialize(value).toString() + "'" : "NULL";
    }

    protected final void update(String sql) {
        this.checkEstablished();
        synchronized(this.connection) {
            if (!this.isConnected()) {
                this.connectUsingLastCredentials();
            }

            sql = this.replaceVariables(sql);
            Valid.checkBoolean(!sql.contains("{table}"), "Table not set! Either use connect() method that specifies it or call addVariable(table, 'yourtablename') in your constructor!", new Object[0]);
            Debugger.debug("mysql", new String[]{"Updating MySQL with: " + sql});

            try {
                Statement statement = this.connection.createStatement();
                statement.executeUpdate(sql);
                statement.close();
            } catch (SQLException var5) {
                Common.error(var5, new String[]{"Error on updating MySQL with: " + sql});
            }

        }
    }

    protected final ResultSet query(String sql) {
        this.checkEstablished();
        synchronized(this.connection) {
            if (!this.isConnected()) {
                this.connectUsingLastCredentials();
            }

            sql = this.replaceVariables(sql);
            Debugger.debug("mysql", new String[]{"Querying MySQL with: " + sql});

            ResultSet var10000;
            try {
                Statement statement = this.connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                var10000 = resultSet;
            } catch (SQLException var6) {
                var6.printStackTrace();
                Common.throwError(var6, new String[]{"Error on querying MySQL with: " + sql});
                return null;
            }

            return var10000;
        }
    }

    protected final void batchUpdate(@NonNull List<String> sqls) {
        if (sqls == null) {
            throw new NullPointerException("sqls is marked non-null but is null");
        } else if (sqls.size() != 0) {
            try {
                Statement batchStatement = this.getConnection().createStatement(1005, 1008);
                int processedCount = sqls.size();
                this.getConnection().setAutoCommit(false);
                Iterator var4 = sqls.iterator();

                while(var4.hasNext()) {
                    String sql = (String)var4.next();
                    batchStatement.addBatch(this.replaceVariables(sql));
                }

                if (processedCount > 10000) {
                    Common.log(new String[]{"Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE " + (processedCount > 50000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed."});
                }

                this.batchUpdateGoingOn = true;
                (new Timer()).scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        if (SimpleDatabase.this.batchUpdateGoingOn) {
                            Common.log(new String[]{"Still executing, " + (String)RandomUtil.nextItem(new String[]{"keep calm", "stand by", "watch the show", "check your db", "drink water", "call your friend"}) + " and DO NOT SHUTDOWN YOUR SERVER."});
                        } else {
                            this.cancel();
                        }

                    }
                }, 30000L, 30000L);
                batchStatement.executeBatch();
                this.getConnection().commit();
            } catch (Throwable var14) {
                var14.printStackTrace();
            } finally {
                try {
                    this.getConnection().setAutoCommit(true);
                } catch (SQLException var13) {
                    var13.printStackTrace();
                }

                this.batchUpdateGoingOn = false;
            }

        }
    }

    protected final PreparedStatement prepareStatement(String sql) throws SQLException {
        this.checkEstablished();
        synchronized(this.connection) {
            if (!this.isConnected()) {
                this.connectUsingLastCredentials();
            }

            sql = this.replaceVariables(sql);
            Debugger.debug("mysql", new String[]{"Preparing statement: " + sql});
            return this.connection.prepareStatement(sql);
        }
    }

    protected final boolean isConnected() {
        if (!this.isLoaded()) {
            return false;
        } else {
            synchronized(this.connection) {
                boolean var10000;
                try {
                    var10000 = this.connection != null && !this.connection.isClosed() && this.connection.isValid(0);
                } catch (SQLException var4) {
                    return false;
                }

                return var10000;
            }
        }
    }

    protected final String getTable() {
        this.checkEstablished();
        return Common.getOrEmpty(this.lastCredentials.table);
    }

    private final void checkEstablished() {
        Valid.checkBoolean(this.isLoaded(), "Connection was never established", new Object[0]);
    }

    public final boolean isLoaded() {
        return this.connection != null;
    }

    protected final void addVariable(String name, String value) {
        this.sqlVariables.put(name, value);
    }

    protected final String replaceVariables(String sql) {
        Entry entry;
        for(Iterator var2 = this.sqlVariables.entrySet().iterator(); var2.hasNext(); sql = sql.replace("{" + (String)entry.getKey() + "}", (CharSequence)entry.getValue())) {
            entry = (Entry)var2.next();
        }

        return sql.replace("{table}", this.getTable());
    }

    protected Connection getConnection() {
        return this.connection;
    }

    private final class LastCredentials {
        private final String url;
        private final String user;
        private final String password;
        private final String table;

        public LastCredentials(String url, String user, String password, String table) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.table = table;
        }
    }
}
