package me.ego.ezbd.lib.fo;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.exception.RegexTimeoutException;
import me.ego.ezbd.lib.fo.model.DiscordSender;
import me.ego.ezbd.lib.fo.model.HookManager;
import me.ego.ezbd.lib.fo.model.Replacer;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompChatColor;
import me.ego.ezbd.lib.fo.remain.CompRunnable;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.remain.CompRunnable.SafeRunnable;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization;
import me.ego.ezbd.lib.fo.settings.SimpleSettings;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class Common {
    private static final Pattern COLOR_AND_DECORATION_REGEX = Pattern.compile("(&|§)[0-9a-fk-orA-FK-OR]");
    public static final Pattern RGB_HEX_COLOR_REGEX = Pattern.compile("(?<!\\\\)#((?:[0-9a-fA-F]{3}){1,2})");
    public static final Pattern RGB_HEX_BRACKET_COLOR_REGEX = Pattern.compile("\\{#((?:[0-9a-fA-F]{3}){1,2})\\}");
    private static final Pattern RGB_X_COLOR_REGEX = Pattern.compile("(§x)(§[0-9a-fA-F]){6}");
    private static final CommandSender CONSOLE_SENDER = Bukkit.getServer() != null ? Bukkit.getServer().getConsoleSender() : null;
    private static final Map<String, Long> TIMED_TELL_CACHE = new HashMap();
    private static final Map<String, Long> TIMED_LOG_CACHE = new HashMap();
    public static boolean ADD_TELL_PREFIX = false;
    public static boolean ADD_LOG_PREFIX = true;
    private static String tellPrefix = "[" + SimplePlugin.getNamed() + "]";
    private static String logPrefix = "[" + SimplePlugin.getNamed() + "]";

    public static void setTellPrefix(String prefix) {
        tellPrefix = colorize(prefix);
    }

    public static void setLogPrefix(String prefix) {
        logPrefix = colorize(prefix);
    }

    public static void broadcastReplaced(String message, Object... replacements) {
        broadcast(Replacer.replaceArray(message, replacements));
    }

    public static void broadcast(String message, CommandSender sender) {
        broadcast(message, resolveSenderName(sender));
    }

    public static void broadcast(String message, String playerReplacement) {
        broadcast(message.replace("{player}", playerReplacement));
    }

    public static void broadcast(String... messages) {
        if (!Valid.isNullOrEmpty(messages)) {
            String[] var1 = messages;
            int var2 = messages.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                String message = var1[var3];
                Iterator var5 = Remain.getOnlinePlayers().iterator();

                while(var5.hasNext()) {
                    Player online = (Player)var5.next();
                    tellJson(online, message);
                }

                log(message);
            }
        }

    }

    public static void broadcastTo(Iterable<? extends CommandSender> recipients, String... messages) {
        Iterator var2 = recipients.iterator();

        while(var2.hasNext()) {
            CommandSender sender = (CommandSender)var2.next();
            tell(sender, messages);
        }

    }

    public static void broadcastWithPerm(String showPermission, String message, boolean log) {
        if (message != null && !message.equals("none")) {
            Iterator var3 = Remain.getOnlinePlayers().iterator();

            while(var3.hasNext()) {
                Player online = (Player)var3.next();
                if (PlayerUtil.hasPerm(online, showPermission)) {
                    tellJson(online, message);
                }
            }

            if (log) {
                log(message);
            }
        }

    }

    public static void broadcastWithPerm(String permission, @NonNull TextComponent message, boolean log) {
        if (message == null) {
            throw new NullPointerException("message is marked non-null but is null");
        } else {
            String legacy = message.toLegacyText();
            if (!legacy.equals("none")) {
                Iterator var4 = Remain.getOnlinePlayers().iterator();

                while(var4.hasNext()) {
                    Player online = (Player)var4.next();
                    if (PlayerUtil.hasPerm(online, permission)) {
                        Remain.sendComponent(online, message);
                    }
                }

                if (log) {
                    log(legacy);
                }
            }

        }
    }

    public static void tellTimedNoPrefix(int delaySeconds, CommandSender sender, String message) {
        boolean hadPrefix = ADD_TELL_PREFIX;
        ADD_TELL_PREFIX = false;
        tellTimed(delaySeconds, sender, message);
        ADD_TELL_PREFIX = hadPrefix;
    }

    public static void tellTimed(int delaySeconds, CommandSender sender, String message) {
        if (!TIMED_TELL_CACHE.containsKey(message)) {
            tell(sender, message);
            TIMED_TELL_CACHE.put(message, TimeUtil.currentTimeSeconds());
        } else {
            if (TimeUtil.currentTimeSeconds() - (Long)TIMED_TELL_CACHE.get(message) > (long)delaySeconds) {
                tell(sender, message);
                TIMED_TELL_CACHE.put(message, TimeUtil.currentTimeSeconds());
            }

        }
    }

    public static void tellLaterConversing(int delayTicks, Conversable conversable, String message) {
        runLater(delayTicks, () -> {
            tellConversing(conversable, message);
        });
    }

    public static void tellConversing(Conversable conversable, String message) {
        conversable.sendRawMessage(colorize((ADD_TELL_PREFIX ? tellPrefix : "") + removeFirstSpaces(message)).trim());
    }

    public static void tellLater(int delayTicks, CommandSender sender, String... messages) {
        runLater(delayTicks, () -> {
            if (!(sender instanceof Player) || ((Player)sender).isOnline()) {
                tell(sender, messages);
            }
        });
    }

    public static void tellNoPrefix(CommandSender sender, Replacer replacer) {
        tellNoPrefix(sender, replacer.getReplacedMessage());
    }

    public static void tellNoPrefix(CommandSender sender, String... messages) {
        boolean was = ADD_TELL_PREFIX;
        ADD_TELL_PREFIX = false;
        tell(sender, messages);
        ADD_TELL_PREFIX = was;
    }

    public static void tell(CommandSender sender, Collection<String> messages) {
        tell(sender, toArray(messages));
    }

    public static void tell(CommandSender sender, String... messages) {
        String[] var2 = messages;
        int var3 = messages.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String message = var2[var4];
            if (message != null && !"none".equals(message)) {
                tellJson(sender, message);
            }
        }

    }

    public static void tellReplaced(CommandSender recipient, String message, Object... replacements) {
        tell(recipient, Replacer.replaceArray(message, replacements));
    }

    private static void tellJson(@NonNull CommandSender sender, String message) {
        if (sender == null) {
            throw new NullPointerException("sender is marked non-null but is null");
        } else if (!message.isEmpty() && !"none".equals(message)) {
            boolean hasPrefix = message.contains("{prefix}");
            boolean hasJSON = message.startsWith("[JSON]");
            message = message.replace("{player}", resolveSenderName(sender));
            if (!hasJSON) {
                message = colorize(message);
            }

            String colorlessMessage = stripColors(message);
            String stripped;
            if (hasJSON) {
                stripped = message.substring(6).trim();
                if (!stripped.isEmpty()) {
                    Remain.sendJson(sender, stripped);
                }
            } else if (colorlessMessage.startsWith("<actionbar>")) {
                stripped = message.replace("<actionbar>", "");
                if (!stripped.isEmpty()) {
                    if (sender instanceof Player) {
                        Remain.sendActionBar((Player)sender, stripped);
                    } else {
                        tellJson(sender, stripped);
                    }
                }
            } else if (colorlessMessage.startsWith("<toast>")) {
                stripped = message.replace("<toast>", "");
                if (!stripped.isEmpty()) {
                    if (sender instanceof Player) {
                        Remain.sendToast((Player)sender, stripped);
                    } else {
                        tellJson(sender, stripped);
                    }
                }
            } else {
                String part;
                if (colorlessMessage.startsWith("<title>")) {
                    stripped = message.replace("<title>", "");
                    if (!stripped.isEmpty()) {
                        String[] split = stripped.split("\\|");
                        String title = split[0];
                        part = split.length > 1 ? joinRange(1, split) : null;
                        if (sender instanceof Player) {
                            Remain.sendTitle((Player)sender, title, part);
                        } else {
                            tellJson(sender, title);
                            if (part != null) {
                                tellJson(sender, part);
                            }
                        }
                    }
                } else if (colorlessMessage.startsWith("<bossbar>")) {
                    stripped = message.replace("<bossbar>", "");
                    if (!stripped.isEmpty()) {
                        if (sender instanceof Player) {
                            Remain.sendBossbarTimed((Player)sender, stripped, 10);
                        } else {
                            tellJson(sender, stripped);
                        }
                    }
                } else {
                    String[] var13 = splitNewline(message);
                    int var12 = var13.length;

                    for(int var14 = 0; var14 < var12; ++var14) {
                        part = var13[var14];
                        String prefixStripped = removeSurroundingSpaces(tellPrefix);
                        String prefix = ADD_TELL_PREFIX && !hasPrefix && !prefixStripped.isEmpty() ? prefixStripped + " " : "";
                        String toSend;
                        if (stripColors(part).startsWith("<center>")) {
                            toSend = ChatUtil.center(prefix + part.replace("<center>", ""));
                        } else {
                            toSend = prefix + part;
                        }

                        if (MinecraftVersion.olderThan(V.v1_9) && toSend.length() + 1 >= 32767) {
                            toSend = toSend.substring(0, 16383);
                            log("Warning: Message to " + sender.getName() + " was too large, sending the first 16,000 letters: " + toSend);
                        }

                        if (sender instanceof Conversable && ((Conversable)sender).isConversing()) {
                            ((Conversable)sender).sendRawMessage(toSend);
                        } else {
                            sender.sendMessage(toSend);
                        }
                    }
                }
            }

        }
    }

    public static String resolveSenderName(CommandSender sender) {
        return !(sender instanceof Player) && !(sender instanceof DiscordSender) ? SimpleLocalization.CONSOLE_NAME : sender.getName();
    }

    private static String removeFirstSpaces(String message) {
        for(message = getOrEmpty(message); message.startsWith(" "); message = message.substring(1)) {
        }

        return message;
    }

    public static List<String> colorize(List<String> list) {
        List<String> copy = new ArrayList();
        copy.addAll(list);

        for(int i = 0; i < copy.size(); ++i) {
            String message = (String)copy.get(i);
            if (message != null) {
                copy.set(i, colorize(message));
            }
        }

        return copy;
    }

    public static String colorize(String... messages) {
        return colorize(StringUtils.join(messages, "\n"));
    }

    public static String[] colorizeArray(String... messages) {
        for(int i = 0; i < messages.length; ++i) {
            messages[i] = colorize(messages[i]);
        }

        return messages;
    }

    public static String colorize(String message) {
        if (message != null && !message.isEmpty()) {
            String result = ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", message.startsWith(tellPrefix) ? "" : removeSurroundingSpaces(tellPrefix.trim())).replace("{server}", SimpleLocalization.SERVER_PREFIX).replace("{plugin_name}", SimplePlugin.getNamed()).replace("{plugin_version}", SimplePlugin.getVersion()));
            if (MinecraftVersion.atLeast(V.v1_16)) {
                Matcher match;
                String colorCode;
                String replacement;
                for(match = RGB_HEX_BRACKET_COLOR_REGEX.matcher(result); match.find(); result = result.replaceAll("\\{#" + colorCode + "\\}", replacement)) {
                    colorCode = match.group(1);
                    replacement = "";

                    try {
                        replacement = CompChatColor.of("#" + colorCode).toString();
                    } catch (IllegalArgumentException var7) {
                    }
                }

                for(match = RGB_HEX_COLOR_REGEX.matcher(result); match.find(); result = result.replaceAll("#" + colorCode, replacement)) {
                    colorCode = match.group(1);
                    replacement = "";

                    try {
                        replacement = CompChatColor.of("#" + colorCode).toString();
                    } catch (IllegalArgumentException var6) {
                    }
                }

                result = result.replace("\\#", "#");
            }

            return result;
        } else {
            return "";
        }
    }

    private static String removeSurroundingSpaces(String message) {
        for(message = getOrEmpty(message); message.endsWith(" "); message = message.substring(0, message.length() - 1)) {
        }

        return removeFirstSpaces(message);
    }

    public static String[] revertColorizing(String[] messages) {
        for(int i = 0; i < messages.length; ++i) {
            messages[i] = revertColorizing(messages[i]);
        }

        return messages;
    }

    public static String revertColorizing(String message) {
        return message.replaceAll("(?i)§([0-9a-fk-or])", "&$1");
    }

    public static String stripColors(String message) {
        if (message != null && !message.isEmpty()) {
            Matcher matcher;
            for(matcher = COLOR_AND_DECORATION_REGEX.matcher(message); matcher.find(); message = matcher.replaceAll("")) {
            }

            if (Remain.hasHexColors()) {
                for(matcher = RGB_HEX_COLOR_REGEX.matcher(message); matcher.find(); message = matcher.replaceAll("")) {
                }

                for(matcher = RGB_X_COLOR_REGEX.matcher(message); matcher.find(); message = matcher.replaceAll("")) {
                }

                message = message.replace("§x", "");
            }

            return message;
        } else {
            return message;
        }
    }

    public static String stripColorsLetter(String message) {
        return message == null ? "" : message.replaceAll("&([0-9a-fk-orA-F-K-OR])", "");
    }

    public static boolean hasColors(String message) {
        return COLOR_AND_DECORATION_REGEX.matcher(message).find();
    }

    public static String lastColor(String message) {
        if (MinecraftVersion.atLeast(V.v1_16)) {
            int c = message.lastIndexOf(167);
            Matcher match = RGB_X_COLOR_REGEX.matcher(message);

            String lastColor;
            for(lastColor = null; match.find(); lastColor = match.group(0)) {
            }

            if (lastColor != null && (c == -1 || c < message.lastIndexOf(lastColor) + lastColor.length())) {
                return lastColor;
            }
        }

        String andLetter = lastColorLetter(message);
        String colorChat = lastColorChar(message);
        return !andLetter.isEmpty() ? andLetter : (!colorChat.isEmpty() ? colorChat : "");
    }

    public static String lastColorLetter(String message) {
        return lastColor(message, '&');
    }

    public static String lastColorChar(String message) {
        return lastColor(message, '§');
    }

    private static String lastColor(String msg, char colorChar) {
        int c = msg.lastIndexOf(colorChar);
        if (c != -1) {
            return msg.length() > c + 1 && msg.substring(c + 1, c + 2).matches("([0-9a-fk-or])") ? msg.substring(c, c + 2).trim() : lastColor(msg.substring(0, c), colorChar);
        } else {
            return "";
        }
    }

    public static String consoleLine() {
        return "!-----------------------------------------------------!";
    }

    public static String consoleLineSmooth() {
        return "______________________________________________________________";
    }

    public static String chatLine() {
        return "*---------------------------------------------------*";
    }

    public static String chatLineSmooth() {
        return "&m-----------------------------------------------------";
    }

    public static String configLine() {
        return "-------------------------------------------------------------------------------------------";
    }

    public static String scoreboardLine(int length) {
        String fill = "";

        for(int i = 0; i < length; ++i) {
            fill = fill + "-";
        }

        return "&m|" + fill + "|";
    }

    public static String plural(long count, String ofWhat) {
        String exception = getException(count, ofWhat);
        return exception != null ? exception : count + " " + ofWhat + (count != 0L && (count <= 1L || ofWhat.endsWith("s")) ? "" : "s");
    }

    public static String pluralEs(long count, String ofWhat) {
        String exception = getException(count, ofWhat);
        return exception != null ? exception : count + " " + ofWhat + (count != 0L && (count <= 1L || ofWhat.endsWith("es")) ? "" : "es");
    }

    public static String pluralIes(long count, String ofWhat) {
        String exception = getException(count, ofWhat);
        return exception != null ? exception : count + " " + (count != 0L && (count <= 1L || ofWhat.endsWith("ies")) ? ofWhat : ofWhat.substring(0, ofWhat.length() - 1) + "ies");
    }

    /** @deprecated */
    @Deprecated
    private static String getException(long count, String ofWhat) {
        SerializedMap exceptions = SerializedMap.ofArray(new Object[]{"life", "lives", "class", "classes", "wolf", "wolves", "knife", "knives", "wife", "wives", "calf", "calves", "leaf", "leaves", "potato", "potatoes", "tomato", "tomatoes", "hero", "heroes", "torpedo", "torpedoes", "veto", "vetoes", "foot", "feet", "tooth", "teeth", "goose", "geese", "man", "men", "woman", "women", "mouse", "mice", "die", "dice", "ox", "oxen", "child", "children", "person", "people", "penny", "pence", "sheep", "sheep", "fish", "fish", "deer", "deer", "moose", "moose", "swine", "swine", "buffalo", "buffalo", "shrimp", "shrimp", "trout", "trout", "spacecraft", "spacecraft", "cactus", "cacti", "axis", "axes", "analysis", "analyses", "crisis", "crises", "thesis", "theses", "datum", "data", "index", "indices", "entry", "entries", "boss", "bosses"});
        return exceptions.containsKey(ofWhat) ? count + " " + (count != 0L && count <= 1L ? ofWhat : exceptions.getString(ofWhat)) : null;
    }

    /** @deprecated */
    @Deprecated
    public static String article(String ofWhat) {
        Valid.checkBoolean(ofWhat.length() > 0, "String cannot be empty", new Object[0]);
        List<String> syllables = Arrays.asList("a", "e", "i", "o", "u", "y");
        return (syllables.contains(ofWhat.toLowerCase().trim().substring(0, 1)) ? "an" : "a") + " " + ofWhat;
    }

    public static String fancyBar(int min, char minChar, int max, char maxChar, ChatColor delimiterColor) {
        String formatted = "";

        int i;
        for(i = 0; i < min; ++i) {
            formatted = formatted + minChar;
        }

        formatted = formatted + delimiterColor;

        for(i = 0; i < max - min; ++i) {
            formatted = formatted + maxChar;
        }

        return formatted;
    }

    public static String shortLocation(Vector vec) {
        return " [" + MathUtil.formatOneDigit(vec.getX()) + ", " + MathUtil.formatOneDigit(vec.getY()) + ", " + MathUtil.formatOneDigit(vec.getZ()) + "]";
    }

    public static String shortLocation(Location loc) {
        if (loc == null) {
            return "Location(null)";
        } else if (loc.equals(new Location((World)null, 0.0D, 0.0D, 0.0D))) {
            return "Location(null, 0, 0, 0)";
        } else {
            Valid.checkNotNull(loc.getWorld(), "Cannot shorten a location with null world!");
            return loc.getWorld().getName() + " [" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
        }
    }

    public static String duplicate(String text, int nTimes) {
        if (nTimes == 0) {
            return "";
        } else {
            String toDuplicate = new String(text);

            for(int i = 1; i < nTimes; ++i) {
                text = text + toDuplicate;
            }

            return text;
        }
    }

    public static String limit(String text, int maxLength) {
        int length = text.length();
        return maxLength >= length ? text : text.substring(0, maxLength) + "...";
    }

    /** @deprecated */
    @Deprecated
    public static boolean doesPluginExistSilently(String pluginName) {
        return doesPluginExist(pluginName);
    }

    public static boolean doesPluginExist(String pluginName) {
        Plugin lookup = null;
        Plugin[] var2 = Bukkit.getPluginManager().getPlugins();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Plugin otherPlugin = var2[var4];
            if (otherPlugin.getName().equals(pluginName)) {
                lookup = otherPlugin;
                break;
            }
        }

        if (lookup == null) {
            return false;
        } else {
            if (!lookup.isEnabled()) {
                runLaterAsync(0, () -> {
                    Valid.checkBoolean(lookup.isEnabled(), SimplePlugin.getNamed() + " could not hook into " + pluginName + " as the plugin is disabled! (DO NOT REPORT THIS TO " + SimplePlugin.getNamed() + ", look for errors above and contact support of '" + pluginName + "')", new Object[0]);
                });
            }

            return true;
        }
    }

    public static void dispatchCommand(@Nullable CommandSender playerReplacement, @NonNull String command) {
        if (command == null) {
            throw new NullPointerException("command is marked non-null but is null");
        } else if (!command.isEmpty() && !command.equalsIgnoreCase("none")) {
            if (command.startsWith("@announce ")) {
                Messenger.announce(playerReplacement, command.replace("@announce ", ""));
            } else if (command.startsWith("@warn ")) {
                Messenger.warn(playerReplacement, command.replace("@warn ", ""));
            } else if (command.startsWith("@error ")) {
                Messenger.error(playerReplacement, command.replace("@error ", ""));
            } else if (command.startsWith("@info ")) {
                Messenger.info(playerReplacement, command.replace("@info ", ""));
            } else if (command.startsWith("@question ")) {
                Messenger.question(playerReplacement, command.replace("@question ", ""));
            } else if (command.startsWith("@success ")) {
                Messenger.success(playerReplacement, command.replace("@success ", ""));
            } else {
                command = command.startsWith("/") ? command.substring(1) : command;
                command = command.replace("{player}", playerReplacement == null ? "" : resolveSenderName(playerReplacement));
                if (!command.startsWith("tellraw")) {
                    command = colorize(command);
                }

                runLater(() -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                });
            }

        }
    }

    public static void dispatchCommandAsPlayer(@NonNull Player playerSender, @NonNull String command) {
        if (playerSender == null) {
            throw new NullPointerException("playerSender is marked non-null but is null");
        } else if (command == null) {
            throw new NullPointerException("command is marked non-null but is null");
        } else if (!command.isEmpty() && !command.equalsIgnoreCase("none")) {
            runLater(() -> {
                playerSender.performCommand(colorize(command.replace("{player}", resolveSenderName(playerSender))));
            });
        }
    }

    public static void logTimed(int delaySec, String msg) {
        if (!TIMED_LOG_CACHE.containsKey(msg)) {
            log(msg);
            TIMED_LOG_CACHE.put(msg, TimeUtil.currentTimeSeconds());
        } else {
            if (TimeUtil.currentTimeSeconds() - (Long)TIMED_LOG_CACHE.get(msg) > (long)delaySec) {
                log(msg);
                TIMED_LOG_CACHE.put(msg, TimeUtil.currentTimeSeconds());
            }

        }
    }

    public static void logF(String format, @NonNull Object... args) {
        if (args == null) {
            throw new NullPointerException("args is marked non-null but is null");
        } else {
            String formatted = format(format, args);
            log(false, formatted);
        }
    }

    public static String format(String format, @NonNull Object... args) {
        if (args == null) {
            throw new NullPointerException("args is marked non-null but is null");
        } else {
            for(int i = 0; i < args.length; ++i) {
                Object arg = args[i];
                if (arg != null) {
                    args[i] = simplify(arg);
                }
            }

            return String.format(format, args);
        }
    }

    public static void log(List<String> messages) {
        log(toArray(messages));
    }

    public static void log(String... messages) {
        log(true, messages);
    }

    public static void logNoPrefix(String... messages) {
        log(false, messages);
    }

    private static void log(boolean addLogPrefix, String... messages) {
        if (messages != null) {
            String[] var2 = messages;
            int var3 = messages.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String message = var2[var4];
                if (!message.equals("none")) {
                    if (stripColors(message).replace(" ", "").isEmpty()) {
                        if (CONSOLE_SENDER == null) {
                            System.out.println(" ");
                        } else {
                            CONSOLE_SENDER.sendMessage("  ");
                        }
                    } else {
                        message = colorize(message);
                        if (message.startsWith("[JSON]")) {
                            String stripped = message.replaceFirst("\\[JSON\\]", "").trim();
                            if (!stripped.isEmpty()) {
                                log(Remain.toLegacyText(stripped, false));
                            }
                        } else {
                            String[] var6 = splitNewline(message);
                            int var7 = var6.length;

                            for(int var8 = 0; var8 < var7; ++var8) {
                                String part = var6[var8];
                                String log = ((addLogPrefix && ADD_LOG_PREFIX ? removeSurroundingSpaces(logPrefix) + " " : "") + getOrEmpty(part).replace("\n", colorize("\n&r"))).trim();
                                if (CONSOLE_SENDER != null) {
                                    CONSOLE_SENDER.sendMessage(log);
                                } else {
                                    System.out.println("[" + SimplePlugin.getNamed() + "] " + stripColors(log));
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    public static void logFramed(String... messages) {
        logFramed(false, messages);
    }

    public static void logFramed(boolean disablePlugin, String... messages) {
        if (messages != null && !Valid.isNullOrEmpty(messages)) {
            log("&7" + consoleLine());
            String[] var2 = messages;
            int var3 = messages.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String msg = var2[var4];
                log(" &c" + msg);
            }

            if (disablePlugin) {
                log(" &cPlugin is now disabled.");
            }

            log("&7" + consoleLine());
        }

        if (disablePlugin) {
            Bukkit.getPluginManager().disablePlugin(SimplePlugin.getInstance());
        }

    }

    public static void error(Throwable t, String... messages) {
        if (!(t instanceof FoException)) {
            Debugger.saveError(t, messages);
        }

        Debugger.printStackTrace(t);
        logFramed(replaceErrorVariable(t, messages));
    }

    public static void throwError(Throwable t, String... messages) {
        while(t.getCause() != null) {
            t = t.getCause();
        }

        if (t instanceof FoException) {
            throw (FoException)t;
        } else {
            if (messages != null) {
                logFramed(false, replaceErrorVariable(t, messages));
            }

            Debugger.saveError(t, messages);
            Remain.sneaky(t);
        }
    }

    private static String[] replaceErrorVariable(Throwable throwable, String... msgs) {
        while(throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        String throwableName = throwable == null ? "Unknown error." : throwable.getClass().getSimpleName();
        String throwableMessage = throwable != null && throwable.getMessage() != null && !throwable.getMessage().isEmpty() ? ": " + throwable.getMessage() : "";

        for(int i = 0; i < msgs.length; ++i) {
            String error = throwableName + throwableMessage;
            msgs[i] = msgs[i].replace("%error%", error).replace("%error", error);
        }

        return msgs;
    }

    public static boolean regExMatch(String regex, String message) {
        return regExMatch(compilePattern(regex), message);
    }

    public static boolean regExMatch(Pattern regex, String message) {
        return regExMatch(compileMatcher(regex, message));
    }

    public static boolean regExMatch(Matcher matcher) {
        Valid.checkNotNull(matcher, "Cannot call regExMatch on null matcher");

        try {
            return matcher.find();
        } catch (RegexTimeoutException var2) {
            handleRegexTimeoutException(var2, matcher.pattern());
            return false;
        }
    }

    public static Matcher compileMatcher(@NonNull Pattern pattern, String message) {
        if (pattern == null) {
            throw new NullPointerException("pattern is marked non-null but is null");
        } else {
            try {
                String strippedMessage = SimplePlugin.getInstance().regexStripColors() ? stripColors(message) : message;
                strippedMessage = SimplePlugin.getInstance().regexStripAccents() ? ChatUtil.replaceDiacritic(strippedMessage) : strippedMessage;
                return pattern.matcher(TimedCharSequence.withSettingsLimit(strippedMessage));
            } catch (RegexTimeoutException var3) {
                handleRegexTimeoutException(var3, pattern);
                return null;
            }
        }
    }

    public static Matcher compileMatcher(String regex, String message) {
        return compileMatcher(compilePattern(regex), message);
    }

    public static Pattern compilePattern(String regex) {
        SimplePlugin instance = SimplePlugin.getInstance();
        Pattern pattern = null;
        regex = SimplePlugin.getInstance().regexStripColors() ? stripColors(regex) : regex;
        regex = SimplePlugin.getInstance().regexStripAccents() ? ChatUtil.replaceDiacritic(regex) : regex;

        try {
            if (instance.regexCaseInsensitive()) {
                pattern = Pattern.compile(regex, instance.regexUnicode() ? 66 : 2);
            } else {
                pattern = instance.regexUnicode() ? Pattern.compile(regex, 64) : Pattern.compile(regex);
            }

            return pattern;
        } catch (PatternSyntaxException var4) {
            throwError(var4, "Your regular expression is malformed!", "Expression: '" + regex + "'", "", "IF YOU CREATED IT YOURSELF, we unfortunately", "can't provide support for custom expressions.", "Use online services like regex101.com to put your", "expression there (without '') and discover where", "the syntax error lays and how to fix it.");
            return null;
        }
    }

    public static void handleRegexTimeoutException(RegexTimeoutException ex, Pattern pattern) {
        boolean caseInsensitive = SimplePlugin.getInstance().regexCaseInsensitive();
        error(ex, "A regular expression took too long to process, and was", "stopped to prevent freezing your server.", " ", "Limit " + SimpleSettings.REGEX_TIMEOUT + "ms ", "Expression: '" + (pattern == null ? "unknown" : pattern.pattern()) + "'", "Evaluated message: '" + ex.getCheckedMessage() + "'", " ", "IF YOU CREATED THAT RULE YOURSELF, we unfortunately", "can't provide support for custom expressions.", " ", "Sometimes, all you need doing is increasing timeout", "limit in your settings.yml", " ", "Use services like regex101.com to test and fix it.", "Put the expression without '' and the message there.", "Ensure to turn flags 'insensitive' and 'unicode' " + (caseInsensitive ? "on" : "off"), "on there when testing: https://i.imgur.com/PRR5Rfn.png");
    }

    @SafeVarargs
    public static <T> List<T> joinArrays(Iterable<T>... arrays) {
        List<T> all = new ArrayList();
        Iterable[] var2 = arrays;
        int var3 = arrays.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Iterable<T> array = var2[var4];
            Iterator var6 = array.iterator();

            while(var6.hasNext()) {
                T element = var6.next();
                all.add(element);
            }
        }

        return all;
    }

    public static <T extends CommandSender> String joinPlayersExcept(Iterable<T> array, String nameToIgnore) {
        Iterator<T> it = array.iterator();
        String message = "";

        while(it.hasNext()) {
            T next = (CommandSender)it.next();
            if (!next.getName().equals(nameToIgnore)) {
                message = message + next.getName() + (it.hasNext() ? ", " : "");
            }
        }

        return message.endsWith(", ") ? message.substring(0, message.length() - 2) : message;
    }

    public static String joinRange(int startIndex, String[] array) {
        return joinRange(startIndex, array.length, array);
    }

    public static String joinRange(int startIndex, int stopIndex, String[] array) {
        return joinRange(startIndex, stopIndex, array, " ");
    }

    public static String joinRange(int start, int stop, String[] array, String delimiter) {
        String joined = "";

        for(int i = start; i < MathUtil.range(stop, 0, array.length); ++i) {
            joined = joined + (joined.isEmpty() ? "" : delimiter) + array[i];
        }

        return joined;
    }

    public static <T> String join(T[] array) {
        return array == null ? "null" : join((Iterable)Arrays.asList(array));
    }

    public static <T> String join(Iterable<T> array) {
        return array == null ? "null" : join(array, ", ");
    }

    public static <T> String join(Iterable<T> array, String delimiter) {
        return join(array, delimiter, (object) -> {
            return object == null ? "" : simplify(object);
        });
    }

    public static <T> String join(T[] array, String delimiter, Common.Stringer<T> stringer) {
        Valid.checkNotNull(array, "Cannot join null array!");
        return join((Iterable)Arrays.asList(array), delimiter, stringer);
    }

    public static <T> String join(Iterable<T> array, String delimiter, Common.Stringer<T> stringer) {
        Iterator<T> it = array.iterator();
        String message = "";

        while(it.hasNext()) {
            T next = it.next();
            if (next != null) {
                message = message + stringer.toString(next) + (it.hasNext() ? delimiter : "");
            }
        }

        return message;
    }

    public static String simplify(Object arg) {
        if (arg instanceof Entity) {
            return Remain.getName((Entity)arg);
        } else if (arg instanceof CommandSender) {
            return ((CommandSender)arg).getName();
        } else if (arg instanceof World) {
            return ((World)arg).getName();
        } else if (arg instanceof Location) {
            return shortLocation((Location)arg);
        } else if (arg.getClass() != Double.TYPE && arg.getClass() != Float.TYPE) {
            if (arg instanceof Collection) {
                return join((Iterable)((Collection)arg), ", ", Common::simplify);
            } else if (arg instanceof ChatColor) {
                return ((Enum)arg).name().toLowerCase();
            } else if (arg instanceof CompChatColor) {
                return ((CompChatColor)arg).getName();
            } else if (arg instanceof Enum) {
                return ((Enum)arg).toString().toLowerCase();
            } else {
                try {
                    if (arg instanceof net.md_5.bungee.api.ChatColor) {
                        return ((net.md_5.bungee.api.ChatColor)arg).getName();
                    }
                } catch (Exception var2) {
                }

                return arg.toString();
            }
        } else {
            return MathUtil.formatTwoDigits((Double)arg);
        }
    }

    public static <T> Map<Integer, List<T>> fillPages(int cellSize, Iterable<T> items) {
        List<T> allItems = toList(items);
        Map<Integer, List<T>> pages = new HashMap();
        int pageCount = allItems.size() == cellSize ? 0 : allItems.size() / cellSize;

        for(int i = 0; i <= pageCount; ++i) {
            List<T> pageItems = new ArrayList();
            int down = cellSize * i;
            int up = down + cellSize;

            for(int valueIndex = down; valueIndex < up && valueIndex < allItems.size(); ++valueIndex) {
                T page = allItems.get(valueIndex);
                pageItems.add(page);
            }

            pages.put(i, pageItems);
        }

        return pages;
    }

    public static <T> T last(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }

    public static <T> T last(T[] array) {
        return array != null && array.length != 0 ? array[array.length - 1] : null;
    }

    public static List<String> getWorldNames() {
        return convert((Iterable)Bukkit.getWorlds(), (Common.TypeConverter)(World::getName));
    }

    public static List<String> getPlayerNames() {
        return getPlayerNames(true, (Player)null);
    }

    public static List<String> getPlayerNames(boolean includeVanished) {
        return getPlayerNames(includeVanished, (Player)null);
    }

    public static List<String> getPlayerNames(boolean includeVanished, @Nullable Player otherPlayer) {
        List<String> found = new ArrayList();
        Iterator var3 = Remain.getOnlinePlayers().iterator();

        while(true) {
            Player online;
            do {
                if (!var3.hasNext()) {
                    return found;
                }

                online = (Player)var3.next();
            } while(PlayerUtil.isVanished(online, otherPlayer) && !includeVanished);

            found.add(online.getName());
        }
    }

    public static List<String> getPlayerNicknames(boolean includeVanished) {
        return getPlayerNicknames(includeVanished, (Player)null);
    }

    public static List<String> getPlayerNicknames(boolean includeVanished, @Nullable Player otherPlayer) {
        List<String> found = new ArrayList();
        Iterator var3 = Remain.getOnlinePlayers().iterator();

        while(true) {
            Player online;
            do {
                if (!var3.hasNext()) {
                    return found;
                }

                online = (Player)var3.next();
            } while(PlayerUtil.isVanished(online, otherPlayer) && !includeVanished);

            found.add(HookManager.getNickColorless(online));
        }
    }

    public static <OLD, NEW> List<NEW> convert(Iterable<OLD> list, Common.TypeConverter<OLD, NEW> converter) {
        List<NEW> copy = new ArrayList();
        Iterator var3 = list.iterator();

        while(var3.hasNext()) {
            OLD old = var3.next();
            NEW result = converter.convert(old);
            if (result != null) {
                copy.add(converter.convert(old));
            }
        }

        return copy;
    }

    public static <OLD, NEW> Set<NEW> convertSet(Iterable<OLD> list, Common.TypeConverter<OLD, NEW> converter) {
        Set<NEW> copy = new HashSet();
        Iterator var3 = list.iterator();

        while(var3.hasNext()) {
            OLD old = var3.next();
            NEW result = converter.convert(old);
            if (result != null) {
                copy.add(converter.convert(old));
            }
        }

        return copy;
    }

    public static <OLD, NEW> StrictList<NEW> convertStrict(Iterable<OLD> list, Common.TypeConverter<OLD, NEW> converter) {
        StrictList<NEW> copy = new StrictList();
        Iterator var3 = list.iterator();

        while(var3.hasNext()) {
            OLD old = var3.next();
            copy.add(converter.convert(old));
        }

        return copy;
    }

    public static <OLD_KEY, OLD_VALUE, NEW_KEY, NEW_VALUE> Map<NEW_KEY, NEW_VALUE> convert(Map<OLD_KEY, OLD_VALUE> oldMap, Common.MapToMapConverter<OLD_KEY, OLD_VALUE, NEW_KEY, NEW_VALUE> converter) {
        Map<NEW_KEY, NEW_VALUE> newMap = new HashMap();
        oldMap.entrySet().forEach((e) -> {
            newMap.put(converter.convertKey(e.getKey()), converter.convertValue(e.getValue()));
        });
        return newMap;
    }

    public static <OLD_KEY, OLD_VALUE, NEW_KEY, NEW_VALUE> StrictMap<NEW_KEY, NEW_VALUE> convertStrict(Map<OLD_KEY, OLD_VALUE> oldMap, Common.MapToMapConverter<OLD_KEY, OLD_VALUE, NEW_KEY, NEW_VALUE> converter) {
        StrictMap<NEW_KEY, NEW_VALUE> newMap = new StrictMap();
        oldMap.entrySet().forEach((e) -> {
            newMap.put(converter.convertKey(e.getKey()), converter.convertValue(e.getValue()));
        });
        return newMap;
    }

    public static <LIST_KEY, OLD_KEY, OLD_VALUE> StrictList<LIST_KEY> convertToList(Map<OLD_KEY, OLD_VALUE> map, Common.MapToListConverter<LIST_KEY, OLD_KEY, OLD_VALUE> converter) {
        StrictList<LIST_KEY> list = new StrictList();
        Iterator var3 = map.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<OLD_KEY, OLD_VALUE> e = (Entry)var3.next();
            list.add(converter.convert(e.getKey(), e.getValue()));
        }

        return list;
    }

    public static <OLD_TYPE, NEW_TYPE> List<NEW_TYPE> convert(OLD_TYPE[] oldArray, Common.TypeConverter<OLD_TYPE, NEW_TYPE> converter) {
        List<NEW_TYPE> newList = new ArrayList();
        Object[] var3 = oldArray;
        int var4 = oldArray.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            OLD_TYPE old = var3[var5];
            newList.add(converter.convert(old));
        }

        return newList;
    }

    /** @deprecated */
    @Deprecated
    public static String[] splitNewline(String message) {
        if (!SimplePlugin.getInstance().enforeNewLine()) {
            return message.split("\n");
        } else {
            String delimiter = "KANGARKOJESUUPER";
            char[] chars = message.toCharArray();
            String parts = "";

            for(int i = 0; i < chars.length; ++i) {
                char c = chars[i];
                if ('\\' == c && i + 1 < chars.length && 'n' == chars[i + 1]) {
                    ++i;
                    parts = parts + "KANGARKOJESUUPER";
                } else {
                    parts = parts + c;
                }
            }

            return parts.split("KANGARKOJESUUPER");
        }
    }

    public static String[] split(String input, int maxLineLength) {
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());

        String word;
        for(int lineLen = 0; tok.hasMoreTokens(); lineLen += word.length()) {
            word = tok.nextToken();
            if (lineLen + word.length() > maxLineLength) {
                output.append("\n");
                lineLen = 0;
            }

            output.append(word);
        }

        return output.toString().split("\n");
    }

    public static <T> List<T> removeNullAndEmpty(T[] array) {
        return (List)(array != null ? removeNullAndEmpty(Arrays.asList(array)) : new ArrayList());
    }

    public static <T> List<T> removeNullAndEmpty(List<T> list) {
        List<T> copy = new ArrayList();
        Iterator var2 = list.iterator();

        while(var2.hasNext()) {
            T key = var2.next();
            if (key != null) {
                if (key instanceof String) {
                    if (!((String)key).isEmpty()) {
                        copy.add(key);
                    }
                } else {
                    copy.add(key);
                }
            }
        }

        return copy;
    }

    public static String[] replaceNullWithEmpty(String[] list) {
        for(int i = 0; i < list.length; ++i) {
            if (list[i] == null) {
                list[i] = "";
            }
        }

        return list;
    }

    public static <T> T getOrDefault(T[] array, int index, T def) {
        return index < array.length ? array[index] : def;
    }

    public static String getOrEmpty(String input) {
        return input != null && !"none".equalsIgnoreCase(input) ? input : "";
    }

    public static String getOrNull(String input) {
        return input != null && !"none".equalsIgnoreCase(input) && !input.isEmpty() ? input : null;
    }

    /** @deprecated */
    @Deprecated
    public static String getOrSupply(String value, String def) {
        return (String)getOrDefault(value, def);
    }

    public static <T> T getOrDefault(T value, T def) {
        return !(value instanceof String) || !"none".equalsIgnoreCase((String)value) && !"".equals(value) ? getOrDefaultStrict(value, def) : def;
    }

    public static <T> T getOrDefaultStrict(T value, T def) {
        return value != null ? value : def;
    }

    public static <T> T getNext(T given, List<T> list, boolean forward) {
        if (given == null && list.isEmpty()) {
            return null;
        } else {
            T[] array = (Object[])((Object[])Array.newInstance((given != null ? given : list.get(0)).getClass(), list.size()));

            for(int i = 0; i < list.size(); ++i) {
                Array.set(array, i, list.get(i));
            }

            return getNext(given, array, forward);
        }
    }

    public static <T> T getNext(T given, T[] array, boolean forward) {
        if (array.length == 0) {
            return null;
        } else {
            int index = 0;

            int nextIndex;
            for(nextIndex = 0; nextIndex < array.length; ++nextIndex) {
                T element = array[nextIndex];
                if (element.equals(given)) {
                    index = nextIndex;
                    break;
                }
            }

            if (index != -1) {
                nextIndex = index + (forward ? 1 : -1);
                return nextIndex >= array.length ? array[0] : (nextIndex < 0 ? array[array.length - 1] : array[nextIndex]);
            } else {
                return null;
            }
        }
    }

    public static String[] toArray(Collection<String> array) {
        return array == null ? new String[0] : (String[])array.toArray(new String[array.size()]);
    }

    public static <T> ArrayList<T> toList(T... array) {
        return array == null ? new ArrayList() : new ArrayList(Arrays.asList(array));
    }

    public static <T> List<T> toList(@Nullable Iterable<T> it) {
        List<T> list = new ArrayList();
        if (it != null) {
            it.forEach((el) -> {
                if (el != null) {
                    list.add(el);
                }

            });
        }

        return list;
    }

    public static <T> T[] reverse(T[] array) {
        if (array == null) {
            return null;
        } else {
            int i = 0;

            for(int j = array.length - 1; j > i; ++i) {
                T tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                --j;
            }

            return array;
        }
    }

    public static <A, B> Map<A, B> newHashMap(A firstKey, B firstValue) {
        Map<A, B> map = new HashMap();
        map.put(firstKey, firstValue);
        return map;
    }

    public static <T> Set<T> newSet(T... keys) {
        return new HashSet(Arrays.asList(keys));
    }

    public static <T> List<T> newList(T... keys) {
        List<T> list = new ArrayList();
        Object[] var2 = keys;
        int var3 = keys.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            T key = var2[var4];
            list.add(key);
        }

        return list;
    }

    public static <T extends Runnable> BukkitTask runLater(T task) {
        return runLater(1, task);
    }

    public static BukkitTask runLater(int delayTicks, Runnable task) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        JavaPlugin instance = SimplePlugin.getInstance();
        SafeRunnable task = new SafeRunnable(task);

        try {
            return runIfDisabled(task) ? null : (delayTicks == 0 ? (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTask(instance) : scheduler.runTask(instance, task)) : (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTaskLater(instance, (long)delayTicks) : scheduler.runTaskLater(instance, task, (long)delayTicks)));
        } catch (NoSuchMethodError var5) {
            return runIfDisabled(task) ? null : (delayTicks == 0 ? (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTask(instance) : getTaskFromId(scheduler.scheduleSyncDelayedTask(instance, task))) : (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTaskLater(instance, (long)delayTicks) : getTaskFromId(scheduler.scheduleSyncDelayedTask(instance, task, (long)delayTicks))));
        }
    }

    public static BukkitTask runAsync(Runnable task) {
        return runLaterAsync(0, task);
    }

    public static BukkitTask runLaterAsync(Runnable task) {
        return runLaterAsync(0, task);
    }

    public static BukkitTask runLaterAsync(int delayTicks, Runnable task) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        JavaPlugin instance = SimplePlugin.getInstance();
        SafeRunnable task = new SafeRunnable(task);

        try {
            return runIfDisabled(task) ? null : (delayTicks == 0 ? (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTaskAsynchronously(instance) : scheduler.runTaskAsynchronously(instance, task)) : (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTaskLaterAsynchronously(instance, (long)delayTicks) : scheduler.runTaskLaterAsynchronously(instance, task, (long)delayTicks)));
        } catch (NoSuchMethodError var5) {
            return runIfDisabled(task) ? null : (delayTicks == 0 ? (task instanceof CompRunnable ? ((CompRunnable)task).runTaskAsynchronously(instance) : getTaskFromId(scheduler.scheduleAsyncDelayedTask(instance, task))) : (task instanceof CompRunnable ? ((CompRunnable)task).runTaskLaterAsynchronously(instance, (long)delayTicks) : getTaskFromId(scheduler.scheduleAsyncDelayedTask(instance, task, (long)delayTicks))));
        }
    }

    public static BukkitTask runTimer(int repeatTicks, Runnable task) {
        return runTimer(0, repeatTicks, task);
    }

    public static BukkitTask runTimer(int delayTicks, int repeatTicks, Runnable task) {
        SafeRunnable task = new SafeRunnable(task);

        try {
            return runIfDisabled(task) ? null : (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTaskTimer(SimplePlugin.getInstance(), (long)delayTicks, (long)repeatTicks) : Bukkit.getScheduler().runTaskTimer(SimplePlugin.getInstance(), task, (long)delayTicks, (long)repeatTicks));
        } catch (NoSuchMethodError var4) {
            return runIfDisabled(task) ? null : (task instanceof CompRunnable ? ((CompRunnable)task).runTaskTimer(SimplePlugin.getInstance(), (long)delayTicks, (long)repeatTicks) : getTaskFromId(Bukkit.getScheduler().scheduleSyncRepeatingTask(SimplePlugin.getInstance(), task, (long)delayTicks, (long)repeatTicks)));
        }
    }

    public static BukkitTask runTimerAsync(int repeatTicks, Runnable task) {
        return runTimerAsync(0, repeatTicks, task);
    }

    public static BukkitTask runTimerAsync(int delayTicks, int repeatTicks, Runnable task) {
        SafeRunnable task = new SafeRunnable(task);

        try {
            return runIfDisabled(task) ? null : (task instanceof BukkitRunnable ? ((BukkitRunnable)task).runTaskTimerAsynchronously(SimplePlugin.getInstance(), (long)delayTicks, (long)repeatTicks) : Bukkit.getScheduler().runTaskTimerAsynchronously(SimplePlugin.getInstance(), task, (long)delayTicks, (long)repeatTicks));
        } catch (NoSuchMethodError var4) {
            return runIfDisabled(task) ? null : (task instanceof CompRunnable ? ((CompRunnable)task).runTaskTimerAsynchronously(SimplePlugin.getInstance(), (long)delayTicks, (long)repeatTicks) : getTaskFromId(Bukkit.getScheduler().scheduleAsyncRepeatingTask(SimplePlugin.getInstance(), task, (long)delayTicks, (long)repeatTicks)));
        }
    }

    private static BukkitTask getTaskFromId(int taskId) {
        Iterator var1 = Bukkit.getScheduler().getPendingTasks().iterator();

        BukkitTask task;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            task = (BukkitTask)var1.next();
        } while(task.getTaskId() != taskId);

        return task;
    }

    private static boolean runIfDisabled(Runnable run) {
        if (!SimplePlugin.getInstance().isEnabled()) {
            (new SafeRunnable(run)).run();
            return true;
        } else {
            return false;
        }
    }

    public static boolean callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
        return event instanceof Cancellable ? !((Cancellable)event).isCancelled() : true;
    }

    public static void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, SimplePlugin.getInstance());
    }

    public static Map<String, Object> getMapFromSection(@NonNull Object mapOrSection) {
        if (mapOrSection == null) {
            throw new NullPointerException("mapOrSection is marked non-null but is null");
        } else {
            Map<String, Object> map = mapOrSection instanceof Map ? (Map)mapOrSection : (mapOrSection instanceof MemorySection ? (Map)ReflectionUtil.getFieldContent(mapOrSection, "map") : null);
            Valid.checkNotNull(map, "Unexpected " + mapOrSection.getClass().getSimpleName() + " '" + mapOrSection + "'. Must be Map or MemorySection! (Do not just send config name here, but the actual section with get('section'))");
            return map;
        }
    }

    public static boolean isDomainReachable(String url, int timeout) {
        url = url.replaceFirst("^https", "http");

        try {
            HttpURLConnection c = (HttpURLConnection)(new URL(url)).openConnection();
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.setRequestMethod("HEAD");
            int responseCode = c.getResponseCode();
            return 200 <= responseCode && responseCode <= 399;
        } catch (IOException var4) {
            return false;
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep((long)millis);
        } catch (InterruptedException var2) {
            var2.printStackTrace();
        }

    }

    private Common() {
    }

    public static String getTellPrefix() {
        return tellPrefix;
    }

    public static String getLogPrefix() {
        return logPrefix;
    }

    public interface MapToMapConverter<A, B, C, D> {
        C convertKey(A var1);

        D convertValue(B var1);
    }

    public interface MapToListConverter<O, K, V> {
        O convert(K var1, V var2);
    }

    public interface TypeConverter<Old, New> {
        New convert(Old var1);
    }

    public interface Stringer<T> {
        String toString(T var1);
    }
}