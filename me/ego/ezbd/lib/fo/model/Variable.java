package me.ego.ezbd.lib.fo.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.settings.YamlConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class Variable extends YamlConfig {
    public static Function<String, String> PROTOTYPE_PATH = (t) -> {
        return NO_DEFAULT;
    };
    private static final ConfigItems<Variable> loadedVariables = ConfigItems.fromFolder("variable", "variables", Variable.class);
    private Variable.Type type;
    private String key;
    private String value;
    @Nullable
    private String senderCondition;
    @Nullable
    private String receiverCondition;
    @Nullable
    private String senderPermission;
    @Nullable
    private String receiverPermission;
    @Nullable
    private List<String> hoverText;
    @Nullable
    private String hoverItem;
    @Nullable
    private String openUrl;
    @Nullable
    private String suggestCommand;
    @Nullable
    private String runCommand;
    private final boolean saveComments;

    private Variable(String file) {
        String prototypePath = (String)PROTOTYPE_PATH.apply(file);
        this.saveComments = prototypePath != null;
        this.loadConfiguration(prototypePath, "variables/" + file + ".yml");
    }

    protected boolean saveComments() {
        return this.saveComments;
    }

    protected void onLoadFinish() {
        this.type = (Variable.Type)this.get("Type", Variable.Type.class);
        this.key = this.getString("Key");
        this.value = this.getString("Value");
        this.senderCondition = this.getString("Sender_Condition");
        this.receiverCondition = this.getString("Receiver_Condition");
        this.senderPermission = this.getString("Sender_Permission");
        this.receiverPermission = this.getString("Receiver_Permission");
        if (this.type == null) {
            this.type = Variable.Type.FORMAT;
            this.save();
        }

        if (this.key.startsWith("{") || this.key.startsWith("[")) {
            this.key = this.key.substring(1);
            this.save();
        }

        if (this.key.endsWith("}") || this.key.endsWith("]")) {
            this.key = this.key.substring(0, this.key.length() - 1);
            this.save();
        }

        if (this.type == Variable.Type.MESSAGE) {
            this.hoverText = this.getStringList("Hover");
            this.hoverItem = this.getString("Hover_Item");
            this.openUrl = this.getString("Open_Url");
            this.suggestCommand = this.getString("Suggest_Command");
            this.runCommand = this.getString("Run_Command");
        }

        if (this.key != null && !this.key.isEmpty()) {
            if (this.value != null && !this.value.isEmpty()) {
                if (!Common.regExMatch("^\\w+$", this.key)) {
                    throw new IllegalArgumentException("(DO NOT REPORT, PLEASE FIX YOURSELF) The 'Key' variable in " + this.getFile() + " must only contains letters, numbers or underscores. Do not write [] or {} there!");
                }
            } else {
                throw new NullPointerException("(DO NOT REPORT, PLEASE FIX YOURSELF) Please set 'Value' key as what the variable shows in " + this.getFile() + " (this can be a JavaScript code)");
            }
        } else {
            throw new NullPointerException("(DO NOT REPORT, PLEASE FIX YOURSELF) Please set 'Key' as variable name in " + this.getFile());
        }
    }

    public SerializedMap serialize() {
        SerializedMap map = new SerializedMap();
        map.putIf("Type", this.type);
        map.putIf("Key", this.key);
        map.putIf("Sender_Condition", this.senderCondition);
        map.putIf("Receiver_Condition", this.receiverCondition);
        map.putIf("Hover", this.hoverText);
        map.putIf("Hover_Item", this.hoverItem);
        map.putIf("Open_Url", this.openUrl);
        map.putIf("Suggest_Command", this.suggestCommand);
        map.putIf("Run_Command", this.runCommand);
        map.putIf("Sender_Permission", this.senderPermission);
        map.putIf("Receiver_Permission", this.receiverPermission);
        return map;
    }

    public String getValue(CommandSender sender, @Nullable Map<String, Object> replacements) {
        Variables.REPLACE_JAVASCRIPT = false;

        String result;
        try {
            String script = Variables.replace(this.value, sender, replacements);
            result = String.valueOf(JavaScriptExecutor.run(script, sender));
            String var5 = result;
            return var5;
        } catch (RuntimeException var9) {
            if (sender instanceof Player) {
                throw var9;
            }

            result = "";
        } finally {
            Variables.REPLACE_JAVASCRIPT = true;
        }

        return result;
    }

    public SimpleComponent build(CommandSender sender, SimpleComponent existingComponent, @Nullable Map<String, Object> replacements) {
        if (this.senderPermission != null && !this.senderPermission.isEmpty() && !PlayerUtil.hasPerm(sender, this.senderPermission)) {
            return SimpleComponent.of("");
        } else {
            if (this.senderCondition != null && !this.senderCondition.isEmpty()) {
                Object result = JavaScriptExecutor.run(this.senderCondition, sender);
                if (result != null) {
                    Valid.checkBoolean(result instanceof Boolean, "Variable '" + this.getName() + "' option Condition must return boolean not " + (result == null ? "null" : result.getClass()), new Object[0]);
                    if (!(Boolean)result) {
                        return SimpleComponent.of("");
                    }
                }
            }

            String value = this.getValue(sender, replacements);
            if (value != null && !value.isEmpty() && !"null".equals(value)) {
                SimpleComponent component = existingComponent.append(Variables.replace(value, sender, replacements)).viewPermission(this.receiverPermission).viewCondition(this.receiverCondition);
                if (!Valid.isNullOrEmpty(this.hoverText)) {
                    component.onHover(Variables.replace(this.hoverText, sender, replacements));
                }

                if (this.hoverItem != null && !this.hoverItem.isEmpty()) {
                    Object result = JavaScriptExecutor.run(Variables.replace(this.hoverItem, sender, replacements), sender);
                    Valid.checkBoolean(result instanceof ItemStack, "Variable '" + this.getName() + "' option Hover_Item must return ItemStack not " + result.getClass(), new Object[0]);
                    component.onHover((ItemStack)result);
                }

                if (this.openUrl != null && !this.openUrl.isEmpty()) {
                    component.onClickOpenUrl(Variables.replace(this.openUrl, sender, replacements));
                }

                if (this.suggestCommand != null && !this.suggestCommand.isEmpty()) {
                    component.onClickSuggestCmd(Variables.replace(this.suggestCommand, sender, replacements));
                }

                if (this.runCommand != null && !this.runCommand.isEmpty()) {
                    component.onClickRunCmd(Variables.replace(this.runCommand, sender, replacements));
                }

                return component;
            } else {
                return SimpleComponent.of("");
            }
        }
    }

    public String toString() {
        return this.serialize().toStringFormatted();
    }

    public static void loadVariables() {
        loadedVariables.loadItems();
    }

    public static void removeVariable(Variable variable) {
        loadedVariables.removeItem(variable);
    }

    public static boolean isVariableLoaded(String name) {
        return loadedVariables.isItemLoaded(name);
    }

    public static Variable findVariable(@NonNull String name) {
        if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
        } else {
            Iterator var1 = getVariables().iterator();

            Variable item;
            do {
                if (!var1.hasNext()) {
                    return null;
                }

                item = (Variable)var1.next();
            } while(!item.getKey().equalsIgnoreCase(name));

            return item;
        }
    }

    public static List<Variable> getVariables() {
        return loadedVariables.getItems();
    }

    public static List<String> getVariableNames() {
        return loadedVariables.getItemNames();
    }

    public Variable.Type getType() {
        return this.type;
    }

    public String getKey() {
        return this.key;
    }

    @Nullable
    public String getSenderCondition() {
        return this.senderCondition;
    }

    @Nullable
    public String getReceiverCondition() {
        return this.receiverCondition;
    }

    @Nullable
    public String getSenderPermission() {
        return this.senderPermission;
    }

    @Nullable
    public String getReceiverPermission() {
        return this.receiverPermission;
    }

    @Nullable
    public List<String> getHoverText() {
        return this.hoverText;
    }

    @Nullable
    public String getHoverItem() {
        return this.hoverItem;
    }

    @Nullable
    public String getOpenUrl() {
        return this.openUrl;
    }

    @Nullable
    public String getSuggestCommand() {
        return this.suggestCommand;
    }

    @Nullable
    public String getRunCommand() {
        return this.runCommand;
    }

    public static enum Type {
        FORMAT("format"),
        MESSAGE("message");

        private final String key;

        public static Variable.Type fromKey(String key) {
            Variable.Type[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Variable.Type mode = var1[var3];
                if (mode.key.equalsIgnoreCase(key)) {
                    return mode;
                }
            }

            throw new IllegalArgumentException("No such item type: " + key + ". Available: " + Common.join(values()));
        }

        public String toString() {
            return this.key;
        }

        private Type(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }
}