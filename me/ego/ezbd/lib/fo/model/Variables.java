package me.ego.ezbd.lib.fo.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.GeoAPI;
import me.ego.ezbd.lib.fo.Messenger;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.TimeUtil;
import me.ego.ezbd.lib.fo.GeoAPI.GeoResponse;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import me.ego.ezbd.lib.fo.collection.expiringmap.ExpiringMap;
import me.ego.ezbd.lib.fo.model.Variable.Type;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.settings.SimpleSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class Variables {
    public static final Pattern MESSAGE_PLACEHOLDER_PATTERN = Pattern.compile("[\\[]([^\\[\\]]+)[\\]]");
    public static final Pattern BRACKET_PLACEHOLDER_PATTERN = Pattern.compile("[({|%)]([^{}]+)[(}|%)]");
    public static final Pattern BRACKET_REL_PLACEHOLDER_PATTERN = Pattern.compile("[({|%)](rel_)([^}]+)[(}|%)]");
    private static final Map<String, Map<String, String>> cache;
    static boolean REPLACE_JAVASCRIPT;
    private static final StrictMap<String, Function<CommandSender, String>> customVariables;
    private static final StrictList<SimpleExpansion> customExpansions;

    public Variables() {
    }

    @Nullable
    public static Function<CommandSender, String> getVariable(String key) {
        return (Function)customVariables.get(key);
    }

    public static void addVariable(String variable, Function<CommandSender, String> replacer) {
        customVariables.override(variable, replacer);
    }

    public static void removeVariable(String variable) {
        customVariables.remove(variable);
    }

    public static boolean hasVariable(String variable) {
        return customVariables.contains(variable);
    }

    public static List<SimpleExpansion> getExpansions() {
        return Collections.unmodifiableList(customExpansions.getSource());
    }

    public static void addExpansion(SimpleExpansion expansion) {
        customExpansions.addIfNotExist(expansion);
    }

    public static void removeExpansion(SimpleExpansion expansion) {
        customExpansions.remove(expansion);
    }

    public static boolean hasExpansion(SimpleExpansion expansion) {
        return customExpansions.contains(expansion);
    }

    /** @deprecated */
    @Deprecated
    public static String replace(boolean replaceCustom, String message, CommandSender sender) {
        return replace(message, sender);
    }

    public static List<String> replace(Iterable<String> messages, @Nullable CommandSender sender, @Nullable Map<String, Object> replacements) {
        String deliminer = "%FLVJ%";
        return Arrays.asList(replace(String.join("%FLVJ%", messages), sender, replacements).split("%FLVJ%"));
    }

    public static String replace(String message, @Nullable CommandSender sender) {
        return replace((String)message, sender, (Map)null);
    }

    public static String replace(String message, @Nullable CommandSender sender, @Nullable Map<String, Object> replacements) {
        return replace(message, sender, replacements, true);
    }

    public static String replace(String message, @Nullable CommandSender sender, @Nullable Map<String, Object> replacements, boolean colorize) {
        if (message != null && !message.isEmpty()) {
            String original = message;
            boolean senderIsPlayer = sender instanceof Player;
            if (replacements != null && !replacements.isEmpty()) {
                message = Replacer.replaceArray(message, new Object[]{replacements});
            }

            Map map;
            if (senderIsPlayer) {
                map = (Map)cache.get(sender.getName());
                String cachedVar = map != null ? (String)map.get(message) : null;
                if (cachedVar != null) {
                    return cachedVar;
                }
            }

            if (REPLACE_JAVASCRIPT) {
                REPLACE_JAVASCRIPT = false;

                try {
                    message = replaceJavascriptVariables0(message, sender, replacements);
                } finally {
                    REPLACE_JAVASCRIPT = true;
                }
            }

            if (senderIsPlayer) {
                message = HookManager.replacePlaceholders((Player)sender, message);
            }

            message = replaceHardVariables0(sender, message);
            if (!message.startsWith("[JSON]")) {
                message = Common.colorize(message);
            }

            if (senderIsPlayer) {
                map = (Map)cache.get(sender.getName());
                if (map != null) {
                    map.put(original, message);
                } else {
                    cache.put(sender.getName(), Common.newHashMap(original, message));
                }
            }

            return message;
        } else {
            return "";
        }
    }

    private static String replaceJavascriptVariables0(String message, CommandSender sender, @Nullable Map<String, Object> replacements) {
        Matcher matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(message);

        while(matcher.find()) {
            String variableKey = matcher.group();
            Variable variable = Variable.findVariable(variableKey.substring(1, variableKey.length() - 1));
            if (variable != null && variable.getType() == Type.FORMAT) {
                SimpleComponent component = variable.build(sender, SimpleComponent.empty(), replacements);
                String plain = component.getPlainMessage();
                if (plain.startsWith("§f§f")) {
                    plain = plain.substring(4);
                }

                message = message.replace(variableKey, plain);
            }
        }

        return message;
    }

    private static String replaceHardVariables0(@Nullable CommandSender sender, String message) {
        Matcher matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(message);
        Player player = sender instanceof Player ? (Player)sender : null;

        while(true) {
            boolean frontSpace;
            boolean backSpace;
            String value;
            do {
                if (!matcher.find()) {
                    return message;
                }

                String variable = matcher.group(1);
                frontSpace = false;
                backSpace = false;
                if (variable.startsWith("+")) {
                    variable = variable.substring(1);
                    frontSpace = true;
                }

                if (variable.endsWith("+")) {
                    variable = variable.substring(0, variable.length() - 1);
                    backSpace = true;
                }

                value = lookupVariable0(player, sender, variable);
            } while(value == null);

            boolean emptyColorless = Common.stripColors(value).isEmpty();
            value = value.isEmpty() ? "" : (frontSpace && !emptyColorless ? " " : "") + Common.colorize(value) + (backSpace && !emptyColorless ? " " : "");
            message = message.replace(matcher.group(), value);
        }
    }

    private static String lookupVariable0(Player player, CommandSender console, String variable) {
        GeoResponse geoResponse = null;
        if (player != null && Arrays.asList("country_code", "country_name", "region_name", "isp").contains(variable)) {
            geoResponse = GeoAPI.getCountry(player.getAddress());
        }

        if (console != null) {
            Iterator var4 = customExpansions.iterator();

            while(var4.hasNext()) {
                SimpleExpansion expansion = (SimpleExpansion)var4.next();
                String value = expansion.replacePlaceholders(console, variable);
                if (value != null) {
                    return value;
                }
            }

            Function<CommandSender, String> customReplacer = (Function)customVariables.get(variable);
            if (customReplacer != null) {
                return (String)customReplacer.apply(console);
            }
        }

        byte var8 = -1;
        switch(variable.hashCode()) {
        case -2111645834:
            if (variable.equals("prefix_announce")) {
                var8 = 52;
            }
            break;
        case -1907930301:
            if (variable.equals("info_prefix")) {
                var8 = 41;
            }
            break;
        case -1770168479:
            if (variable.equals("sender_is_discord")) {
                var8 = 38;
            }
            break;
        case -1578285648:
            if (variable.equals("player_prefix")) {
                var8 = 21;
            }
            break;
        case -1565768458:
            if (variable.equals("region_name")) {
                var8 = 34;
            }
            break;
        case -1489597841:
            if (variable.equals("player_suffix")) {
                var8 = 23;
            }
            break;
        case -1456864472:
            if (variable.equals("announce_prefix")) {
                var8 = 51;
            }
            break;
        case -1255697231:
            if (variable.equals("pl_address")) {
                var8 = 30;
            }
            break;
        case -1221262756:
            if (variable.equals("health")) {
                var8 = 10;
            }
            break;
        case -1133418477:
            if (variable.equals("timestamp_short")) {
                var8 = 3;
            }
            break;
        case -1091888612:
            if (variable.equals("faction")) {
                var8 = 8;
            }
            break;
        case -1052618937:
            if (variable.equals("nation")) {
                var8 = 7;
            }
            break;
        case -985752863:
            if (variable.equals("player")) {
                var8 = 15;
            }
            break;
        case -936488674:
            if (variable.equals("plugin_prefix")) {
                var8 = 40;
            }
            break;
        case -907155211:
            if (variable.equals("tab_name")) {
                var8 = 17;
            }
            break;
        case -758770169:
            if (variable.equals("server_name")) {
                var8 = 0;
            }
            break;
        case -698197397:
            if (variable.equals("warn_prefix")) {
                var8 = 45;
            }
            break;
        case -684066824:
            if (variable.equals("player_vanished")) {
                var8 = 31;
            }
            break;
        case -542581236:
            if (variable.equals("sender_is_player")) {
                var8 = 37;
            }
            break;
        case 120:
            if (variable.equals("x")) {
                var8 = 12;
            }
            break;
        case 121:
            if (variable.equals("y")) {
                var8 = 13;
            }
            break;
        case 122:
            if (variable.equals("z")) {
                var8 = 14;
            }
            break;
        case 104582:
            if (variable.equals("isp")) {
                var8 = 35;
            }
            break;
        case 3381091:
            if (variable.equals("nick")) {
                var8 = 20;
            }
            break;
        case 3566226:
            if (variable.equals("town")) {
                var8 = 6;
            }
            break;
        case 55126294:
            if (variable.equals("timestamp")) {
                var8 = 2;
            }
            break;
        case 79333281:
            if (variable.equals("player_group")) {
                var8 = 25;
            }
            break;
        case 102727412:
            if (variable.equals("label")) {
                var8 = 36;
            }
            break;
        case 113318802:
            if (variable.equals("world")) {
                var8 = 9;
            }
            break;
        case 312018089:
            if (variable.equals("error_prefix")) {
                var8 = 47;
            }
            break;
        case 327874126:
            if (variable.equals("success_prefix")) {
                var8 = 43;
            }
            break;
        case 401878581:
            if (variable.equals("pl_prefix")) {
                var8 = 22;
            }
            break;
        case 490566388:
            if (variable.equals("pl_suffix")) {
                var8 = 24;
            }
            break;
        case 556940585:
            if (variable.equals("player_name")) {
                var8 = 16;
            }
            break;
        case 556947969:
            if (variable.equals("player_nick")) {
                var8 = 19;
            }
            break;
        case 787886619:
            if (variable.equals("prefix_error")) {
                var8 = 48;
            }
            break;
        case 856614742:
            if (variable.equals("prefix_success")) {
                var8 = 44;
            }
            break;
        case 969710125:
            if (variable.equals("nms_version")) {
                var8 = 1;
            }
            break;
        case 1411003963:
            if (variable.equals("prefix_info")) {
                var8 = 42;
            }
            break;
        case 1411408915:
            if (variable.equals("prefix_warn")) {
                var8 = 46;
            }
            break;
        case 1480014044:
            if (variable.equals("ip_address")) {
                var8 = 29;
            }
            break;
        case 1481071862:
            if (variable.equals("country_code")) {
                var8 = 32;
            }
            break;
        case 1481386388:
            if (variable.equals("country_name")) {
                var8 = 33;
            }
            break;
        case 1580471551:
            if (variable.equals("pl_primary_group")) {
                var8 = 28;
            }
            break;
        case 1604015602:
            if (variable.equals("chat_line_smooth")) {
                var8 = 5;
            }
            break;
        case 1615086568:
            if (variable.equals("display_name")) {
                var8 = 18;
            }
            break;
        case 1619864699:
            if (variable.equals("chat_line")) {
                var8 = 4;
            }
            break;
        case 1667071731:
            if (variable.equals("prefix_question")) {
                var8 = 50;
            }
            break;
        case 1804928908:
            if (variable.equals("sender_is_console")) {
                var8 = 39;
            }
            break;
        case 1805777532:
            if (variable.equals("pl_group")) {
                var8 = 26;
            }
            break;
        case 1877501195:
            if (variable.equals("question_prefix")) {
                var8 = 49;
            }
            break;
        case 1901043637:
            if (variable.equals("location")) {
                var8 = 11;
            }
            break;
        case 2119413028:
            if (variable.equals("player_primary_group")) {
                var8 = 27;
            }
        }

        switch(var8) {
        case 0:
            return Remain.getServerName();
        case 1:
            return MinecraftVersion.getServerVersion();
        case 2:
            return SimpleSettings.TIMESTAMP_FORMAT.format(System.currentTimeMillis());
        case 3:
            return TimeUtil.getFormattedDateShort();
        case 4:
            return Common.chatLine();
        case 5:
            return Common.chatLineSmooth();
        case 6:
            return player == null ? "" : HookManager.getTownName(player);
        case 7:
            return player == null ? "" : HookManager.getNation(player);
        case 8:
            return player == null ? "" : HookManager.getFaction(player);
        case 9:
            return player == null ? "" : HookManager.getWorldAlias(player.getWorld());
        case 10:
            return player == null ? "" : formatHealth0(player) + ChatColor.RESET;
        case 11:
            return player == null ? "" : Common.shortLocation(player.getLocation());
        case 12:
            return player == null ? "" : String.valueOf(player.getLocation().getBlockX());
        case 13:
            return player == null ? "" : String.valueOf(player.getLocation().getBlockY());
        case 14:
            return player == null ? "" : String.valueOf(player.getLocation().getBlockZ());
        case 15:
        case 16:
            return player == null ? Common.resolveSenderName(console) : player.getName();
        case 17:
            return player == null ? Common.resolveSenderName(console) : player.getPlayerListName();
        case 18:
            return player == null ? Common.resolveSenderName(console) : player.getDisplayName();
        case 19:
        case 20:
            return player == null ? Common.resolveSenderName(console) : HookManager.getNickColored(player);
        case 21:
        case 22:
            return player == null ? "" : HookManager.getPlayerPrefix(player);
        case 23:
        case 24:
            return player == null ? "" : HookManager.getPlayerSuffix(player);
        case 25:
        case 26:
            return player == null ? "" : HookManager.getPlayerPermissionGroup(player);
        case 27:
        case 28:
            return player == null ? "" : HookManager.getPlayerPrimaryGroup(player);
        case 29:
        case 30:
            return player == null ? "" : formatIp0(player);
        case 31:
            return player == null ? "false" : String.valueOf(PlayerUtil.isVanished(player));
        case 32:
            return player == null ? "" : geoResponse.getCountryCode();
        case 33:
            return player == null ? "" : geoResponse.getCountryName();
        case 34:
            return player == null ? "" : geoResponse.getRegionName();
        case 35:
            return player == null ? "" : geoResponse.getIsp();
        case 36:
            return SimplePlugin.getInstance().getMainCommand() != null ? SimplePlugin.getInstance().getMainCommand().getLabel() : "noMainCommandLabel";
        case 37:
            return player != null ? "true" : "false";
        case 38:
            return console instanceof DiscordSender ? "true" : "false";
        case 39:
            return console instanceof ConsoleCommandSender ? "true" : "false";
        case 40:
            return SimpleSettings.PLUGIN_PREFIX;
        case 41:
        case 42:
            return Messenger.getInfoPrefix();
        case 43:
        case 44:
            return Messenger.getSuccessPrefix();
        case 45:
        case 46:
            return Messenger.getWarnPrefix();
        case 47:
        case 48:
            return Messenger.getErrorPrefix();
        case 49:
        case 50:
            return Messenger.getQuestionPrefix();
        case 51:
        case 52:
            return Messenger.getAnnouncePrefix();
        default:
            return null;
        }
    }

    private static String formatHealth0(Player player) {
        int hp = Remain.getHealth(player);
        return (hp > 10 ? ChatColor.DARK_GREEN : (hp > 5 ? ChatColor.GOLD : ChatColor.RED)) + "" + hp;
    }

    private static String formatIp0(Player player) {
        try {
            return player.getAddress().toString().split("\\:")[0];
        } catch (Throwable var2) {
            return player.getAddress() != null ? player.getAddress().toString() : "";
        }
    }

    static {
        cache = ExpiringMap.builder().expiration(10L, TimeUnit.MILLISECONDS).build();
        REPLACE_JAVASCRIPT = true;
        customVariables = new StrictMap();
        customExpansions = new StrictList();
    }
}