package me.ego.ezbd.lib.fo.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.bungee.SimpleBungee;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.command.SimpleCommand;
import me.ego.ezbd.lib.fo.command.SimpleCommandGroup;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.event.SimpleListener;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.menu.MenuListener;
import me.ego.ezbd.lib.fo.menu.tool.Rocket;
import me.ego.ezbd.lib.fo.menu.tool.Tool;
import me.ego.ezbd.lib.fo.menu.tool.ToolsListener;
import me.ego.ezbd.lib.fo.metrics.Metrics;
import me.ego.ezbd.lib.fo.model.DiscordListener;
import me.ego.ezbd.lib.fo.model.EnchantmentListener;
import me.ego.ezbd.lib.fo.model.FolderWatcher;
import me.ego.ezbd.lib.fo.model.HookManager;
import me.ego.ezbd.lib.fo.model.JavaScriptExecutor;
import me.ego.ezbd.lib.fo.model.SimpleEnchantment;
import me.ego.ezbd.lib.fo.model.SimpleScoreboard;
import me.ego.ezbd.lib.fo.model.SpigotUpdater;
import me.ego.ezbd.lib.fo.model.DiscordListener.DiscordListenerImpl;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.remain.CompMetadata.MetadataFile;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization;
import me.ego.ezbd.lib.fo.settings.SimpleSettings;
import me.ego.ezbd.lib.fo.settings.YamlConfig;
import me.ego.ezbd.lib.fo.settings.YamlStaticConfig;
import me.ego.ezbd.lib.fo.visual.BlockVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

public abstract class SimplePlugin extends JavaPlugin implements Listener {
    private static volatile SimplePlugin instance;
    private static String version;
    private static String named;
    private static File source;
    private static File data;
    private static volatile boolean reloading = false;
    protected boolean isEnabled = true;
    private final Reloadables reloadables = new Reloadables();
    private boolean startingReloadables = false;

    public SimplePlugin() {
    }

    public static SimplePlugin getInstance() {
        if (instance == null) {
            try {
                instance = (SimplePlugin)JavaPlugin.getPlugin(SimplePlugin.class);
            } catch (IllegalStateException var1) {
                if (Bukkit.getPluginManager().getPlugin("PlugMan") != null) {
                    Bukkit.getLogger().severe("Failed to get instance of the plugin, if you reloaded using PlugMan you need to do a clean restart instead.");
                }

                throw var1;
            }

            Objects.requireNonNull(instance, "Cannot get a new instance! Have you reloaded?");
        }

        return instance;
    }

    public static final boolean hasInstance() {
        return instance != null;
    }

    public final void onLoad() {
        try {
            getInstance();
        } catch (Throwable var2) {
            if (!MinecraftVersion.olderThan(V.v1_7)) {
                throw var2;
            }

            instance = this;
        }

        version = instance.getDescription().getVersion();
        named = instance.getName();
        source = instance.getFile();
        data = instance.getDataFolder();
        this.onPluginLoad();
    }

    public final void onEnable() {
        StackTraceElement[] var1 = (new Throwable()).getStackTrace();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            StackTraceElement element = var1[var3];
            if (element.toString().contains("com.rylinaux.plugman.util.PluginUtil.load")) {
                Common.log(new String[]{"&cWarning: &fDetected PlugMan reload, which is poorly designed. It causes Bukkit not able to get our plugin from a static initializer. It may or may not run. Use our own reload command or do a clean restart!"});
                break;
            }
        }

        this.checkShading();
        if (this.isEnabled) {
            if (this.checkLibraries0() && this.checkServerVersions0()) {
                Debugger.detectDebugMode();
                if (this.getStartupLogo() != null) {
                    Common.ADD_LOG_PREFIX = false;
                }

                if (this.getStartupLogo() != null) {
                    boolean hadLogPrefix = Common.ADD_LOG_PREFIX;
                    Common.ADD_LOG_PREFIX = false;
                    Common.log(this.getStartupLogo());
                    Common.ADD_LOG_PREFIX = hadLogPrefix;
                }

                Remain.injectServerName();
                this.onPluginPreStart();
                if (this.isEnabled && this.isEnabled()) {
                    try {
                        if (this.getSettings() != null) {
                            YamlStaticConfig.load(this.getSettings());
                            Valid.checkBoolean(SimpleSettings.isSettingsCalled() != null && SimpleLocalization.isLocalizationCalled() != null, "Developer forgot to call Settings or Localization", new Object[0]);
                        }

                        if (this.isEnabled && this.isEnabled()) {
                            checkSingletons();

                            try {
                                HookManager.loadDependencies();
                            } catch (Throwable var5) {
                                Common.throwError(var5, new String[]{"Error while loading " + this.getName() + " dependencies!"});
                            }

                            if (this.isEnabled && this.isEnabled()) {
                                MetadataFile.getInstance();
                                if (this.getMainCommand() != null) {
                                    Valid.checkBoolean(!SimpleSettings.MAIN_COMMAND_ALIASES.isEmpty(), "Please make a settings class extending SimpleSettings and specify Command_Aliases in your settings file.", new Object[0]);
                                    this.reloadables.registerCommands(SimpleSettings.MAIN_COMMAND_ALIASES, this.getMainCommand());
                                }

                                if (this.isEnabled && this.isEnabled()) {
                                    this.startingReloadables = true;
                                    this.onReloadablesStart();
                                    this.startingReloadables = false;
                                    this.onPluginStart();
                                    if (this.isEnabled && this.isEnabled()) {
                                        this.registerBungeeCord();
                                        if (this.getUpdateCheck() != null) {
                                            this.getUpdateCheck().run();
                                        }

                                        this.registerEvents((Listener)this);
                                        this.registerEvents((Listener)(new MenuListener()));
                                        this.registerEvents((Listener)(new FoundationListener()));
                                        this.registerEvents((Listener)(new ToolsListener()));
                                        this.registerEvents((Listener)(new EnchantmentListener()));
                                        FoundationPacketListener.addPacketListener();
                                        if (HookManager.isDiscordSRVLoaded()) {
                                            DiscordListenerImpl discord = DiscordListenerImpl.getInstance();
                                            discord.resubscribe();
                                            discord.registerHook();
                                            this.reloadables.registerEvents(DiscordListenerImpl.getInstance());
                                        }

                                        Common.setTellPrefix(SimpleSettings.PLUGIN_PREFIX);
                                        int pluginId = this.getMetricsPluginId();
                                        if (pluginId != -1) {
                                            new Metrics(this, pluginId);
                                        }

                                        JavaScriptExecutor.run("");
                                    }
                                }
                            }
                        }
                    } catch (Throwable var6) {
                        this.displayError0(var6);
                    }
                }
            } else {
                this.isEnabled = false;
                this.setEnabled(false);
            }
        }
    }

    private final void registerBungeeCord() {
        Messenger messenger = this.getServer().getMessenger();
        SimpleBungee bungee = this.getBungeeCord();
        if (bungee != null) {
            messenger.registerIncomingPluginChannel(this, bungee.getChannel(), bungee.getListener());
            messenger.registerOutgoingPluginChannel(this, bungee.getChannel());
            this.reloadables.registerEvents(bungee.getListener());
            Debugger.debug("bungee", new String[]{"Registered BungeeCord listener for " + bungee.getChannel()});
        }

    }

    private static void checkSingletons() {
        try {
            JarFile file = new JarFile(getSource());
            Throwable var1 = null;

            try {
                Enumeration entry = file.entries();

                while(entry.hasMoreElements()) {
                    JarEntry jar = (JarEntry)entry.nextElement();
                    String name = jar.getName().replace("/", ".");

                    try {
                        if (name.endsWith(".class")) {
                            String className = name.substring(0, name.length() - 6);
                            Class clazz = null;

                            try {
                                clazz = SimplePlugin.class.getClassLoader().loadClass(className);
                            } catch (ClassNotFoundException | IncompatibleClassChangeError | NoClassDefFoundError var27) {
                                continue;
                            }

                            boolean isTool = Tool.class.isAssignableFrom(clazz) && !Tool.class.equals(clazz) && !Rocket.class.equals(clazz);
                            boolean isEnchant = SimpleEnchantment.class.isAssignableFrom(clazz) && !SimpleEnchantment.class.equals(clazz);
                            if (isTool || isEnchant) {
                                if (isEnchant && MinecraftVersion.olderThan(V.v1_13)) {
                                    Bukkit.getLogger().warning("**** WARNING ****");
                                    Bukkit.getLogger().warning("SimpleEnchantment requires Minecraft 1.13.2 or greater. The following class will not be registered: " + clazz.getName());
                                } else {
                                    try {
                                        Field instanceField = null;
                                        Field[] var34 = clazz.getDeclaredFields();
                                        int var11 = var34.length;

                                        int var12;
                                        for(var12 = 0; var12 < var11; ++var12) {
                                            Field field = var34[var12];
                                            if ((Tool.class.isAssignableFrom(field.getType()) || Enchantment.class.isAssignableFrom(field.getType())) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                                                instanceField = field;
                                            }
                                        }

                                        if (SimpleEnchantment.class.isAssignableFrom(clazz)) {
                                            Valid.checkNotNull(instanceField, "Your enchant class " + clazz.getSimpleName() + " must be a singleton and have static 'instance' field and private constructors!");
                                        }

                                        if (instanceField != null) {
                                            instanceField.setAccessible(true);
                                            Object instance = instanceField.get((Object)null);
                                            Constructor[] var36 = instance.getClass().getDeclaredConstructors();
                                            var12 = var36.length;

                                            for(int var37 = 0; var37 < var12; ++var37) {
                                                Constructor<?> con = var36[var37];
                                                Valid.checkBoolean(Modifier.isPrivate(con.getModifiers()), "Constructor " + con + " not private! Did you put '@NoArgsConstructor(access = AccessLevel.PRIVATE)' in your tools class?", new Object[0]);
                                            }

                                            if (instance instanceof Listener) {
                                                Common.registerEvents((Listener)instance);
                                            }
                                        }
                                    } catch (NoSuchFieldError var28) {
                                    } catch (Throwable var29) {
                                        String error = Common.getOrEmpty(var29.getMessage());
                                        if (var29 instanceof NoClassDefFoundError && error.contains("org/bukkit/entity")) {
                                            Bukkit.getLogger().warning("**** WARNING ****");
                                            if (error.contains("DragonFireball")) {
                                                Bukkit.getLogger().warning("Your Minecraft version does not have DragonFireball class, we suggest replacing it with a Fireball instead in: " + clazz);
                                            } else {
                                                Bukkit.getLogger().warning("Your Minecraft version does not have " + error + " class you call in: " + clazz);
                                            }
                                        } else {
                                            Common.error(var29, new String[]{"Failed to register events in " + clazz.getSimpleName() + " class " + clazz});
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable var30) {
                        if (!(var30 instanceof VerifyError)) {
                            Common.error(var30, new String[]{"Failed to scan class '" + name + "' using Foundation!"});
                        }
                    }
                }
            } catch (Throwable var31) {
                var1 = var31;
                throw var31;
            } finally {
                if (file != null) {
                    if (var1 != null) {
                        try {
                            file.close();
                        } catch (Throwable var26) {
                            var1.addSuppressed(var26);
                        }
                    } else {
                        file.close();
                    }
                }

            }
        } catch (Throwable var33) {
            Common.error(var33, new String[]{"Failed to scan classes using Foundation!"});
        }

    }

    private final void checkShading() {
        try {
            throw new SimplePlugin.ShadingException();
        } catch (Throwable var2) {
        }
    }

    private final boolean checkLibraries0() {
        boolean md_5 = false;
        boolean gson = false;

        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            md_5 = true;
        } catch (ClassNotFoundException var5) {
        }

        try {
            Class.forName("com.google.gson.JsonSyntaxException");
            gson = true;
        } catch (ClassNotFoundException var4) {
        }

        if (!md_5 || !gson) {
            Bukkit.getLogger().severe(Common.consoleLine());
            Bukkit.getLogger().severe("Your Minecraft version (" + MinecraftVersion.getCurrent() + ")");
            Bukkit.getLogger().severe("lacks libraries " + this.getName() + " needs:");
            Bukkit.getLogger().severe("JSON Chat (by md_5) found: " + md_5);
            Bukkit.getLogger().severe("Gson (by Google) found: " + gson);
            Bukkit.getLogger().severe(" ");
            Bukkit.getLogger().severe("To fix that, please install BungeeChatAPI:");
            Bukkit.getLogger().severe("https://mineacademy.org/plugins/#misc");
            Bukkit.getLogger().severe(Common.consoleLine());
        }

        return true;
    }

    private final boolean checkServerVersions0() {
        if (!MinecraftVersion.getCurrent().isTested()) {
            Common.logFramed(new String[]{"*** WARNING ***", "Your Minecraft version " + MinecraftVersion.getCurrent() + " has not yet", "been officialy tested with the Foundation,", "the library that " + getNamed() + " plugin uses.", "", "Loading the plugin at your own risk...", Common.consoleLine()});
        }

        V minimumVersion = this.getMinimumVersion();
        if (minimumVersion != null && MinecraftVersion.olderThan(minimumVersion)) {
            Common.logFramed(false, new String[]{this.getName() + " requires Minecraft " + minimumVersion + " or newer to run.", "Please upgrade your server."});
            return false;
        } else {
            V maximumVersion = this.getMaximumVersion();
            if (maximumVersion != null && MinecraftVersion.newerThan(maximumVersion)) {
                Common.logFramed(false, new String[]{this.getName() + " requires Minecraft " + maximumVersion + " or older to run.", "Please downgrade your server or", "wait for the new version."});
                return false;
            } else {
                return true;
            }
        }
    }

    protected final void displayError0(Throwable throwable) {
        Debugger.printStackTrace(throwable);
        Common.log(new String[]{"&4    ___                  _ ", "&4   / _ \\  ___  _ __  ___| |", "&4  | | | |/ _ \\| '_ \\/ __| |", "&4  | |_| | (_) | |_) \\__ \\_|", "&4   \\___/ \\___/| .__/|___(_)", "&4             |_|          ", "&4!-----------------------------------------------------!", " &cError loading " + this.getDescription().getName() + " v" + this.getDescription().getVersion() + ", plugin is disabled!", " &cRunning on " + this.getServer().getBukkitVersion() + " (" + MinecraftVersion.getServerVersion() + ") & Java " + System.getProperty("java.version"), "&4!-----------------------------------------------------!"});
        if (throwable instanceof InvalidConfigurationException) {
            Common.log(new String[]{" &cSeems like your config is not a valid YAML."});
            Common.log(new String[]{" &cUse online services like"});
            Common.log(new String[]{" &chttp://yaml-online-parser.appspot.com/"});
            Common.log(new String[]{" &cto check for syntax errors!"});
        } else if (throwable instanceof UnsupportedOperationException || throwable.getCause() != null && throwable.getCause() instanceof UnsupportedOperationException) {
            if (this.getServer().getBukkitVersion().startsWith("1.2.5")) {
                Common.log(new String[]{" &cSorry but Minecraft 1.2.5 is no longer supported!"});
            } else {
                Common.log(new String[]{" &cUnable to setup reflection!"});
                Common.log(new String[]{" &cYour server is either too old or"});
                Common.log(new String[]{" &cthe plugin broke on the new version :("});
            }
        }

        while(throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        String error = "Unable to get the error message, search above.";
        if (throwable.getMessage() != null && !throwable.getMessage().isEmpty() && !throwable.getMessage().equals("null")) {
            error = throwable.getMessage();
        }

        Common.log(new String[]{" &cError: " + error});
        Common.log(new String[]{"&4!-----------------------------------------------------!"});
        this.getPluginLoader().disablePlugin(this);
    }

    public final void onDisable() {
        if (this.isEnabled) {
            try {
                this.onPluginStop();
            } catch (Throwable var4) {
                Common.log(new String[]{"&cPlugin might not shut down property. Got " + var4.getClass().getSimpleName() + ": " + var4.getMessage()});
            }

            this.unregisterReloadables();

            Iterator var1;
            Player online;
            try {
                var1 = Remain.getOnlinePlayers().iterator();

                while(var1.hasNext()) {
                    online = (Player)var1.next();
                    SimpleScoreboard.clearBoardsFor(online);
                }
            } catch (Throwable var6) {
                Common.log(new String[]{"Error clearing scoreboards for players.."});
                var6.printStackTrace();
            }

            try {
                var1 = Remain.getOnlinePlayers().iterator();

                while(var1.hasNext()) {
                    online = (Player)var1.next();
                    Menu menu = Menu.getMenu(online);
                    if (menu != null) {
                        online.closeInventory();
                    }
                }
            } catch (Throwable var5) {
                Common.log(new String[]{"Error closing menu inventories for players.."});
                var5.printStackTrace();
            }

            Objects.requireNonNull(instance, "Instance of " + this.getName() + " already nulled!");
            instance = null;
        }
    }

    protected void onPluginLoad() {
    }

    protected void onPluginPreStart() {
    }

    protected abstract void onPluginStart();

    protected void onPluginStop() {
    }

    protected void onPluginPreReload() {
    }

    protected void onPluginReload() {
    }

    protected void onReloadablesStart() {
    }

    public final void reload() {
        boolean hadLogPrefix = Common.ADD_LOG_PREFIX;
        Common.ADD_LOG_PREFIX = false;
        Common.log(new String[]{Common.consoleLineSmooth()});
        Common.log(new String[]{" "});
        Common.log(new String[]{"Reloading plugin " + this.getName() + " v" + getVersion()});
        Common.log(new String[]{" "});
        reloading = true;

        try {
            Debugger.detectDebugMode();
            this.unregisterReloadables();

            try {
                HookManager.loadDependencies();
            } catch (Throwable var7) {
                Common.throwError(var7, new String[]{"Error while loading " + this.getName() + " dependencies!"});
            }

            this.onPluginPreReload();
            this.reloadables.reload();
            YamlConfig.clearLoadedFiles();
            if (this.getSettings() != null) {
                YamlStaticConfig.load(this.getSettings());
            }

            MetadataFile.onReload();
            FoundationPacketListener.addPacketListener();
            Common.setTellPrefix(SimpleSettings.PLUGIN_PREFIX);
            this.onPluginReload();
            if (this.isEnabled && this.isEnabled()) {
                if (this.getMainCommand() != null) {
                    this.reloadables.registerCommands(SimpleSettings.MAIN_COMMAND_ALIASES, this.getMainCommand());
                }

                this.startingReloadables = true;
                this.onReloadablesStart();
                this.startingReloadables = false;
                if (HookManager.isDiscordSRVLoaded()) {
                    DiscordListenerImpl.getInstance().resubscribe();
                    this.reloadables.registerEvents(DiscordListenerImpl.getInstance());
                }

                this.registerBungeeCord();
                Common.log(new String[]{Common.consoleLineSmooth()});
                return;
            }
        } catch (Throwable var8) {
            Common.throwError(var8, new String[]{"Error reloading " + this.getName() + " " + getVersion()});
            return;
        } finally {
            Common.ADD_LOG_PREFIX = hadLogPrefix;
            reloading = false;
        }

    }

    private final void unregisterReloadables() {
        SimpleSettings.resetSettingsCall();
        SimpleLocalization.resetLocalizationCall();
        BlockVisualizer.stopAll();
        FolderWatcher.stopThreads();
        DiscordListener.clearRegisteredListeners();

        try {
            HookManager.unloadDependencies(this);
        } catch (NoClassDefFoundError var2) {
        }

        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getScheduler().cancelTasks(this);
    }

    protected final void registerEventsIf(Listener listener, boolean condition) {
        if (condition) {
            if (this.startingReloadables) {
                this.reloadables.registerEvents(listener);
            } else {
                this.registerEvents(listener);
            }
        }

    }

    protected final void registerEvents(Listener listener) {
        if (this.startingReloadables) {
            this.reloadables.registerEvents(listener);
        } else {
            this.getServer().getPluginManager().registerEvents(listener, this);
        }

        if (listener instanceof DiscordListener) {
            ((DiscordListener)listener).register();
        }

    }

    protected final void registerEventsIf(SimpleListener<? extends Event> listener, boolean condition) {
        if (condition) {
            if (this.startingReloadables) {
                this.reloadables.registerEvents(listener);
            } else {
                this.registerEvents(listener);
            }
        }

    }

    protected final void registerEvents(SimpleListener<? extends Event> listener) {
        if (this.startingReloadables) {
            this.reloadables.registerEvents(listener);
        } else {
            listener.register();
        }

    }

    protected final void registerCommand(Command command) {
        Remain.registerCommand(command);
    }

    protected final void registerCommand(SimpleCommand command) {
        command.register();
    }

    protected final void registerCommands(String labelAndAliases, SimpleCommandGroup group) {
        this.registerCommands(new StrictList(labelAndAliases.split("\\|")), group);
    }

    protected final void registerCommands(StrictList<String> labelAndAliases, SimpleCommandGroup group) {
        Valid.checkBoolean(!labelAndAliases.isEmpty(), "Must specify at least label for command group: " + group, new Object[0]);
        if (this.getMainCommand() != null && this.getMainCommand().getLabel().equals(labelAndAliases.get(0))) {
            throw new FoException("Your main command group is registered automatically!");
        } else {
            this.reloadables.registerCommands(labelAndAliases, group);
        }
    }

    protected String[] getStartupLogo() {
        return null;
    }

    public V getMinimumVersion() {
        return null;
    }

    public V getMaximumVersion() {
        return null;
    }

    public List<Class<? extends YamlStaticConfig>> getSettings() {
        return null;
    }

    public SimpleCommandGroup getMainCommand() {
        return null;
    }

    public int getFoundedYear() {
        return -1;
    }

    public SpigotUpdater getUpdateCheck() {
        return null;
    }

    public int getMetricsPluginId() {
        return -1;
    }

    public int getRegexTimeout() {
        throw new FoException("Must override getRegexTimeout()");
    }

    public boolean regexStripColors() {
        return true;
    }

    public boolean regexCaseInsensitive() {
        return true;
    }

    public boolean regexUnicode() {
        return true;
    }

    public boolean regexStripAccents() {
        return true;
    }

    public boolean similarityStripAccents() {
        return true;
    }

    public SimpleBungee getBungeeCord() {
        return null;
    }

    public boolean enforeNewLine() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public boolean areScriptVariablesEnabled() {
        return false;
    }

    protected final File getFile() {
        return super.getFile();
    }

    /** @deprecated */
    @Deprecated
    public final PluginCommand getCommand(String name) {
        return super.getCommand(name);
    }

    /** @deprecated */
    @Deprecated
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        throw this.unsupported("onCommand");
    }

    /** @deprecated */
    @Deprecated
    public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        throw this.unsupported("onTabComplete");
    }

    /** @deprecated */
    @Deprecated
    public final FileConfiguration getConfig() {
        throw this.unsupported("getConfig");
    }

    /** @deprecated */
    @Deprecated
    public final void saveConfig() {
        throw this.unsupported("saveConfig");
    }

    /** @deprecated */
    @Deprecated
    public final void saveDefaultConfig() {
        throw this.unsupported("saveDefaultConfig");
    }

    /** @deprecated */
    @Deprecated
    public final void reloadConfig() {
        throw new FoException("Cannot call reloadConfig in " + this.getName() + ", use reload()!");
    }

    private final FoException unsupported(String method) {
        return new FoException("Cannot call " + method + " in " + this.getName() + ", use YamlConfig or SimpleCommand classes in Foundation for that!");
    }

    public static String getVersion() {
        return version;
    }

    public static String getNamed() {
        return named;
    }

    public static File getSource() {
        return source;
    }

    public static File getData() {
        return data;
    }

    public static boolean isReloading() {
        return reloading;
    }

    private final class ShadingException extends Throwable {
        private static final long serialVersionUID = 1L;

        public ShadingException() {
            if (!SimplePlugin.getNamed().equals(SimplePlugin.this.getDescription().getName())) {
                Bukkit.getLogger().severe(Common.consoleLine());
                Bukkit.getLogger().severe("We have a class path problem in the Foundation library");
                Bukkit.getLogger().severe("preventing " + SimplePlugin.this.getDescription().getName() + " from loading correctly!");
                Bukkit.getLogger().severe("");
                Bukkit.getLogger().severe("This is likely caused by two plugins having the");
                Bukkit.getLogger().severe("same Foundation library paths - make sure you");
                Bukkit.getLogger().severe("relocale the package! If you are testing using");
                Bukkit.getLogger().severe("Ant, only test one plugin at the time.");
                Bukkit.getLogger().severe("");
                Bukkit.getLogger().severe("Possible cause: " + SimplePlugin.getNamed());
                Bukkit.getLogger().severe("Foundation package: " + SimplePlugin.class.getPackage().getName());
                Bukkit.getLogger().severe(Common.consoleLine());
                SimplePlugin.this.isEnabled = false;
            }

        }
    }
}