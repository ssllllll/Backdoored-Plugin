package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.SerializeUtil.SerializeFailedException;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public final class Replacer {
    private static final String DELIMITER = "%D3L1M1T3R%";
    private final String[] messages;
    private String[] variables;
    private String[] replacedMessage;

    private Replacer(String... messages) {
        this.messages = messages;
    }

    /** @deprecated */
    @Deprecated
    public Replacer find(String... variables) {
        this.variables = variables;
        return this;
    }

    /** @deprecated */
    @Deprecated
    public String replaceAll(Object... associativeArray) {
        SerializedMap map = SerializedMap.ofArray(associativeArray);
        List<String> find = new ArrayList();
        List<Object> replaced = new ArrayList();
        Iterator var5 = map.entrySet().iterator();

        while(var5.hasNext()) {
            Entry<String, Object> entry = (Entry)var5.next();
            find.add(entry.getKey());
            replaced.add(entry.getValue());
        }

        this.find((String[])find.toArray(new String[find.size()]));
        this.replace(replaced.toArray(new Object[replaced.size()]));
        return this.getReplacedMessageJoined();
    }

    /** @deprecated */
    @Deprecated
    public Replacer replace(Object... replacements) {
        Valid.checkNotNull(this.variables, "call find() first");
        Valid.checkBoolean(replacements.length == this.variables.length, "Variables " + this.variables.length + " != replacements " + replacements.length, new Object[0]);
        String message = StringUtils.join(this.messages, "%D3L1M1T3R%");

        for(int i = 0; i < this.variables.length; ++i) {
            String find = this.variables[i];
            if (!find.startsWith("{")) {
                find = "{" + find;
            }

            if (!find.endsWith("}")) {
                find = find + "}";
            }

            Object rep = i < replacements.length ? replacements[i] : null;

            String serialized;
            try {
                serialized = Objects.toString(SerializeUtil.serialize(rep));
            } catch (SerializeFailedException var8) {
                serialized = rep.toString();
            }

            message = message.replace(find, rep != null ? serialized : "");
        }

        this.replacedMessage = message.split("%D3L1M1T3R%");
        return this;
    }

    /** @deprecated */
    @Deprecated
    public void tell(CommandSender recipient) {
        Valid.checkNotNull(this.replacedMessage, "Replaced message not yet set, use find() and replace()");
        Common.tell(recipient, this.replacedMessage);
    }

    /** @deprecated */
    @Deprecated
    public String getReplacedMessageJoined() {
        Valid.checkNotNull(this.replacedMessage, "Replaced message not yet set, use find() and replace()");
        return StringUtils.join(this.replacedMessage, " ");
    }

    /** @deprecated */
    @Deprecated
    public static Replacer of(String... messages) {
        return new Replacer(messages);
    }

    public static List<String> replaceArray(List<String> list, Object... replacements) {
        String joined = String.join("%FLPV%", list);
        joined = replaceArray(joined, replacements);
        return Arrays.asList(joined.split("%FLPV%"));
    }

    public static String replaceArray(String message, Object... replacements) {
        SerializedMap map = SerializedMap.ofArray(replacements);
        return replaceVariables(message, map);
    }

    public static List<String> replaceVariables(List<String> list, SerializedMap replacements) {
        String joined = String.join("%FLPV%", list);
        joined = replaceVariables(joined, replacements);
        return Arrays.asList(joined.split("%FLPV%"));
    }

    public static String replaceVariables(@Nullable String message, SerializedMap variables) {
        if (message == null) {
            return null;
        } else if ("".equals(message)) {
            return "";
        } else {
            Matcher matcher = Variables.BRACKET_PLACEHOLDER_PATTERN.matcher(message);

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

                    value = null;
                    Iterator var7 = variables.entrySet().iterator();

                    while(var7.hasNext()) {
                        Entry<String, Object> entry = (Entry)var7.next();
                        String variableKey = (String)entry.getKey();
                        variableKey = variableKey.startsWith("{") ? variableKey.substring(1) : variableKey;
                        variableKey = variableKey.endsWith("}") ? variableKey.substring(0, variableKey.length() - 1) : variableKey;
                        if (variableKey.equals(variable)) {
                            value = entry.getValue() == null ? "null" : entry.getValue().toString();
                        }
                    }
                } while(value == null);

                boolean emptyColorless = Common.stripColors(value).isEmpty();
                value = value.isEmpty() ? "" : (frontSpace && !emptyColorless ? " " : "") + Common.colorize(value) + (backSpace && !emptyColorless ? " " : "");
                message = message.replace(matcher.group(), value);
            }
        }
    }

    public Replacer(String[] messages, String[] variables, String[] replacedMessage) {
        this.messages = messages;
        this.variables = variables;
        this.replacedMessage = replacedMessage;
    }

    public String[] getReplacedMessage() {
        return this.replacedMessage;
    }
}