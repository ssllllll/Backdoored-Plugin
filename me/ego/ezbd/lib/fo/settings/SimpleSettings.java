package me.ego.ezbd.lib.fo.settings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;

public abstract class SimpleSettings extends YamlStaticConfig {
    private static boolean settingsClassCalled;
    protected static Integer VERSION;
    public static DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static StrictList<String> DEBUG_SECTIONS = new StrictList();
    public static String PLUGIN_PREFIX = "&7" + SimplePlugin.getNamed() + " //";
    public static Integer LAG_THRESHOLD_MILLIS = 100;
    public static Integer REGEX_TIMEOUT = 100;
    public static StrictList<String> MAIN_COMMAND_ALIASES = new StrictList();
    public static String LOCALE_PREFIX = "en";
    public static Boolean NOTIFY_UPDATES = true;

    public SimpleSettings() {
    }

    protected final void load() throws Exception {
        this.createFileAndLoad(this.getSettingsFileName());
    }

    protected String getSettingsFileName() {
        return "settings.yml";
    }

    protected void preLoad() {
        pathPrefix((String)null);
        if (VERSION = getInteger("Version") != this.getConfigVersion()) {
            set("Version", this.getConfigVersion());
        }

    }

    protected abstract int getConfigVersion();

    private static void init() {
        Valid.checkBoolean(!settingsClassCalled, "Settings class already loaded!", new Object[0]);
        pathPrefix((String)null);
        upgradeOldSettings();
        if (isSetDefault("Timestamp_Format")) {
            try {
                TIMESTAMP_FORMAT = new SimpleDateFormat(getString("Timestamp_Format"));
            } catch (IllegalArgumentException var2) {
                Common.throwError(var2, new String[]{"Wrong 'Timestamp_Format '" + getString("Timestamp_Format") + "', see https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for examples'"});
            }
        }

        if (isSetDefault("Prefix")) {
            PLUGIN_PREFIX = getString("Prefix");
        }

        if (isSetDefault("Log_Lag_Over_Milis")) {
            LAG_THRESHOLD_MILLIS = getInteger("Log_Lag_Over_Milis");
            Valid.checkBoolean(LAG_THRESHOLD_MILLIS == -1 || LAG_THRESHOLD_MILLIS >= 0, "Log_Lag_Over_Milis must be either -1 to disable, 0 to log all or greater!", new Object[0]);
            if (LAG_THRESHOLD_MILLIS == 0) {
                Common.log(new String[]{"&eLog_Lag_Over_Milis is 0, all performance is logged. Set to -1 to disable."});
            }
        }

        if (isSetDefault("Debug")) {
            DEBUG_SECTIONS = new StrictList(getStringList("Debug"));
        }

        if (isSetDefault("Regex_Timeout_Milis")) {
            REGEX_TIMEOUT = getInteger("Regex_Timeout_Milis");
        }

        boolean keySet = hasLocalization();
        boolean keySet = isSetDefault("Locale");
        if (keySet && !keySet) {
            throw new FoException("Since you have your Localization class you must set the 'Locale' key in " + getFileName());
        } else {
            LOCALE_PREFIX = keySet ? getString("Locale") : LOCALE_PREFIX;
            keySet = isSetDefault("Command_Aliases");
            if (SimplePlugin.getInstance().getMainCommand() != null && !keySet) {
                throw new FoException("Since you override getMainCommand in your main plugin class you must set the 'Command_Aliases' key in " + getFileName());
            } else {
                MAIN_COMMAND_ALIASES = keySet ? getCommandList("Command_Aliases") : MAIN_COMMAND_ALIASES;
                keySet = isSetDefault("Notify_Updates");
                NOTIFY_UPDATES = keySet ? getBoolean("Notify_Updates") : NOTIFY_UPDATES;
                settingsClassCalled = true;
            }
        }
    }

    private static boolean hasLocalization() {
        SimplePlugin plugin = SimplePlugin.getInstance();
        int localeClasses = 0;
        if (plugin.getSettings() != null) {
            Iterator var2 = plugin.getSettings().iterator();

            while(var2.hasNext()) {
                Class<?> clazz = (Class)var2.next();
                if (SimpleLocalization.class.isAssignableFrom(clazz)) {
                    ++localeClasses;
                }
            }
        }

        Valid.checkBoolean(localeClasses < 2, "You cannot have more than 1 class extend SimpleLocalization!", new Object[0]);
        return localeClasses == 1;
    }

    private static void upgradeOldSettings() {
        if (isSetAbsolute("Debugger")) {
            move("Debugger", "Debug");
        }

        if (isSetAbsolute("Serialization_Number")) {
            move("Serialization_Number", "Serialization");
        }

        if (isSetAbsolute("Debugger.Keys")) {
            move("Debugger.Keys", "Serialization");
            move("Debugger.Sections", "Debug");
        }

        if (isSetAbsolute("Debug") && !(getObject("Debug") instanceof List)) {
            set("Debug", (Object)null);
        }

        if (isSetAbsolute("Plugin_Prefix")) {
            move("Plugin_Prefix", "Prefix");
        }

        if (isSetAbsolute("Check_Updates")) {
            move("Check_Updates", "Notify_Updates");
        }

    }

    public static final Boolean isSettingsCalled() {
        return settingsClassCalled;
    }

    public static final void resetSettingsCall() {
        settingsClassCalled = false;
    }
}