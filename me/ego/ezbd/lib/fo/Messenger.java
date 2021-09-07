package me.ego.ezbd.lib.fo;

import java.util.Iterator;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Messenger {
    public static boolean ENABLED = false;
    private static String infoPrefix = "&8&l[&9&li&8&l]&7 ";
    private static String successPrefix = "&8&l[&2&l✔&8&l]&7 ";
    private static String warnPrefix = "&8&l[&6&l!&8&l]&6 ";
    private static String errorPrefix = "&8&l[&4&l✕&8&l]&c ";
    private static String questionPrefix = "&8&l[&a&l?&l&8]&7 ";
    private static String announcePrefix = "&8&l[&5&l!&l&8]&d ";

    public static void broadcastInfo(String message) {
        Iterator var1 = Remain.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player online = (Player)var1.next();
            tell(online, infoPrefix, message);
        }

    }

    public static void broadcastSuccess(String message) {
        Iterator var1 = Remain.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player online = (Player)var1.next();
            tell(online, successPrefix, message);
        }

    }

    public static void broadcastWarn(String message) {
        Iterator var1 = Remain.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player online = (Player)var1.next();
            tell(online, warnPrefix, message);
        }

    }

    public static void broadcastError(String message) {
        Iterator var1 = Remain.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player online = (Player)var1.next();
            tell(online, errorPrefix, message);
        }

    }

    public static void broadcastQuestion(String message) {
        Iterator var1 = Remain.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player online = (Player)var1.next();
            tell(online, questionPrefix, message);
        }

    }

    public static void broadcastAnnounce(String message) {
        Iterator var1 = Remain.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player online = (Player)var1.next();
            tell(online, announcePrefix, message);
        }

    }

    public static void info(CommandSender player, String message) {
        tell(player, infoPrefix, message);
    }

    public static void success(CommandSender player, String message) {
        tell(player, successPrefix, message);
    }

    public static void warn(CommandSender player, String message) {
        tell(player, warnPrefix, message);
    }

    public static void error(CommandSender player, String... messages) {
        String[] var2 = messages;
        int var3 = messages.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String message = var2[var4];
            error(player, message);
        }

    }

    public static void error(CommandSender player, String message) {
        tell(player, errorPrefix, message);
    }

    public static void question(CommandSender player, String message) {
        tell(player, questionPrefix, message);
    }

    public static void announce(CommandSender player, String message) {
        tell(player, announcePrefix, message);
    }

    private static void tell(CommandSender player, String prefix, String message) {
        if (!message.isEmpty() && !"none".equals(message)) {
            String colorless = Common.stripColors(message);
            boolean foundElements = ChatUtil.isInteractive(colorless);
            if (colorless.startsWith("<actionbar>")) {
                message = message.replace("<actionbar>", "<actionbar>" + prefix);
            }

            Common.tellNoPrefix(player, new String[]{(foundElements ? "" : prefix) + message});
        }
    }

    private Messenger() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void setInfoPrefix(String infoPrefix) {
        Messenger.infoPrefix = infoPrefix;
    }

    public static String getInfoPrefix() {
        return infoPrefix;
    }

    public static void setSuccessPrefix(String successPrefix) {
        Messenger.successPrefix = successPrefix;
    }

    public static String getSuccessPrefix() {
        return successPrefix;
    }

    public static void setWarnPrefix(String warnPrefix) {
        Messenger.warnPrefix = warnPrefix;
    }

    public static String getWarnPrefix() {
        return warnPrefix;
    }

    public static void setErrorPrefix(String errorPrefix) {
        Messenger.errorPrefix = errorPrefix;
    }

    public static String getErrorPrefix() {
        return errorPrefix;
    }

    public static void setQuestionPrefix(String questionPrefix) {
        Messenger.questionPrefix = questionPrefix;
    }

    public static String getQuestionPrefix() {
        return questionPrefix;
    }

    public static void setAnnouncePrefix(String announcePrefix) {
        Messenger.announcePrefix = announcePrefix;
    }

    public static String getAnnouncePrefix() {
        return announcePrefix;
    }
}