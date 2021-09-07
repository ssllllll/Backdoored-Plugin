package me.ego.ezbd.lib.fo.settings;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.JavaScriptExecutor;
import me.ego.ezbd.lib.fo.model.SimpleComponent;

public final class SimpleLang extends YamlConfig {
    private static volatile SimpleLang instance;

    public static void setInstance(String filePath) {
        instance = new SimpleLang(filePath);
    }

    public static void setInstance() {
        instance = new SimpleLang("localization/messages_" + SimpleSettings.LOCALE_PREFIX + ".yml");
    }

    private SimpleLang(String path) {
        this.loadConfiguration(path);
    }

    protected boolean saveComments() {
        return true;
    }

    private String getStringStrict(String path) {
        String key = this.getString(path);
        Valid.checkNotNull(key, "Missing localization key '" + path + "' from " + this.getFileName());
        return key;
    }

    public static void reloadFile() {
        synchronized(instance) {
            instance.reload();
        }
    }

    public static boolean getOption(String path) {
        return instance.getBoolean(path);
    }

    public static List<SimpleComponent> ofComponentList(String path, @Nullable Object... variables) {
        return Common.convert(ofList(path, variables), (item) -> {
            return SimpleComponent.of(item);
        });
    }

    public static List<String> ofList(String path, @Nullable Object... variables) {
        return Arrays.asList(ofArray(path, variables));
    }

    public static String[] ofArray(String path, @Nullable Object... variables) {
        return of(path, variables).split("\n");
    }

    public static SimpleComponent ofComponent(String path, @Nullable Object... variables) {
        return SimpleComponent.of(of(path, variables));
    }

    public static String ofCase(long amount, String path) {
        return amount + " " + ofCaseNoAmount(amount, path);
    }

    public static String ofCaseNoAmount(long amount, String path) {
        String key = of(path);
        String[] split = key.split(", ");
        Valid.checkBoolean(split.length == 1 || split.length == 2, "Invalid syntax of key at '" + path + "', this key is a special one and it needs singular and plural form separated with , such as: second, seconds", new Object[0]);
        String singular = split[0];
        String plural = split[split.length == 2 ? 1 : 0];
        return amount != 0L && amount <= 1L ? singular : plural;
    }

    public static String ofScript(String path, SerializedMap scriptVariables, @Nullable Object... variables) {
        String script = of(path, variables);
        if (!script.contains("?") && !script.contains(":") && !script.contains("+") && !script.startsWith("'") && !script.endsWith("'")) {
            script = "'" + script + "'";
        }

        Object result;
        try {
            result = JavaScriptExecutor.run(script, scriptVariables.asMap());
        } catch (Throwable var6) {
            throw new FoException(var6, "Failed to compile localization key '" + path + "' with script: " + script + " (this must be a valid JavaScript code)");
        }

        return result.toString();
    }

    public static String of(String path, @Nullable Object... variables) {
        synchronized(instance) {
            String key = instance.getStringStrict(path);
            return translate(key, variables);
        }
    }

    private static String translate(String key, @Nullable Object... variables) {
        if (variables != null) {
            for(int i = 0; i < variables.length; ++i) {
                Object variable = variables[i];
                variable = Common.getOrDefaultStrict(SerializeUtil.serialize(variable), "");
                Valid.checkNotNull("Failed to replace {" + i + "} as " + variable + "(raw = " + variables[i] + ")");
                key = key.replace("{" + i + "}", variable.toString());
            }
        }

        return key;
    }
}