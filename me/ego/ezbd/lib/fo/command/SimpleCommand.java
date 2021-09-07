package me.ego.ezbd.lib.fo.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Messenger;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.TabUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.collection.expiringmap.ExpiringMap;
import me.ego.ezbd.lib.fo.command.SimpleCommandGroup.MainCommand;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.debug.LagCatcher;
import me.ego.ezbd.lib.fo.exception.CommandException;
import me.ego.ezbd.lib.fo.exception.EventHandledException;
import me.ego.ezbd.lib.fo.exception.InvalidCommandArgException;
import me.ego.ezbd.lib.fo.model.ChatPaginator;
import me.ego.ezbd.lib.fo.model.Replacer;
import me.ego.ezbd.lib.fo.model.SimpleComponent;
import me.ego.ezbd.lib.fo.model.SimpleTime;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public abstract class SimpleCommand extends Command {
    /** @deprecated */
    @Deprecated
    public static boolean USE_MESSENGER;
    protected static final List<String> NO_COMPLETE;
    private final ExpiringMap<UUID, Long> cooldownMap;
    private String label;
    private boolean registered;
    private boolean addTellPrefix;
    private String tellPrefix;
    private int minArguments;
    private int cooldownSeconds;
    private String cooldownMessage;
    private boolean autoHandleHelp;
    protected CommandSender sender;
    protected String[] args;

    protected static final String getDefaultPermission() {
        return SimplePlugin.getNamed().toLowerCase() + ".command.{label}";
    }

    protected SimpleCommand(String label) {
        this(parseLabel0(label), parseAliases0(label));
    }

    protected SimpleCommand(StrictList<String> labels) {
        this(parseLabelList0(labels), labels.size() > 1 ? labels.subList(1, labels.size()) : null);
    }

    protected SimpleCommand(String label, List<String> aliases) {
        super(label);
        this.cooldownMap = ExpiringMap.builder().expiration(30L, TimeUnit.MINUTES).build();
        this.registered = false;
        this.addTellPrefix = true;
        this.tellPrefix = "";
        this.minArguments = 0;
        this.cooldownSeconds = 0;
        this.cooldownMessage = null;
        this.autoHandleHelp = true;
        Valid.checkBoolean(!(this instanceof CommandExecutor), "Please do not write 'implements CommandExecutor' for /" + super.getLabel() + " cmd, we already have a listener there", new Object[0]);
        Valid.checkBoolean(!(this instanceof TabCompleter), "Please do not write 'implements TabCompleter' for /" + super.getLabel() + " cmd, simply override tabComplete method", new Object[0]);
        this.setLabel(label);
        if (aliases != null) {
            this.setAliases(aliases);
        }

        this.setPermission(getDefaultPermission());
    }

    private static String parseLabel0(String label) {
        Valid.checkNotNull(label, "Label must not be null!");
        return label.split("(\\||\\/)")[0];
    }

    private static List<String> parseAliases0(String label) {
        String[] aliases = label.split("(\\||\\/)");
        return (List)(aliases.length > 0 ? Arrays.asList(Arrays.copyOfRange(aliases, 1, aliases.length)) : new ArrayList());
    }

    private static String parseLabelList0(StrictList<String> labels) {
        Valid.checkBoolean(!labels.isEmpty(), "Command label must not be empty!", new Object[0]);
        return (String)labels.get(0);
    }

    public final void register() {
        this.register(true);
    }

    public final void register(boolean unregisterOldAliases) {
        this.register(true, unregisterOldAliases);
    }

    public final void register(boolean unregisterOldCommand, boolean unregisterOldAliases) {
        Valid.checkBoolean(!(this instanceof SimpleSubCommand), "Sub commands cannot be registered!", new Object[0]);
        Valid.checkBoolean(!this.registered, "The command /" + this.getLabel() + " has already been registered!", new Object[0]);
        PluginCommand oldCommand = Bukkit.getPluginCommand(this.getLabel());
        if (oldCommand != null && unregisterOldCommand) {
            String owningPlugin = oldCommand.getPlugin().getName();
            if (!owningPlugin.equals(SimplePlugin.getNamed())) {
                Debugger.debug("command", new String[]{"Command /" + this.getLabel() + " already (" + owningPlugin + "), overriding and unregistering /" + oldCommand.getLabel() + ", /" + String.join(", /", oldCommand.getAliases())});
            }

            Remain.unregisterCommand(oldCommand.getLabel(), unregisterOldAliases);
        }

        Remain.registerCommand(this);
        this.registered = true;
    }

    public final void unregister() {
        Valid.checkBoolean(!(this instanceof SimpleSubCommand), "Sub commands cannot be unregistered!", new Object[0]);
        Valid.checkBoolean(this.registered, "The command /" + this.getLabel() + " is not registered!", new Object[0]);
        Remain.unregisterCommand(this.getLabel());
        this.registered = false;
    }

    public final boolean execute(CommandSender sender, String label, String[] args) {
        if (!SimplePlugin.isReloading() && SimplePlugin.getInstance().isEnabled()) {
            this.sender = sender;
            this.label = label;
            this.args = args;
            boolean hadTellPrefix = Common.ADD_TELL_PREFIX;
            if (hadTellPrefix) {
                Common.ADD_TELL_PREFIX = this.addTellPrefix;
            }

            String sublabel = this instanceof SimpleSubCommand ? " " + ((SimpleSubCommand)this).getSublabel() : "";
            String lagSection = "Command /" + this.getLabel() + sublabel + (args.length > 0 ? " " + String.join(" ", args) : "");

            boolean var7;
            try {
                if (!(this instanceof MainCommand)) {
                    LagCatcher.start(lagSection);
                }

                if (this.getPermission() != null) {
                    this.checkPerm(this.getPermission());
                }

                if (args.length >= this.getMinArguments() && (!this.autoHandleHelp || args.length != 1 || !"help".equals(args[0]) && !"?".equals(args[0]))) {
                    if (this.cooldownSeconds > 0) {
                        this.handleCooldown();
                    }

                    this.onCommand();
                    return true;
                }

                Common.runAsync(() -> {
                    String usage = this.getMultilineUsageMessage() != null ? String.join("\n&c", this.getMultilineUsageMessage()) : (this.getUsage() != null ? this.getUsage() : null);
                    Valid.checkNotNull(usage, "getUsage() nor getMultilineUsageMessage() not implemented for '/" + this.getLabel() + sublabel + "' command!");
                    ChatPaginator paginator = new ChatPaginator(Commands.HEADER_SECONDARY_COLOR);
                    List<String> pages = new ArrayList();
                    if (!Common.getOrEmpty(this.getDescription()).isEmpty()) {
                        pages.add(this.replacePlaceholders(Commands.LABEL_DESCRIPTION));
                        pages.add(this.replacePlaceholders("&c" + this.getDescription()));
                    }

                    if (this.getMultilineUsageMessage() != null) {
                        pages.add("");
                        pages.add(this.replacePlaceholders(Commands.LABEL_USAGES));
                        String[] var7 = usage.split("\n");
                        int var8 = var7.length;

                        for(int var9 = 0; var9 < var8; ++var9) {
                            String usagePart = var7[var9];
                            pages.add(this.replacePlaceholders("&c" + usagePart));
                        }
                    } else {
                        pages.add("");
                        pages.add(Commands.LABEL_USAGE);
                        pages.add("&c" + this.replacePlaceholders("/" + label + sublabel + (!usage.startsWith("/") ? " " + Common.stripColors(usage) : "")));
                    }

                    paginator.setFoundationHeader(Commands.LABEL_HELP_FOR.replace("{label}", this.getLabel() + sublabel)).setPages(Common.toArray(pages));
                    Common.runLater(() -> {
                        paginator.send(sender);
                    });
                });
                var7 = true;
            } catch (InvalidCommandArgException var18) {
                if (this.getMultilineUsageMessage() != null) {
                    this.dynamicTellError(Commands.INVALID_ARGUMENT_MULTILINE);
                    String[] var8 = this.getMultilineUsageMessage();
                    int var9 = var8.length;

                    for(int var10 = 0; var10 < var9; ++var10) {
                        String line = var8[var10];
                        this.tellNoPrefix("&c" + line);
                    }

                    return true;
                }

                this.dynamicTellError(var18.getMessage() != null ? var18.getMessage() : Commands.INVALID_SUB_ARGUMENT);
                return true;
            } catch (EventHandledException var19) {
                if (var19.getMessages() != null) {
                    this.dynamicTellError(var19.getMessages());
                }

                return true;
            } catch (CommandException var20) {
                if (var20.getMessages() != null) {
                    this.dynamicTellError(var20.getMessages());
                }

                return true;
            } catch (Throwable var21) {
                this.dynamicTellError(Commands.ERROR.replace("{error}", var21.toString()));
                Common.error(var21, new String[]{"Failed to execute command /" + this.getLabel() + sublabel + " " + String.join(" ", args)});
                return true;
            } finally {
                Common.ADD_TELL_PREFIX = hadTellPrefix;
                if (!(this instanceof MainCommand)) {
                    LagCatcher.end(lagSection, 8, "{section} took {time} ms");
                }

            }

            return var7;
        } else {
            Common.tell(sender, new String[]{Commands.USE_WHILE_NULL.replace("{state}", SimplePlugin.isReloading() ? Commands.RELOADING : Commands.DISABLED)});
            return false;
        }
    }

    private void dynamicTellError(String... messages) {
        if (USE_MESSENGER) {
            String[] var2 = messages;
            int var3 = messages.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String message = var2[var4];
                this.tellError(message);
            }
        } else {
            this.tell(messages);
        }

    }

    private void handleCooldown() {
        if (this.isPlayer()) {
            Player player = this.getPlayer();
            long lastExecution = (Long)this.cooldownMap.getOrDefault(player.getUniqueId(), 0L);
            long lastExecutionDifference = (System.currentTimeMillis() - lastExecution) / 1000L;
            this.checkBoolean(lastExecution == 0L || lastExecutionDifference > (long)this.cooldownSeconds, ((String)Common.getOrDefault(this.cooldownMessage, Commands.COOLDOWN_WAIT)).replace("{duration}", (long)this.cooldownSeconds - lastExecutionDifference + 1L + ""));
            this.cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
        }

    }

    protected abstract void onCommand();

    protected String[] getMultilineUsageMessage() {
        return null;
    }

    protected final void checkConsole() throws CommandException {
        if (!this.isPlayer()) {
            throw new CommandException(new String[]{"&c" + Commands.NO_CONSOLE});
        }
    }

    public final void checkPerm(@NonNull String perm) throws CommandException {
        if (perm == null) {
            throw new NullPointerException("perm is marked non-null but is null");
        } else if (this.isPlayer() && !this.hasPerm(perm)) {
            throw new CommandException(new String[]{this.getPermissionMessage().replace("{permission}", perm)});
        }
    }

    public final void checkPerm(@NonNull CommandSender sender, @NonNull String perm) throws CommandException {
        if (sender == null) {
            throw new NullPointerException("sender is marked non-null but is null");
        } else if (perm == null) {
            throw new NullPointerException("perm is marked non-null but is null");
        } else if (this.isPlayer() && !this.hasPerm(sender, perm)) {
            throw new CommandException(new String[]{this.getPermissionMessage().replace("{permission}", perm)});
        }
    }

    protected final void checkArgs(int minimumLength, String falseMessage) throws CommandException {
        if (this.args.length < minimumLength) {
            this.returnTell((USE_MESSENGER ? "" : "&c") + falseMessage);
        }

    }

    protected final void checkBoolean(boolean value, String falseMessage) throws CommandException {
        if (!value) {
            this.returnTell((USE_MESSENGER ? "" : "&c") + falseMessage);
        }

    }

    protected final void checkUsage(boolean value) throws CommandException {
        if (!value) {
            this.returnInvalidArgs();
        }

    }

    protected final void checkNotNull(Object value, String messageIfNull) throws CommandException {
        if (value == null) {
            this.returnTell((USE_MESSENGER ? "" : "&c") + messageIfNull);
        }

    }

    protected final Player findPlayer(String name) throws CommandException {
        return this.findPlayer(name, me.ego.ezbd.lib.fo.settings.SimpleLocalization.Player.NOT_ONLINE);
    }

    protected final Player findPlayer(String name, String falseMessage) throws CommandException {
        Player player = this.findPlayerInternal(name);
        this.checkBoolean(player != null && player.isOnline() && !PlayerUtil.isVanished(player), falseMessage.replace("{player}", name));
        return player;
    }

    protected final Player findPlayerOrSelf(@Nullable String name) throws CommandException {
        if (name == null) {
            this.checkBoolean(this.isPlayer(), Commands.CONSOLE_MISSING_PLAYER_NAME);
            return this.getPlayer();
        } else {
            Player player = this.findPlayerInternal(name);
            this.checkBoolean(player != null && player.isOnline(), me.ego.ezbd.lib.fo.settings.SimpleLocalization.Player.NOT_ONLINE.replace("{player}", name));
            return player;
        }
    }

    protected Player findPlayerInternal(String name) {
        return Bukkit.getPlayer(name);
    }

    protected final SimpleTime findTime(String raw) {
        try {
            return SimpleTime.from(raw);
        } catch (IllegalArgumentException var3) {
            this.returnTell(Commands.INVALID_TIME.replace("{input}", raw));
            return null;
        }
    }

    protected final CompMaterial findMaterial(String name, String falseMessage) throws CommandException {
        CompMaterial found = CompMaterial.fromString(name);
        this.checkNotNull(found, falseMessage.replace("{enum}", name).replace("{item}", name));
        return found;
    }

    protected final <T extends Enum<T>> T findEnum(Class<T> enumType, String name, String falseMessage) throws CommandException {
        return this.findEnum(enumType, name, (Function)null, falseMessage);
    }

    protected final <T extends Enum<T>> T findEnum(Class<T> enumType, String name, Function<T, Boolean> condition, String falseMessage) throws CommandException {
        Enum found = null;

        try {
            found = ReflectionUtil.lookupEnum(enumType, name);
            if (!(Boolean)condition.apply(found)) {
                found = null;
            }
        } catch (Throwable var7) {
        }

        this.checkNotNull(found, falseMessage.replace("{enum}", name));
        return found;
    }

    protected final int findNumber(int index, int min, int max, String falseMessage) {
        return (Integer)this.findNumber(Integer.class, index, min, max, falseMessage);
    }

    protected final int findNumber(int index, String falseMessage) {
        return (Integer)this.findNumber(Integer.class, index, falseMessage);
    }

    protected final <T extends Number & Comparable<T>> T findNumber(Class<T> numberType, int index, T min, T max, String falseMessage) {
        T number = this.findNumber(numberType, index, falseMessage);
        this.checkBoolean(((Comparable)number).compareTo(min) >= 0 && ((Comparable)number).compareTo(max) <= 0, falseMessage.replace("{min}", min + "").replace("{max}", max + ""));
        return number;
    }

    protected final <T extends Number> T findNumber(Class<T> numberType, int index, String falseMessage) {
        this.checkBoolean(index < this.args.length, falseMessage);

        try {
            return (Number)numberType.getMethod("valueOf", String.class).invoke((Object)null, this.args[index]);
        } catch (NoSuchMethodException | IllegalAccessException var5) {
            var5.printStackTrace();
        } catch (InvocationTargetException var6) {
            if (!(var6.getCause() instanceof NumberFormatException)) {
                var6.printStackTrace();
            }
        }

        throw new CommandException(new String[]{this.replacePlaceholders((USE_MESSENGER ? "" : "&c") + falseMessage)});
    }

    protected final boolean findBoolean(int index, String invalidMessage) {
        this.checkBoolean(index < this.args.length, invalidMessage);
        if (this.args[index].equalsIgnoreCase("true")) {
            return true;
        } else if (this.args[index].equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new CommandException(new String[]{this.replacePlaceholders((USE_MESSENGER ? "" : "&c") + invalidMessage)});
        }
    }

    protected final boolean hasPerm(@Nullable String permission) {
        return this.hasPerm(this.sender, permission);
    }

    protected final boolean hasPerm(CommandSender sender, @Nullable String permission) {
        return permission == null ? true : PlayerUtil.hasPerm(sender, permission.replace("{label}", this.getLabel()));
    }

    protected final void tellReplaced(String message, Object... replacements) {
        this.tell(Replacer.replaceArray(message, replacements));
    }

    protected final void tell(@Nullable List<SimpleComponent> components) {
        if (components != null) {
            this.tell((SimpleComponent[])components.toArray(new SimpleComponent[components.size()]));
        }

    }

    protected final void tell(@Nullable SimpleComponent... components) {
        if (components != null) {
            SimpleComponent[] var2 = components;
            int var3 = components.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                SimpleComponent component = var2[var4];
                component.send(new CommandSender[]{this.sender});
            }
        }

    }

    protected final void tell(@Nullable Replacer replacer) {
        if (replacer != null) {
            this.tell(replacer.getReplacedMessage());
        }

    }

    protected final void tell(@Nullable Collection<String> messages) {
        if (messages != null) {
            this.tell((String[])messages.toArray(new String[messages.size()]));
        }

    }

    protected final void tellNoPrefix(@Nullable Replacer replacer) {
        if (replacer != null) {
            this.tellNoPrefix(replacer.getReplacedMessage());
        }

    }

    protected final void tellNoPrefix(Collection<String> messages) {
        this.tellNoPrefix((String[])messages.toArray(new String[messages.size()]));
    }

    protected final void tellNoPrefix(@Nullable String... messages) {
        boolean tellPrefix = Common.ADD_TELL_PREFIX;
        boolean localPrefix = this.addTellPrefix;
        Common.ADD_TELL_PREFIX = false;
        this.addTellPrefix = false;
        this.tell(messages);
        Common.ADD_TELL_PREFIX = tellPrefix;
        this.addTellPrefix = localPrefix;
    }

    protected final void tell(@Nullable String... messages) {
        if (messages != null) {
            messages = this.replacePlaceholders(messages);
            if (this.addTellPrefix && !USE_MESSENGER && messages.length <= 2) {
                if (this.tellPrefix.isEmpty()) {
                    Common.tell(this.sender, messages);
                } else {
                    String[] var6 = messages;
                    int var3 = messages.length;

                    for(int var4 = 0; var4 < var3; ++var4) {
                        String message = var6[var4];
                        Common.tellNoPrefix(this.sender, new String[]{(this.tellPrefix.isEmpty() ? "" : this.tellPrefix + " ") + message});
                    }
                }
            } else if (USE_MESSENGER && this.addTellPrefix) {
                this.tellInfo(messages[0]);
                if (messages.length > 1) {
                    for(int i = 1; i < messages.length; ++i) {
                        Common.tellNoPrefix(this.sender, new String[]{messages[i]});
                    }
                }
            } else {
                Common.tellNoPrefix(this.sender, messages);
            }
        }

    }

    protected final void tellSuccess(String message) {
        if (message != null) {
            message = this.replacePlaceholders(message);
            Messenger.success(this.sender, message);
        }

    }

    protected final void tellInfo(String message) {
        if (message != null) {
            message = this.replacePlaceholders(message);
            Messenger.info(this.sender, message);
        }

    }

    protected final void tellWarn(String message) {
        if (message != null) {
            message = this.replacePlaceholders(message);
            Messenger.warn(this.sender, message);
        }

    }

    protected final void tellError(String message) {
        if (message != null) {
            message = this.replacePlaceholders(message);
            Messenger.error(this.sender, message);
        }

    }

    protected final void tellQuestion(String message) {
        if (message != null) {
            message = this.replacePlaceholders(message);
            Messenger.question(this.sender, message);
        }

    }

    protected final void returnInvalidArgs() {
        this.tellError(Commands.INVALID_ARGUMENT.replace("{label}", this.getLabel()));
        throw new CommandException(new String[0]);
    }

    protected final void returnTell(Collection<String> messages) throws CommandException {
        this.returnTell((String[])messages.toArray(new String[messages.size()]));
    }

    protected final void returnTell(Replacer replacer) throws CommandException {
        this.returnTell(replacer.getReplacedMessage());
    }

    protected final void returnTell(String... messages) throws CommandException {
        throw new CommandException(this.replacePlaceholders(messages));
    }

    protected final void returnUsage() throws InvalidCommandArgException {
        throw new InvalidCommandArgException();
    }

    protected final String[] replacePlaceholders(String[] messages) {
        for(int i = 0; i < messages.length; ++i) {
            messages[i] = this.replacePlaceholders(messages[i]).replace("{prefix}", Common.getTellPrefix());
        }

        return messages;
    }

    protected String replacePlaceholders(String message) {
        message = this.replaceBasicPlaceholders0(message);

        for(int i = 0; i < this.args.length; ++i) {
            message = message.replace("{" + i + "}", Common.getOrEmpty(this.args[i]));
        }

        return message;
    }

    private String replaceBasicPlaceholders0(String message) {
        return message.replace("{label}", this.getLabel()).replace("{sublabel}", this instanceof SimpleSubCommand ? ((SimpleSubCommand)this).getSublabels()[0] : super.getLabel());
    }

    protected final void setArg(int position, String value) {
        if (this.args.length <= position) {
            this.args = (String[])Arrays.copyOf(this.args, position + 1);
        }

        this.args[position] = value;
    }

    protected final String getLastArg() {
        return this.args.length > 0 ? this.args[this.args.length - 1] : "";
    }

    protected final String[] rangeArgs(int from) {
        return this.rangeArgs(from, this.args.length);
    }

    protected final String[] rangeArgs(int from, int to) {
        return (String[])Arrays.copyOfRange(this.args, from, to);
    }

    protected final String joinArgs(int from) {
        return this.joinArgs(from, this.args.length);
    }

    protected final String joinArgs(int from, int to) {
        String message = "";

        for(int i = from; i < this.args.length && i < to; ++i) {
            message = message + this.args[i] + (i + 1 == this.args.length ? "" : " ");
        }

        return message;
    }

    /** @deprecated */
    @Deprecated
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        return this.tabComplete(sender, alias, args);
    }

    public final List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        this.sender = sender;
        this.label = alias;
        this.args = args;
        if (this.hasPerm(this.getPermission())) {
            List<String> suggestions = this.tabComplete();
            if (suggestions == null) {
                suggestions = this.completeLastWordPlayerNames();
            }

            return suggestions;
        } else {
            return new ArrayList();
        }
    }

    protected List<String> tabComplete() {
        return null;
    }

    protected final <T> List<String> completeLastWord(T... suggestions) {
        return TabUtil.complete(this.getLastArg(), suggestions);
    }

    protected final <T> List<String> completeLastWord(Iterable<T> suggestions) {
        List<T> list = new ArrayList();
        Iterator var3 = suggestions.iterator();

        while(var3.hasNext()) {
            T suggestion = var3.next();
            list.add(suggestion);
        }

        return TabUtil.complete(this.getLastArg(), list.toArray());
    }

    protected final <T> List<String> completeLastWord(Iterable<T> suggestions, Function<T, String> toString) {
        List<String> list = new ArrayList();
        Iterator var4 = suggestions.iterator();

        while(var4.hasNext()) {
            T suggestion = var4.next();
            list.add(toString.apply(suggestion));
        }

        return TabUtil.complete(this.getLastArg(), list.toArray());
    }

    protected List<String> completeLastWordPlayerNames() {
        return TabUtil.complete(this.getLastArg(), this.isPlayer() ? Common.getPlayerNames(false) : Common.getPlayerNames());
    }

    protected final Player getPlayer() {
        return this.isPlayer() ? (Player)this.getSender() : null;
    }

    protected final boolean isPlayer() {
        return this.sender instanceof Player;
    }

    protected final void addTellPrefix(boolean addTellPrefix) {
        this.addTellPrefix = addTellPrefix;
    }

    protected final void setTellPrefix(String tellPrefix) {
        this.tellPrefix = tellPrefix;
    }

    protected final void setMinArguments(int minArguments) {
        Valid.checkBoolean(minArguments >= 0, "Minimum arguments must be 0 or greater", new Object[0]);
        this.minArguments = minArguments;
    }

    protected final void setCooldown(int cooldown, TimeUnit unit) {
        Valid.checkBoolean(cooldown >= 0, "Cooldown must be >= 0 for /" + this.getLabel(), new Object[0]);
        this.cooldownSeconds = (int)unit.toSeconds((long)cooldown);
    }

    protected final void setCooldownMessage(String cooldownMessage) {
        this.cooldownMessage = cooldownMessage;
    }

    public final String getPermissionMessage() {
        return (String)Common.getOrDefault(super.getPermissionMessage(), "&c" + SimpleLocalization.NO_PERMISSION);
    }

    public final String getPermission() {
        return super.getPermission() == null ? null : this.replaceBasicPlaceholders0(super.getPermission());
    }

    /** @deprecated */
    @Deprecated
    protected final String getRawPermission() {
        return super.getPermission();
    }

    public final void setPermission(String permission) {
        super.setPermission(permission);
    }

    protected final CommandSender getSender() {
        Valid.checkNotNull(this.sender, "Sender cannot be null");
        return this.sender;
    }

    public final List<String> getAliases() {
        return super.getAliases();
    }

    public final String getDescription() {
        return super.getDescription();
    }

    public final String getName() {
        return super.getName();
    }

    public final String getUsage() {
        String bukkitUsage = super.getUsage();
        return bukkitUsage.equals("/" + this.getMainLabel()) ? "" : bukkitUsage;
    }

    public final String getLabel() {
        return this.label;
    }

    public final String getMainLabel() {
        return super.getLabel();
    }

    public final boolean setLabel(String label) {
        this.label = label;
        return super.setLabel(label);
    }

    protected final void setAutoHandleHelp(boolean autoHandleHelp) {
        this.autoHandleHelp = autoHandleHelp;
    }

    public boolean equals(Object obj) {
        return obj instanceof SimpleCommand ? ((SimpleCommand)obj).getLabel().equals(this.getLabel()) && ((SimpleCommand)obj).getAliases().equals(this.getAliases()) : false;
    }

    public final String toString() {
        return "Command{label=/" + this.label + "}";
    }

    public int getMinArguments() {
        return this.minArguments;
    }

    static {
        USE_MESSENGER = Messenger.ENABLED;
        NO_COMPLETE = Collections.unmodifiableList(new ArrayList());
    }
}
