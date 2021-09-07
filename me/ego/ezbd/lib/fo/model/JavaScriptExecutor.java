package me.ego.ezbd.lib.fo.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.collection.expiringmap.ExpiringMap;
import me.ego.ezbd.lib.fo.exception.EventHandledException;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public final class JavaScriptExecutor {
    private static final ScriptEngine engine;
    private static final Map<UUID, Map<String, Object>> resultCache;

    public JavaScriptExecutor() {
    }

    public static Object run(String javascript) {
        return run(javascript, (CommandSender)null, (Event)null);
    }

    public static Object run(String javascript, CommandSender sender) {
        return run(javascript, sender, (Event)null);
    }

    public static Object run(@NonNull String javascript, CommandSender sender, Event event) {
        if (javascript == null) {
            throw new NullPointerException("javascript is marked non-null but is null");
        } else {
            Map<String, Object> cached = sender instanceof Player ? (Map)resultCache.get(((Player)sender).getUniqueId()) : null;
            Object result;
            if (cached != null) {
                result = ((Map)cached).get(javascript);
                if (result != null) {
                    return result;
                }
            }

            if (engine == null) {
                Common.log(new String[]{"Warning: Not running script for " + sender.getName() + " because JavaScript library is missing (install Oracle Java 8 or 11): " + javascript});
                return null;
            } else {
                try {
                    engine.getBindings(100).clear();
                    if (sender != null) {
                        engine.put("player", sender);
                    }

                    if (event != null) {
                        engine.put("event", event);
                    }

                    if (sender instanceof DiscordSender) {
                        for(Matcher matcher = Variables.BRACKET_PLACEHOLDER_PATTERN.matcher(javascript); matcher.find(); javascript = javascript.replace(matcher.group(), "false")) {
                        }
                    }

                    result = engine.eval(javascript);
                    if (sender instanceof Player) {
                        if (cached == null) {
                            cached = new HashMap();
                        }

                        ((Map)cached).put(javascript, result);
                        resultCache.put(((Player)sender).getUniqueId(), cached);
                    }

                    return result;
                } catch (Throwable var9) {
                    String message = var9.toString();
                    String error = "Script execution failed for";
                    if (message.contains("ReferenceError:") && message.contains("is not defined")) {
                        error = "Found invalid or unparsed variable in";
                    }

                    String cause = var9.getCause().toString();
                    if (var9.getCause() != null && cause.contains("event handled")) {
                        String[] errorMessageSplit = cause.contains("event handled: ") ? cause.split("event handled\\: ") : new String[0];
                        if (errorMessageSplit.length == 2) {
                            Common.tellNoPrefix(sender, new String[]{errorMessageSplit[1]});
                        }

                        throw new EventHandledException(true, new String[0]);
                    } else {
                        throw new RuntimeException(error + " '" + javascript + "'", var9);
                    }
                }
            }
        }
    }

    public static Object run(String javascript, Map<String, Object> replacements) {
        if (engine == null) {
            Common.log(new String[]{"Warning: Not running script because JavaScript library is missing (install Oracle Java 8 or 11): " + javascript});
            return javascript;
        } else {
            try {
                engine.getBindings(100).clear();
                if (replacements != null) {
                    Iterator var2 = replacements.entrySet().iterator();

                    while(var2.hasNext()) {
                        Entry<String, Object> replacement = (Entry)var2.next();
                        engine.put((String)replacement.getKey(), replacement.getValue());
                    }
                }

                return engine.eval(javascript);
            } catch (ScriptException var4) {
                throw new RuntimeException("Script execution failed for '" + javascript + "'", var4);
            }
        }
    }

    static {
        resultCache = ExpiringMap.builder().expiration(1L, TimeUnit.SECONDS).build();
        Thread.currentThread().setContextClassLoader(SimplePlugin.class.getClassLoader());
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = engineManager.getEngineByName("Nashorn");
        if (scriptEngine == null) {
            engineManager = new ScriptEngineManager((ClassLoader)null);
            scriptEngine = engineManager.getEngineByName("Nashorn");
        }

        if (scriptEngine == null) {
            String nashorn = "org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory";
            if (ReflectionUtil.isClassAvailable("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory")) {
                ScriptEngineFactory engineFactory = (ScriptEngineFactory)ReflectionUtil.instantiate(ReflectionUtil.lookupClass("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory"));
                engineManager.registerEngineName("Nashorn", engineFactory);
                scriptEngine = engineManager.getEngineByName("Nashorn");
            }
        }

        engine = scriptEngine;
        if (engine == null) {
            List<String> warningMessage = Common.newList(new String[]{"ERROR: JavaScript placeholders will not function!", "", "Your Java version/distribution lacks the", "Nashorn library for JavaScript placeholders."});
            if (Remain.getJavaVersion() >= 15) {
                warningMessage.addAll(Arrays.asList("", "To fix this, install the NashornPlus", "plugin from mineacademy.org/nashorn"));
            } else {
                warningMessage.addAll(Arrays.asList("", "To fix this, install Java 11 from Oracle", "or other vendor that supports Nashorn."));
            }

            Common.logFramed(false, Common.toArray(warningMessage));
        }

    }
}