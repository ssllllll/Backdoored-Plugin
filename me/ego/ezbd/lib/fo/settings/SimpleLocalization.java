package me.ego.ezbd.lib.fo.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class SimpleLocalization extends YamlStaticConfig {
    private static String FALLBACK_LOCALIZATION_FILE = "localization/messages_en.yml";
    private static FileConfiguration fallbackLocalization;
    private static boolean localizationClassCalled;
    protected static Integer VERSION;
    public static String NO_PERMISSION = "&cInsufficient permission ({permission}).";
    public static String SERVER_PREFIX = "[Server]";
    public static String CONSOLE_NAME = "Console";
    public static String DATA_MISSING = "&c{name} lacks database information! Please only create {type} in-game! Skipping..";
    public static String CONVERSATION_REQUIRES_PLAYER = "Only players may enter this conversation.";

    public SimpleLocalization() {
    }

    public static void setFallbackLocalizationFile(String fallBackFile) {
        FALLBACK_LOCALIZATION_FILE = fallBackFile;
    }

    protected final void load() throws Exception {
        this.createLocalizationFile(SimpleSettings.LOCALE_PREFIX);
    }

    protected final void preLoad() {
        pathPrefix((String)null);
        if (VERSION = getInteger("Version") != this.getConfigVersion()) {
            set("Version", this.getConfigVersion());
        }

        fallbackLocalization = FileUtil.loadInternalConfiguration(FALLBACK_LOCALIZATION_FILE);
    }

    protected abstract int getConfigVersion();

    protected static final String getFallbackString(String path) {
        return (String)getFallback(path, String.class);
    }

    protected static final <T> List<T> getFallbackList(String path, Class<T> listType) {
        List<T> list = new ArrayList();
        List<Object> objects = (List)getFallback(path, List.class);
        if (objects != null) {
            Iterator var4 = objects.iterator();

            while(var4.hasNext()) {
                Object object = var4.next();
                list.add(object != null ? SerializeUtil.deserialize(listType, object) : null);
            }
        }

        return list;
    }

    protected static final <T> T getFallback(String path, Class<T> typeOf) {
        if (!isSet(path) && !isSetDefault(path) && !"en".equals(SimpleSettings.LOCALE_PREFIX)) {
            String relativePath = formPathPrefix(path);
            Object key = fallbackLocalization.get(relativePath);
            Valid.checkNotNull(key, "Neither " + getFileName() + ", the default one, nor " + FALLBACK_LOCALIZATION_FILE + " contained " + relativePath + "! Please report this to " + SimplePlugin.getNamed() + " developers!");
            Valid.checkBoolean(key.getClass().isAssignableFrom(typeOf), "Expected " + typeOf + " at " + relativePath + " in " + FALLBACK_LOCALIZATION_FILE + " but got " + key.getClass() + ": " + key, new Object[0]);
            set(path, key);
            return key;
        } else {
            return get(path, typeOf);
        }
    }

    private static void init() {
        pathPrefix((String)null);
        Valid.checkBoolean(!localizationClassCalled, "Localization class already loaded!", new Object[0]);
        if (isSetDefault("No_Permission")) {
            NO_PERMISSION = getString("No_Permission");
        }

        if (isSetDefault("Server_Prefix")) {
            SERVER_PREFIX = getString("Server_Prefix");
        }

        if (isSetDefault("Console_Name")) {
            CONSOLE_NAME = getString("Console_Name");
        }

        if (isSetDefault("Data_Missing")) {
            DATA_MISSING = getString("Data_Missing");
        }

        if (isSetDefault("Conversation_Requires_Player")) {
            CONVERSATION_REQUIRES_PLAYER = getString("Conversation_Requires_Player");
        }

        localizationClassCalled = true;
    }

    public static final Boolean isLocalizationCalled() {
        return localizationClassCalled;
    }

    public static final void resetLocalizationCall() {
        localizationClassCalled = false;
    }

    public static final class Update {
        public static String AVAILABLE = "&2A new version of &3{plugin_name}&2 is available.\n&2Current version: &f{current}&2; New version: &f{new}\n&2URL: &7https://spigotmc.org/resources/{resource_id}/.";
        public static String DOWNLOADED = "&3{plugin_name}&2 has been upgraded from {current} to {new}.\n&2Visit &7https://spigotmc.org/resources/{resource_id} &2for more information.\n&2Please restart the server to load the new version.";

        public Update() {
        }

        private static void init() {
            YamlStaticConfig.pathPrefix((String)null);
            if (YamlStaticConfig.isSetAbsolute("Update_Available")) {
                YamlStaticConfig.move("Update_Available", "Update.Available");
            }

            YamlStaticConfig.pathPrefix("Update");
            if (YamlStaticConfig.isSetDefault("Available")) {
                AVAILABLE = YamlStaticConfig.getString("Available");
            }

            if (YamlStaticConfig.isSetDefault("Downloaded")) {
                DOWNLOADED = YamlStaticConfig.getString("Downloaded");
            }

        }
    }

    public static final class Tool {
        public static String ERROR = "&cOups! There was a problem with this tool! Please contact the administrator to review the console for details.";

        public Tool() {
        }

        private static void init() {
            YamlStaticConfig.pathPrefix("Tool");
            if (YamlStaticConfig.isSetDefault("Error")) {
                ERROR = YamlStaticConfig.getString("Error");
            }

        }
    }

    public static final class Menu {
        public static String ITEM_DELETED = "&2The {item} has been deleted.";
        public static String CANNOT_OPEN_DURING_CONVERSATION = "&cType 'exit' to quit your conversation before opening menu.";
        public static String ERROR = "&cOups! There was a problem with this menu! Please contact the administrator to review the console for details.";
        public static String PAGE_PREVIOUS = "&8<< &fPage {page}";
        public static String PAGE_NEXT = "Page {page} &8>>";
        public static String PAGE_FIRST = "&7First Page";
        public static String PAGE_LAST = "&7Last Page";
        public static String TITLE_TOOLS = "Tools Menu";
        public static String TOOLTIP_INFO = "&fMenu Information";
        public static String BUTTON_RETURN_TITLE = "&4&lReturn";
        public static String[] BUTTON_RETURN_LORE = new String[]{"", "Return back."};

        public Menu() {
        }

        private static void init() {
            YamlStaticConfig.pathPrefix("Menu");
            if (YamlStaticConfig.isSetDefault("Item_Deleted")) {
                ITEM_DELETED = YamlStaticConfig.getString("Item_Deleted");
            }

            if (YamlStaticConfig.isSetDefault("Cannot_Open_During_Conversation")) {
                CANNOT_OPEN_DURING_CONVERSATION = YamlStaticConfig.getString("Cannot_Open_During_Conversation");
            }

            if (YamlStaticConfig.isSetDefault("Error")) {
                ERROR = YamlStaticConfig.getString("Error");
            }

            if (YamlStaticConfig.isSetDefault("Page_Previous")) {
                PAGE_PREVIOUS = YamlStaticConfig.getString("Page_Previous");
            }

            if (YamlStaticConfig.isSetDefault("Page_Next")) {
                PAGE_NEXT = YamlStaticConfig.getString("Page_Next");
            }

            if (YamlStaticConfig.isSetDefault("Page_First")) {
                PAGE_FIRST = YamlStaticConfig.getString("Page_First");
            }

            if (YamlStaticConfig.isSetDefault("Page_Last")) {
                PAGE_LAST = YamlStaticConfig.getString("Page_Last");
            }

            if (YamlStaticConfig.isSetDefault("Title_Tools")) {
                TITLE_TOOLS = YamlStaticConfig.getString("Title_Tools");
            }

            if (YamlStaticConfig.isSetDefault("Tooltip_Info")) {
                TOOLTIP_INFO = YamlStaticConfig.getString("Tooltip_Info");
            }

            if (YamlStaticConfig.isSetDefault("Button_Return_Title")) {
                BUTTON_RETURN_TITLE = YamlStaticConfig.getString("Button_Return_Title");
            }

            if (YamlStaticConfig.isSetDefault("Button_Return_Lore")) {
                BUTTON_RETURN_LORE = YamlStaticConfig.getStringArray("Button_Return_Lore");
            }

        }
    }

    public static final class Pages {
        public static String NO_PAGE_NUMBER = "&cPlease specify the page number for this command.";
        public static String NO_PAGES = "&cYou do not have any pages saved to show.";
        public static String NO_PAGE = "Pages do not contain the given page number.";
        public static String INVALID_PAGE = "&cYour input '{input}' is not a valid number.";
        public static String GO_TO_PAGE = "&7Go to page {page}";
        public static String GO_TO_FIRST_PAGE = "&7Go to the first page";
        public static String[] TOOLTIP = new String[]{"&7You can also navigate using the", "&7hidden /#flp <page> command."};

        public Pages() {
        }

        private static void init() {
            YamlStaticConfig.pathPrefix("Pages");
            if (YamlStaticConfig.isSetDefault("No_Page_Number")) {
                NO_PAGE_NUMBER = YamlStaticConfig.getString("No_Page_Number");
            }

            if (YamlStaticConfig.isSetDefault("No_Pages")) {
                NO_PAGES = YamlStaticConfig.getString("No_Pages");
            }

            if (YamlStaticConfig.isSetDefault("No_Page")) {
                NO_PAGE = YamlStaticConfig.getString("No_Page");
            }

            if (YamlStaticConfig.isSetDefault("Invalid_Page")) {
                INVALID_PAGE = YamlStaticConfig.getString("Invalid_Page");
            }

            if (YamlStaticConfig.isSetDefault("Go_To_Page")) {
                GO_TO_PAGE = YamlStaticConfig.getString("Go_To_Page");
            }

            if (YamlStaticConfig.isSetDefault("Go_To_First_Page")) {
                GO_TO_FIRST_PAGE = YamlStaticConfig.getString("Go_To_First_Page");
            }

            if (YamlStaticConfig.isSetDefault("Tooltip")) {
                TOOLTIP = YamlStaticConfig.getStringArray("Tooltip");
            }

        }
    }

    public static final class Player {
        public static String NOT_ONLINE = "&cPlayer {player} &cis not online on this server.";

        public Player() {
        }

        private static void init() {
            YamlStaticConfig.pathPrefix("Player");
            if (YamlStaticConfig.isSetDefault("Not_Online")) {
                NOT_ONLINE = YamlStaticConfig.getString("Not_Online");
            }

        }
    }

    public static final class Commands {
        public static String NO_CONSOLE = "&cYou may only use this command as a player";
        public static String CONSOLE_MISSING_PLAYER_NAME = "When running from console, specify player name.";
        public static String COOLDOWN_WAIT = "&cWait {duration} second(s) before using this command again.";
        public static String INVALID_ARGUMENT = "&cInvalid argument. Run &6/{label} ? &cfor help.";
        public static String INVALID_SUB_ARGUMENT = "&cInvalid argument. Run '/{label} {0}' for help.";
        public static String INVALID_ARGUMENT_MULTILINE = "&cInvalid argument. Usage:";
        public static String INVALID_TIME = "Expected time such as '3 hours' or '15 minutes'. Got: '{input}'";
        public static String INVALID_NUMBER = "The number must be a whole or a decimal number. Got: '{input}'";
        public static String LABEL_AUTHORS = "Made by";
        public static String LABEL_DESCRIPTION = "&c&lDescription:";
        public static String LABEL_OPTIONAL_ARGS = "optional arguments";
        public static String LABEL_REQUIRED_ARGS = "required arguments";
        public static String LABEL_USAGES = "&c&lUsages:";
        public static String LABEL_USAGE = "&c&lUsage:";
        public static String LABEL_HELP_FOR = "Help for /{label}";
        public static String LABEL_SUBCOMMAND_DESCRIPTION = " &f/{label} {sublabel} {usage+}{dash+}{description}";
        public static String HELP_TOOLTIP_DESCRIPTION = "&7Description: &f{description}";
        public static String HELP_TOOLTIP_PERMISSION = "&7Permission: &f{permission}";
        public static String HELP_TOOLTIP_USAGE = "&7Usage: &f";
        public static String RELOAD_DESCRIPTION = "Reload the configuration.";
        public static String RELOAD_STARTED = "Reloading plugin's data, please wait..";
        public static String RELOAD_SUCCESS = "&6{plugin_name} {plugin_version} has been reloaded.";
        public static String RELOAD_FILE_LOAD_ERROR = "&4Oups, &cthere was a problem loading files from your disk! See the console for more information. {plugin_name} has not been reloaded.";
        public static String RELOAD_FAIL = "&4Oups, &creloading failed! See the console for more information. Error: {error}";
        public static String ERROR = "&4&lOups! &cThe command failed :( Check the console and report the error.";
        public static String HEADER_NO_SUBCOMMANDS = "&cThere are no arguments for this command.";
        public static String HEADER_NO_SUBCOMMANDS_PERMISSION = "&cYou don't have permissions to view any subcommands.";
        public static ChatColor HEADER_COLOR;
        public static ChatColor HEADER_SECONDARY_COLOR;
        public static String RELOADING;
        public static String DISABLED;
        public static String USE_WHILE_NULL;
        public static String DEBUG_DESCRIPTION;
        public static String DEBUG_PREPARING;
        public static String DEBUG_SUCCESS;
        public static String DEBUG_COPY_FAIL;
        public static String DEBUG_ZIP_FAIL;
        public static String PERMS_DESCRIPTION;
        public static String PERMS_USAGE;
        public static String PERMS_HEADER;
        public static String PERMS_MAIN;
        public static String PERMS_PERMISSIONS;
        public static String PERMS_TRUE_BY_DEFAULT;
        public static String PERMS_INFO;
        public static String PERMS_DEFAULT;
        public static String PERMS_APPLIED;
        public static String PERMS_YES;
        public static String PERMS_NO;

        public Commands() {
        }

        private static void init() {
            YamlStaticConfig.pathPrefix("Commands");
            if (YamlStaticConfig.isSetDefault("No_Console")) {
                NO_CONSOLE = YamlStaticConfig.getString("No_Console");
            }

            if (YamlStaticConfig.isSetDefault("Console_Missing_Player_Name")) {
                CONSOLE_MISSING_PLAYER_NAME = YamlStaticConfig.getString("Console_Missing_Player_Name");
            }

            if (YamlStaticConfig.isSetDefault("Cooldown_Wait")) {
                COOLDOWN_WAIT = YamlStaticConfig.getString("Cooldown_Wait");
            }

            if (YamlStaticConfig.isSetDefault("Invalid_Argument")) {
                INVALID_ARGUMENT = YamlStaticConfig.getString("Invalid_Argument");
            }

            if (YamlStaticConfig.isSetDefault("Invalid_Sub_Argument")) {
                INVALID_SUB_ARGUMENT = YamlStaticConfig.getString("Invalid_Sub_Argument");
            }

            if (YamlStaticConfig.isSetDefault("Invalid_Argument_Multiline")) {
                INVALID_ARGUMENT_MULTILINE = YamlStaticConfig.getString("Invalid_Argument_Multiline");
            }

            if (YamlStaticConfig.isSetDefault("Invalid_Time")) {
                INVALID_TIME = YamlStaticConfig.getString("Invalid_Time");
            }

            if (YamlStaticConfig.isSetDefault("Invalid_Number")) {
                INVALID_NUMBER = YamlStaticConfig.getString("Invalid_Number");
            }

            if (YamlStaticConfig.isSetDefault("Label_Authors")) {
                LABEL_AUTHORS = YamlStaticConfig.getString("Label_Authors");
            }

            if (YamlStaticConfig.isSetDefault("Label_Description")) {
                LABEL_DESCRIPTION = YamlStaticConfig.getString("Label_Description");
            }

            if (YamlStaticConfig.isSetDefault("Label_Optional_Args")) {
                LABEL_OPTIONAL_ARGS = YamlStaticConfig.getString("Label_Optional_Args");
            }

            if (YamlStaticConfig.isSetDefault("Label_Required_Args")) {
                LABEL_REQUIRED_ARGS = YamlStaticConfig.getString("Label_Required_Args");
            }

            if (YamlStaticConfig.isSetDefault("Label_Usage")) {
                LABEL_USAGE = YamlStaticConfig.getString("Label_Usage");
            }

            if (YamlStaticConfig.isSetDefault("Label_Help_For")) {
                LABEL_HELP_FOR = YamlStaticConfig.getString("Label_Help_For");
            }

            if (YamlStaticConfig.isSetDefault("Label_Subcommand_Description")) {
                LABEL_SUBCOMMAND_DESCRIPTION = YamlStaticConfig.getString("Label_Subcommand_Description");
            }

            if (YamlStaticConfig.isSetDefault("Label_Usages")) {
                LABEL_USAGES = YamlStaticConfig.getString("Label_Usages");
            }

            if (YamlStaticConfig.isSetDefault("Help_Tooltip_Description")) {
                HELP_TOOLTIP_DESCRIPTION = YamlStaticConfig.getString("Help_Tooltip_Description");
            }

            if (YamlStaticConfig.isSetDefault("Help_Tooltip_Permission")) {
                HELP_TOOLTIP_PERMISSION = YamlStaticConfig.getString("Help_Tooltip_Permission");
            }

            if (YamlStaticConfig.isSetDefault("Help_Tooltip_Usage")) {
                HELP_TOOLTIP_USAGE = YamlStaticConfig.getString("Help_Tooltip_Usage");
            }

            if (YamlStaticConfig.isSetDefault("Reload_Description")) {
                RELOAD_DESCRIPTION = YamlStaticConfig.getString("Reload_Description");
            }

            if (YamlStaticConfig.isSetDefault("Reload_Started")) {
                RELOAD_STARTED = YamlStaticConfig.getString("Reload_Started");
            }

            if (YamlStaticConfig.isSetDefault("Reload_Success")) {
                RELOAD_SUCCESS = YamlStaticConfig.getString("Reload_Success");
            }

            if (YamlStaticConfig.isSetDefault("Reload_File_Load_Error")) {
                RELOAD_FILE_LOAD_ERROR = YamlStaticConfig.getString("Reload_File_Load_Error");
            }

            if (YamlStaticConfig.isSetDefault("Reload_Fail")) {
                RELOAD_FAIL = YamlStaticConfig.getString("Reload_Fail");
            }

            if (YamlStaticConfig.isSetDefault("Error")) {
                ERROR = YamlStaticConfig.getString("Error");
            }

            if (YamlStaticConfig.isSetDefault("Header_No_Subcommands")) {
                HEADER_NO_SUBCOMMANDS = YamlStaticConfig.getString("Header_No_Subcommands");
            }

            if (YamlStaticConfig.isSetDefault("Header_No_Subcommands_Permission")) {
                HEADER_NO_SUBCOMMANDS_PERMISSION = YamlStaticConfig.getString("Header_No_Subcommands_Permission");
            }

            if (YamlStaticConfig.isSetDefault("Header_Color")) {
                HEADER_COLOR = (ChatColor)YamlStaticConfig.get("Header_Color", ChatColor.class);
            }

            if (YamlStaticConfig.isSetDefault("Header_Secondary_Color")) {
                HEADER_SECONDARY_COLOR = (ChatColor)YamlStaticConfig.get("Header_Secondary_Color", ChatColor.class);
            }

            if (YamlStaticConfig.isSet("Reloading")) {
                RELOADING = YamlStaticConfig.getString("Reloading");
            }

            if (YamlStaticConfig.isSet("Disabled")) {
                DISABLED = YamlStaticConfig.getString("Disabled");
            }

            if (YamlStaticConfig.isSet("Use_While_Null")) {
                USE_WHILE_NULL = YamlStaticConfig.getString("Use_While_Null");
            }

            if (YamlStaticConfig.isSetDefault("Debug_Description")) {
                DEBUG_DESCRIPTION = YamlStaticConfig.getString("Debug_Description");
            }

            if (YamlStaticConfig.isSetDefault("Debug_Preparing")) {
                DEBUG_PREPARING = YamlStaticConfig.getString("Debug_Preparing");
            }

            if (YamlStaticConfig.isSetDefault("Debug_Success")) {
                DEBUG_SUCCESS = YamlStaticConfig.getString("Debug_Success");
            }

            if (YamlStaticConfig.isSetDefault("Debug_Copy_Fail")) {
                DEBUG_COPY_FAIL = YamlStaticConfig.getString("Debug_Copy_Fail");
            }

            if (YamlStaticConfig.isSetDefault("Debug_Zip_Fail")) {
                DEBUG_ZIP_FAIL = YamlStaticConfig.getString("Debug_Zip_Fail");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Description")) {
                PERMS_DESCRIPTION = YamlStaticConfig.getString("Perms_Description");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Usage")) {
                PERMS_USAGE = YamlStaticConfig.getString("Perms_Usage");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Header")) {
                PERMS_HEADER = YamlStaticConfig.getString("Perms_Header");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Main")) {
                PERMS_MAIN = YamlStaticConfig.getString("Perms_Main");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Permissions")) {
                PERMS_PERMISSIONS = YamlStaticConfig.getString("Perms_Permissions");
            }

            if (YamlStaticConfig.isSetDefault("Perms_True_By_Default")) {
                PERMS_TRUE_BY_DEFAULT = YamlStaticConfig.getString("Perms_True_By_Default");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Info")) {
                PERMS_INFO = YamlStaticConfig.getString("Perms_Info");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Default")) {
                PERMS_DEFAULT = YamlStaticConfig.getString("Perms_Default");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Applied")) {
                PERMS_APPLIED = YamlStaticConfig.getString("Perms_Applied");
            }

            if (YamlStaticConfig.isSetDefault("Perms_Yes")) {
                PERMS_YES = YamlStaticConfig.getString("Perms_Yes");
            }

            if (YamlStaticConfig.isSetDefault("Perms_No")) {
                PERMS_NO = YamlStaticConfig.getString("Perms_No");
            }

        }

        static {
            HEADER_COLOR = ChatColor.GOLD;
            HEADER_SECONDARY_COLOR = ChatColor.RED;
            RELOADING = "reloading";
            DISABLED = "disabled";
            USE_WHILE_NULL = "&cCannot use this command while the plugin is {state}.";
            DEBUG_DESCRIPTION = "ZIP your settings for reporting bugs.";
            DEBUG_PREPARING = "&6Preparing debug log...";
            DEBUG_SUCCESS = "&2Successfuly copied {amount} file(s) to debug.zip. Your sensitive MySQL information has been removed from yml files. Please upload it via uploadfiles.io and send it to us for review.";
            DEBUG_COPY_FAIL = "&cCopying files failed on file {file} and it was stopped. See console for more information.";
            DEBUG_ZIP_FAIL = "&cCreating a ZIP of your files failed, see console for more information. Please ZIP debug/ folder and send it to us via uploadfiles.io manually.";
            PERMS_DESCRIPTION = "List all permissions the plugin has.";
            PERMS_USAGE = "[phrase]";
            PERMS_HEADER = "Listing All {plugin_name} Permissions";
            PERMS_MAIN = "Main";
            PERMS_PERMISSIONS = "Permissions:";
            PERMS_TRUE_BY_DEFAULT = "&7[true by default]";
            PERMS_INFO = "&7Info: &f";
            PERMS_DEFAULT = "&7Default? ";
            PERMS_APPLIED = "&7Do you have it? ";
            PERMS_YES = "&2yes";
            PERMS_NO = "&cno";
        }
    }
}