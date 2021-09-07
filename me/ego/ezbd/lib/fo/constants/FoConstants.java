package me.ego.ezbd.lib.fo.constants;

import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.TimeUtil;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;

public final class FoConstants {
    public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public FoConstants() {
    }

    public static final class NBT {
        public static final String TAG = SimplePlugin.getNamed() + "_NbtTag";
        public static final String TAG_MENU_CURRENT = SimplePlugin.getNamed() + "_Menu";
        public static final String TAG_MENU_PREVIOUS = SimplePlugin.getNamed() + "_Previous_Menu";

        public NBT() {
        }
    }

    public static final class Header {
        public static final String[] DATA_FILE = new String[]{"", "This file stores various data you create via the plugin.", "", " ** THE FILE IS MACHINE GENERATED. PLEASE DO NOT EDIT **", ""};
        public static final String[] UPDATED_FILE = new String[]{Common.configLine(), "", " Your file has been automatically updated at " + TimeUtil.getFormattedDate(), " to " + SimplePlugin.getNamed() + " " + SimplePlugin.getVersion(), "", " Unfortunatelly, due to how Bukkit saves all .yml files, it was not possible", " preserve the documentation comments in your file. We apologize.", "", " If you'd like to view the default file, you can either:", " a) Open the " + SimplePlugin.getSource().getName() + " with a WinRar or similar", " b) or, visit: https://github.com/kangarko/" + SimplePlugin.getNamed() + "/wiki", "", Common.configLine(), ""};

        public Header() {
        }
    }

    public static final class File {
        public static final String SETTINGS = "settings.yml";
        public static final String ERRORS = "error.log";
        public static final String DEBUG = "debug.log";
        public static final String DATA = "data.db";

        public File() {
        }

        public static final class ChatControl {
            public static final String COMMAND_SPY = "logs/command-spy.log";
            public static final String CHAT_LOG = "logs/chat.log";
            public static final String ADMIN_CHAT = "logs/admin-chat.log";
            public static final String BUNGEE_CHAT = "logs/bungee-chat.log";
            public static final String RULES_LOG = "logs/rules.log";
            public static final String CONSOLE_LOG = "logs/console.log";
            public static final String CHANNEL_JOINS = "logs/channel-joins.log";

            public ChatControl() {
            }
        }
    }
}
