package me.ego.ezbd.lib.fo.model;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

class PlaceholderAPIHook {
    private static volatile PlaceholderAPIHook.VariablesInjector injector;

    PlaceholderAPIHook() {
        try {
            injector = new PlaceholderAPIHook.VariablesInjector();
            injector.register();
        } catch (Throwable var2) {
            Common.error(var2, new String[]{"Failed to inject our variables into PlaceholderAPI!"});
        }

    }

    final void unregister() {
        if (injector != null) {
            try {
                injector.unregister();
            } catch (Throwable var2) {
            }
        }

    }

    final String replacePlaceholders(Player pl, String msg) {
        try {
            return this.setPlaceholders(pl, msg);
        } catch (Throwable var4) {
            Common.error(var4, new String[]{"PlaceholderAPI failed to replace variables!", "Player: " + pl.getName(), "Message: " + msg, "Error: %error"});
            return msg;
        }
    }

    private String setPlaceholders(Player player, String text) {
        Map<String, PlaceholderHook> hooks = PlaceholderAPI.getPlaceholders();
        if (hooks.isEmpty()) {
            return text;
        } else {
            Matcher matcher = Variables.BRACKET_PLACEHOLDER_PATTERN.matcher(text);

            while(matcher.find()) {
                String format = matcher.group(1);
                boolean frontSpace = false;
                boolean backSpace = false;
                if (format.startsWith("+")) {
                    frontSpace = true;
                    format = format.substring(1);
                }

                if (format.endsWith("+")) {
                    backSpace = true;
                    format = format.substring(0, format.length() - 1);
                }

                int index = format.indexOf("_");
                if (index > 0 && index < format.length()) {
                    String identifier = format.substring(0, index);
                    String params = format.substring(index + 1);
                    if (hooks.containsKey(identifier)) {
                        Thread currentThread = Thread.currentThread();
                        BukkitTask watchDog = Common.runLater(20, () -> {
                            Common.logFramed(new String[]{"IMPORTANT: PREVENTED SERVER CRASH FROM PLACEHOLDERAPI", "Replacing a variable using PlaceholderAPI took", "longer than our maximum limit (1 second) and", "was forcefully interrupted to prevent your", "server from crashing. This is not error on", "our end, please contact the expansion author.", "", "Variable: " + identifier, "Player: " + player.getName()});
                            currentThread.stop();
                        });
                        String value = ((PlaceholderHook)hooks.get(identifier)).onRequest(player, params);
                        watchDog.cancel();
                        if (value != null) {
                            value = Matcher.quoteReplacement(Common.colorize(value));
                            text = text.replaceAll(Pattern.quote(matcher.group()), value.isEmpty() ? "" : (frontSpace ? " " : "") + value + (backSpace ? " " : ""));
                        }
                    }
                }
            }

            return text;
        }
    }

    final String replaceRelationPlaceholders(Player one, Player two, String message) {
        try {
            return this.setRelationalPlaceholders(one, two, message);
        } catch (Throwable var5) {
            Common.error(var5, new String[]{"PlaceholderAPI failed to replace relation variables!", "Player one: " + one, "Player two: " + two, "Message: " + message, "Error: %error"});
            return message;
        }
    }

    private String setRelationalPlaceholders(Player one, Player two, String text) {
        Map<String, PlaceholderHook> hooks = PlaceholderAPI.getPlaceholders();
        if (hooks.isEmpty()) {
            return text;
        } else {
            Matcher matcher = Variables.BRACKET_REL_PLACEHOLDER_PATTERN.matcher(text);

            while(true) {
                String identifier;
                String params;
                do {
                    do {
                        String format;
                        int index;
                        do {
                            do {
                                if (!matcher.find()) {
                                    return text;
                                }

                                format = matcher.group(2);
                                index = format.indexOf("_");
                            } while(index <= 0);
                        } while(index >= format.length());

                        identifier = format.substring(0, index);
                        params = format.substring(index + 1);
                    } while(!hooks.containsKey(identifier));
                } while(!(hooks.get(identifier) instanceof Relational));

                Relational rel = (Relational)hooks.get(identifier);
                String value = one != null && two != null ? rel.onPlaceholderRequest(one, two, params) : "";
                if (value != null) {
                    text = text.replaceAll(Pattern.quote(matcher.group()), Matcher.quoteReplacement(Common.colorize(value)));
                }
            }
        }
    }

    private class VariablesInjector extends PlaceholderExpansion {
        private VariablesInjector() {
        }

        public boolean persist() {
            return true;
        }

        public boolean canRegister() {
            return true;
        }

        public String getAuthor() {
            return SimplePlugin.getInstance().getDescription().getAuthors().toString();
        }

        public String getIdentifier() {
            return SimplePlugin.getNamed().toLowerCase().replace("%", "").replace(" ", "").replace("_", "");
        }

        public String getVersion() {
            return SimplePlugin.getInstance().getDescription().getVersion();
        }

        public String onRequest(OfflinePlayer offlinePlayer, @NonNull String identifier) {
            if (identifier == null) {
                throw new NullPointerException("identifier is marked non-null but is null");
            } else {
                Player player = offlinePlayer != null ? offlinePlayer.getPlayer() : null;
                if (player != null && player.isOnline()) {
                    boolean frontSpace = identifier.startsWith("+");
                    boolean backSpace = identifier.endsWith("+");
                    identifier = frontSpace ? identifier.substring(1) : identifier;
                    identifier = backSpace ? identifier.substring(0, identifier.length() - 1) : identifier;
                    Function variable = Variables.getVariable(identifier);

                    try {
                        if (variable != null) {
                            String value = (String)variable.apply(player);
                            if (value != null) {
                                return value;
                            }
                        }

                        Iterator var12 = Variables.getExpansions().iterator();

                        while(var12.hasNext()) {
                            SimpleExpansion expansion = (SimpleExpansion)var12.next();
                            String valuex = expansion.replacePlaceholders(player, identifier);
                            if (valuex != null) {
                                boolean emptyColorless = Common.stripColors(valuex).isEmpty();
                                return (!valuex.isEmpty() && frontSpace && !emptyColorless ? " " : "") + valuex + (!valuex.isEmpty() && backSpace && !emptyColorless ? " " : "");
                            }
                        }
                    } catch (Exception var11) {
                        Common.error(var11, new String[]{"Error replacing PlaceholderAPI variables", "Identifier: " + identifier, "Player: " + player.getName()});
                    }

                    return null;
                } else {
                    return null;
                }
            }
        }
    }
}