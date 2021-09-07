package me.ego.ezbd.lib.fo.debug;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.TimeUtil;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.settings.SimpleSettings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public final class Debugger {
    private static final Map<String, ArrayList<String>> pendingMessages = new HashMap();
    private static boolean debugModeEnabled = false;

    public static void detectDebugMode() {
        if ((new File(SimplePlugin.getData(), "debug.lock")).exists()) {
            debugModeEnabled = true;
            Bukkit.getLogger().info("Detected debug.lock file, debug features enabled!");
        } else {
            debugModeEnabled = false;
        }

    }

    public static void debug(String section, String... messages) {
        if (isDebugged(section)) {
            String[] var2 = messages;
            int var3 = messages.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String message = var2[var4];
                if (SimplePlugin.hasInstance()) {
                    Common.log(new String[]{"[" + section + "] " + message});
                } else {
                    System.out.println("[" + section + "] " + message);
                }
            }
        }

    }

    public static void put(String section, String message) {
        if (isDebugged(section)) {
            ArrayList<String> list = (ArrayList)pendingMessages.getOrDefault(section, new ArrayList());
            list.add(message);
            pendingMessages.put(section, list);
        }
    }

    public static void push(String section, String message) {
        put(section, message);
        push(section);
    }

    public static void push(String section) {
        if (isDebugged(section)) {
            List<String> parts = (List)pendingMessages.remove(section);
            if (parts != null) {
                String whole = StringUtils.join(parts, "");
                String[] var3 = whole.split("\n");
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    String message = var3[var5];
                    debug(section, message);
                }

            }
        }
    }

    public static boolean isDebugged(String section) {
        return SimpleSettings.DEBUG_SECTIONS.contains(section) || SimpleSettings.DEBUG_SECTIONS.contains("*");
    }

    public static void saveError(Throwable t, String... messages) {
        if (Bukkit.getServer() != null) {
            List<String> lines = new ArrayList();
            String header = SimplePlugin.getNamed() + " " + SimplePlugin.getVersion() + " encountered " + Common.article(t.getClass().getSimpleName());
            fill(lines, "------------------------------------[ " + TimeUtil.getFormattedDate() + " ]-----------------------------------", header, "Running " + Bukkit.getName() + " " + Bukkit.getBukkitVersion() + " and Java " + System.getProperty("java.version"), "Plugins: " + StringUtils.join(Bukkit.getPluginManager().getPlugins(), ", "), "----------------------------------------------------------------------------------------------");
            if (messages != null && !StringUtils.join(messages, "").isEmpty()) {
                fill(lines, "\nMore Information: ");
                fill(lines, messages);
            }

            do {
                fill(lines, t == null ? "Unknown error" : t.getClass().getSimpleName() + " " + (String)Common.getOrDefault(t.getMessage(), Common.getOrDefault(t.getLocalizedMessage(), "(Unknown cause)")));
                int count = 0;
                StackTraceElement[] var5 = t.getStackTrace();
                int var6 = var5.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    StackTraceElement el = var5[var7];
                    ++count;
                    String trace = el.toString();
                    if (!trace.contains("sun.reflect")) {
                        if (count > 6 && trace.startsWith("net.minecraft.server")) {
                            break;
                        }

                        fill(lines, "\t at " + el.toString());
                    }
                }
            } while((t = t.getCause()) != null);

            fill(lines, "----------------------------------------------------------------------------------------------", System.lineSeparator());
            Common.log(new String[]{header + "! Please check your error.log and report this issue with the information in that file."});
            FileUtil.write("error.log", lines);
        }
    }

    private static void fill(List<String> list, String... messages) {
        list.addAll(Arrays.asList(messages));
    }

    public static List<String> traceRoute(boolean trackLineNumbers) {
        Exception exception = new RuntimeException("I love horses");
        List<String> paths = new ArrayList();
        StackTraceElement[] var3 = exception.getStackTrace();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            StackTraceElement el = var3[var5];
            String[] classNames = el.getClassName().split("\\.");
            String className = classNames[classNames.length - 1];
            String line = el.toString();
            if (line.contains("net.minecraft.server") || line.contains("org.bukkit.craftbukkit")) {
                break;
            }

            if (!line.contains("org.bukkit.plugin.java.JavaPluginLoader") && !line.contains("org.bukkit.plugin.SimplePluginManager") && !line.contains("org.bukkit.plugin.JavaPlugin") && !paths.contains(className)) {
                paths.add(className + "#" + el.getMethodName() + (trackLineNumbers ? "(" + el.getLineNumber() + ")" : ""));
            }
        }

        if (!paths.isEmpty()) {
            paths.remove(0);
        }

        return paths;
    }

    public static void printValues(Object[] values) {
        if (values != null) {
            print(Common.consoleLine());
            print("Enumeration of " + Common.plural((long)values.length, values.getClass().getSimpleName().toLowerCase().replace("[]", "")));

            for(int i = 0; i < values.length; ++i) {
                print("&8[" + i + "] &7" + values[i]);
            }
        } else {
            print("Value are null");
        }

    }

    public static void printStackTrace(String message) {
        StackTraceElement[] trace = (new Exception()).getStackTrace();
        print("!----------------------------------------------------------------------------------------------------------!");
        print(message);
        print("!----------------------------------------------------------------------------------------------------------!");

        for(int i = 1; i < trace.length; ++i) {
            String line = trace[i].toString();
            if (canPrint(line)) {
                print("\tat " + line);
            }
        }

        print("--------------------------------------------------------------------------------------------------------end-");
    }

    public static void printStackTrace(@NonNull Throwable throwable) {
        if (throwable == null) {
            throw new NullPointerException("throwable is marked non-null but is null");
        } else {
            List<Throwable> causes = new ArrayList();
            Throwable lastCause;
            if (throwable.getCause() != null) {
                lastCause = throwable.getCause();

                do {
                    causes.add(lastCause);
                } while((lastCause = lastCause.getCause()) != null);
            }

            if (throwable instanceof FoException && !causes.isEmpty()) {
                print(throwable.getMessage());
            } else {
                print(throwable.toString());
                printStackTraceElements(throwable);
            }

            if (!causes.isEmpty()) {
                lastCause = (Throwable)causes.get(causes.size() - 1);
                print(lastCause.toString());
                printStackTraceElements(lastCause);
            }

        }
    }

    private static void printStackTraceElements(Throwable throwable) {
        StackTraceElement[] var1 = throwable.getStackTrace();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            StackTraceElement element = var1[var3];
            String line = element.toString();
            if (canPrint(line)) {
                print("\tat " + line);
            }
        }

    }

    private static boolean canPrint(String message) {
        return !message.contains("net.minecraft") && !message.contains("org.bukkit.craftbukkit") && !message.contains("nashorn") && !message.contains("javax.script") && !message.contains("org.yaml.snakeyaml");
    }

    private static void print(String message) {
        if (SimplePlugin.hasInstance()) {
            Common.logNoPrefix(new String[]{message});
        } else {
            System.out.println(message);
        }

    }

    private Debugger() {
    }

    public static boolean isDebugModeEnabled() {
        return debugModeEnabled;
    }
}
