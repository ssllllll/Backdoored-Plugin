package me.ego.ezbd.lib.fo.command;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MathUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.RandomUtil;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.ChatPaginator;
import me.ego.ezbd.lib.fo.model.Replacer;
import me.ego.ezbd.lib.fo.model.SimpleComponent;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Commands;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class SimpleCommandGroup {
    private final StrictList<SimpleSubCommand> subcommands = new StrictList();
    private SimpleCommand mainCommand;
    private int commandsPerPage = 12;

    public final void register(StrictList<String> labelAndAliases) {
        this.register((String)labelAndAliases.get(0), (labelAndAliases.size() > 1 ? labelAndAliases.range(1) : new StrictList()).getSource());
    }

    public final void register(String label, List<String> aliases) {
        Valid.checkBoolean(!this.isRegistered(), "Main command already registered as: " + this.mainCommand, new Object[0]);
        this.mainCommand = new SimpleCommandGroup.MainCommand(label);
        if (aliases != null) {
            this.mainCommand.setAliases(aliases);
        }

        this.mainCommand.register();
        this.registerSubcommands();
        Collections.sort(this.subcommands.getSource(), (f, s) -> {
            return f.getSublabel().compareTo(s.getSublabel());
        });
        this.checkSubCommandAliasesCollision();
    }

    private void checkSubCommandAliasesCollision() {
        List<String> aliases = new ArrayList();
        Iterator var2 = this.subcommands.iterator();

        while(var2.hasNext()) {
            SimpleSubCommand subCommand = (SimpleSubCommand)var2.next();
            String[] var4 = subCommand.getSublabels();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String alias = var4[var6];
                Valid.checkBoolean(!aliases.contains(alias), "Subcommand '/" + this.getLabel() + " " + subCommand.getSublabel() + "' has alias '" + alias + "' that is already in use by another subcommand!", new Object[0]);
                aliases.add(alias);
            }
        }

    }

    public final void unregister() {
        Valid.checkBoolean(this.isRegistered(), "Main command not registered!", new Object[0]);
        this.mainCommand.unregister();
        this.mainCommand = null;
        this.subcommands.clear();
    }

    public final boolean isRegistered() {
        return this.mainCommand != null;
    }

    /** @deprecated */
    @Deprecated
    protected final <T extends SimpleSubCommand> void autoRegisterSubcommands(Class<T> ofClass) {
        Iterator var2 = ReflectionUtil.getClasses(SimplePlugin.getInstance(), ofClass).iterator();

        while(var2.hasNext()) {
            Class<? extends SimpleSubCommand> clazz = (Class)var2.next();
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                this.registerSubcommand((SimpleSubCommand)ReflectionUtil.instantiate(clazz));
            }
        }

    }

    protected abstract void registerSubcommands();

    protected final void registerSubcommand(SimpleSubCommand command) {
        Valid.checkNotNull(this.mainCommand, "Cannot add subcommands when main command is missing! Call register()");
        Valid.checkBoolean(!this.subcommands.contains(command), "Subcommand /" + this.mainCommand.getLabel() + " " + command.getSublabel() + " already registered when trying to add " + command.getClass(), new Object[0]);
        this.subcommands.add(command);
    }

    protected final void registerHelpLine(String... menuHelp) {
        Valid.checkNotNull(this.mainCommand, "Cannot add subcommands when main command is missing! Call register()");
        this.subcommands.add(new SimpleCommandGroup.FillerSubCommand(this, menuHelp));
    }

    public final String getLabel() {
        Valid.checkBoolean(this.isRegistered(), "Main command has not yet been set!", new Object[0]);
        return this.mainCommand.getMainLabel();
    }

    public final List<String> getAliases() {
        return this.mainCommand.getAliases();
    }

    protected List<SimpleComponent> getNoParamsHeader(CommandSender sender) {
        int foundedYear = SimplePlugin.getInstance().getFoundedYear();
        int yearNow = Calendar.getInstance().get(1);
        List<String> messages = new ArrayList();
        messages.add("&8" + Common.chatLineSmooth());
        messages.add(this.getHeaderPrefix() + "  " + SimplePlugin.getNamed() + this.getTrademark() + " &7" + SimplePlugin.getVersion());
        messages.add(" ");
        String credits = String.join(", ", SimplePlugin.getInstance().getDescription().getAuthors());
        if (!credits.isEmpty()) {
            messages.add("   &7" + Commands.LABEL_AUTHORS + " &f" + credits + (foundedYear != -1 ? " &7© " + foundedYear + (yearNow != foundedYear ? " - " + yearNow : "") : ""));
        }

        credits = this.getCredits();
        if (credits != null && !credits.isEmpty()) {
            messages.add("   " + credits);
        }

        messages.add("&8" + Common.chatLineSmooth());
        return Common.convert(messages, SimpleComponent::of);
    }

    protected boolean sendHelpIfNoArgs() {
        return false;
    }

    private String getTrademark() {
        return SimplePlugin.getInstance().getDescription().getAuthors().contains("kangarko") ? this.getHeaderPrefix() + "&8™" : "";
    }

    protected String getCredits() {
        return "&7Visit &fmineacademy.org &7for more information.";
    }

    protected List<String> getHelpLabel() {
        return Arrays.asList("help", "?");
    }

    protected String[] getHelpHeader() {
        return new String[]{"&8", "&8" + Common.chatLineSmooth(), this.getHeaderPrefix() + "  " + SimplePlugin.getNamed() + this.getTrademark() + " &7" + SimplePlugin.getVersion(), " ", "&2  [] &f= " + Commands.LABEL_OPTIONAL_ARGS, "&6  <> &f= " + Commands.LABEL_REQUIRED_ARGS, " "};
    }

    protected String getSubcommandDescription() {
        return Commands.LABEL_SUBCOMMAND_DESCRIPTION;
    }

    protected String getHeaderPrefix() {
        return "" + ChatColor.GOLD + ChatColor.BOLD;
    }

    protected SimpleCommandGroup() {
    }

    protected void setCommandsPerPage(int commandsPerPage) {
        this.commandsPerPage = commandsPerPage;
    }

    private final class FillerSubCommand extends SimpleSubCommand {
        private final String[] helpMessages;

        private FillerSubCommand(SimpleCommandGroup parent, String... menuHelp) {
            super(parent, "_" + RandomUtil.nextBetween(1, 32767));
            this.helpMessages = menuHelp;
        }

        protected void onCommand() {
            throw new FoException("Filler space command cannot be run!");
        }

        public String[] getHelpMessages() {
            return this.helpMessages;
        }
    }

    public final class MainCommand extends SimpleCommand {
        private MainCommand(String label) {
            super(label);
            this.setPermission((String)null);
            this.setAutoHandleHelp(false);
        }

        protected void onCommand() {
            if (this.args.length == 0) {
                if (SimpleCommandGroup.this.sendHelpIfNoArgs()) {
                    this.tellSubcommandsHelp();
                } else {
                    this.tell(SimpleCommandGroup.this.getNoParamsHeader(this.sender));
                }

            } else {
                String argument = this.args[0];
                SimpleSubCommand command = this.findSubcommand(argument);
                if (command != null) {
                    String oldSublabel = command.getSublabel();

                    try {
                        command.setSublabel(this.args[0]);
                        command.execute(this.sender, this.getLabel(), this.args.length == 1 ? new String[0] : (String[])Arrays.copyOfRange(this.args, 1, this.args.length));
                    } finally {
                        command.setSublabel(oldSublabel);
                    }
                } else if (!SimpleCommandGroup.this.getHelpLabel().isEmpty() && Valid.isInList(argument, SimpleCommandGroup.this.getHelpLabel())) {
                    this.tellSubcommandsHelp();
                } else {
                    this.returnInvalidArgs();
                }

            }
        }

        protected void tellSubcommandsHelp() {
            Common.runAsync(() -> {
                if (SimpleCommandGroup.this.subcommands.isEmpty()) {
                    this.tellError(Commands.HEADER_NO_SUBCOMMANDS);
                } else {
                    List<SimpleComponent> lines = new ArrayList();
                    boolean atLeast17 = MinecraftVersion.atLeast(V.v1_7);
                    Iterator var3 = SimpleCommandGroup.this.subcommands.iterator();

                    while(true) {
                        while(true) {
                            SimpleSubCommand subcommand;
                            do {
                                do {
                                    if (!var3.hasNext()) {
                                        if (!lines.isEmpty()) {
                                            ChatPaginator pages = new ChatPaginator(MathUtil.range(0, lines.size(), SimpleCommandGroup.this.commandsPerPage), ChatColor.DARK_GRAY);
                                            if (SimpleCommandGroup.this.getHelpHeader() != null) {
                                                pages.setHeader(SimpleCommandGroup.this.getHelpHeader());
                                            }

                                            pages.setPages(lines);
                                            Common.runLater(() -> {
                                                pages.send(this.sender);
                                            });
                                        } else {
                                            this.tellError(Commands.HEADER_NO_SUBCOMMANDS_PERMISSION);
                                        }

                                        return;
                                    }

                                    subcommand = (SimpleSubCommand)var3.next();
                                } while(!subcommand.showInHelp());
                            } while(!this.hasPerm(subcommand.getPermission()));

                            if (subcommand instanceof SimpleCommandGroup.FillerSubCommand) {
                                this.tellNoPrefix(((SimpleCommandGroup.FillerSubCommand)subcommand).getHelpMessages());
                            } else {
                                String usage = this.colorizeUsage(subcommand.getUsage());
                                String desc = Common.getOrEmpty(subcommand.getDescription());
                                String plainMessage = Replacer.replaceArray(SimpleCommandGroup.this.getSubcommandDescription(), new Object[]{"label", this.getLabel(), "sublabel", subcommand.getSublabel(), "usage", usage, "description", !desc.isEmpty() && !atLeast17 ? desc : "", "dash", !desc.isEmpty() && !atLeast17 ? "&e-" : ""});
                                SimpleComponent line = SimpleComponent.of(plainMessage);
                                if (!desc.isEmpty() && atLeast17) {
                                    String command = Common.stripColors(plainMessage).substring(1);
                                    List<String> hover = new ArrayList();
                                    hover.add(Commands.HELP_TOOLTIP_DESCRIPTION.replace("{description}", desc));
                                    if (subcommand.getPermission() != null) {
                                        hover.add(Commands.HELP_TOOLTIP_PERMISSION.replace("{permission}", subcommand.getPermission()));
                                    }

                                    if (subcommand.getMultilineUsageMessage() != null && subcommand.getMultilineUsageMessage().length > 0) {
                                        hover.add(Commands.HELP_TOOLTIP_USAGE);
                                        String[] var11 = subcommand.getMultilineUsageMessage();
                                        int var12 = var11.length;

                                        for(int var13 = 0; var13 < var12; ++var13) {
                                            String usageLine = var11[var13];
                                            hover.add("&f" + this.replacePlaceholders(this.colorizeUsage(usageLine.replace("{sublabel}", subcommand.getSublabel()))));
                                        }
                                    } else {
                                        hover.add(Commands.HELP_TOOLTIP_USAGE + (usage.isEmpty() ? command : usage));
                                    }

                                    line.onHover(hover);
                                    line.onClickSuggestCmd("/" + this.getLabel() + " " + subcommand.getSublabel());
                                }

                                lines.add(line);
                            }
                        }
                    }
                }
            });
        }

        private String colorizeUsage(String message) {
            return message == null ? "" : message.replace("<", "&6<").replace(">", "&6>&f").replace("[", "&2[").replace("]", "&2]&f").replaceAll(" \\-([a-zA-Z])", " &3-$1");
        }

        private SimpleSubCommand findSubcommand(String label) {
            Iterator var2 = SimpleCommandGroup.this.subcommands.iterator();

            while(true) {
                SimpleSubCommand command;
                do {
                    if (!var2.hasNext()) {
                        return null;
                    }

                    command = (SimpleSubCommand)var2.next();
                } while(command instanceof SimpleCommandGroup.FillerSubCommand);

                String[] var4 = command.getSublabels();
                int var5 = var4.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    String alias = var4[var6];
                    if (alias.equalsIgnoreCase(label)) {
                        return command;
                    }
                }
            }
        }

        public List<String> tabComplete() {
            if (this.args.length == 1) {
                return this.tabCompleteSubcommands(this.sender, this.args[0]);
            } else {
                if (this.args.length > 1) {
                    SimpleSubCommand cmd = this.findSubcommand(this.args[0]);
                    if (cmd != null) {
                        return cmd.tabComplete(this.sender, this.getLabel(), (String[])Arrays.copyOfRange(this.args, 1, this.args.length));
                    }
                }

                return null;
            }
        }

        private List<String> tabCompleteSubcommands(CommandSender sender, String param) {
            param = param.toLowerCase();
            List<String> tab = new ArrayList();
            Iterator var4 = SimpleCommandGroup.this.subcommands.iterator();

            while(true) {
                SimpleSubCommand subcommand;
                do {
                    do {
                        do {
                            if (!var4.hasNext()) {
                                return tab;
                            }

                            subcommand = (SimpleSubCommand)var4.next();
                        } while(!subcommand.showInHelp());
                    } while(subcommand instanceof SimpleCommandGroup.FillerSubCommand);
                } while(!this.hasPerm(subcommand.getPermission()));

                String[] var6 = subcommand.getSublabels();
                int var7 = var6.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    String label = var6[var8];
                    if (!label.trim().isEmpty() && label.startsWith(param)) {
                        tab.add(label);
                    }
                }
            }
        }
    }
}
