package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class HookManager {
    private static AuthMeHook authMeHook;
    private static BanManagerHook banManagerHook;
    private static BossHook bossHook;
    private static CitizensHook citizensHook;
    private static CMIHook CMIHook;
    private static DiscordSRVHook discordSRVHook;
    private static EssentialsHook essentialsHook;
    private static FactionsHook factionsHook;
    private static ItemsAdderHook itemsAdderHook;
    private static LandsHook landsHook;
    private static LiteBansHook liteBansHook;
    private static LocketteProHook locketteProHook;
    private static LWCHook lwcHook;
    private static McMMOHook mcmmoHook;
    private static MultiverseHook multiverseHook;
    private static MVdWPlaceholderHook MVdWPlaceholderHook;
    private static MythicMobsHook mythicMobsHook;
    private static NickyHook nickyHook;
    private static PlaceholderAPIHook placeholderAPIHook;
    private static PlotSquaredHook plotSquaredHook;
    private static ProtocolLibHook protocolLibHook;
    private static ResidenceHook residenceHook;
    private static TownyHook townyHook;
    private static VaultHook vaultHook;
    private static WorldEditHook worldeditHook;
    private static WorldGuardHook worldguardHook;
    private static boolean nbtAPIDummyHook = false;
    private static boolean nuVotifierDummyHook = false;
    private static boolean townyChatDummyHook = false;

    public static void loadDependencies() {
        if (Common.doesPluginExist("AuthMe")) {
            authMeHook = new AuthMeHook();
        }

        if (Common.doesPluginExist("BanManager")) {
            banManagerHook = new BanManagerHook();
        }

        if (Common.doesPluginExist("Boss")) {
            bossHook = new BossHook();
        }

        if (Common.doesPluginExist("Citizens")) {
            citizensHook = new CitizensHook();
        }

        if (Common.doesPluginExist("CMI")) {
            CMIHook = new CMIHook();
        }

        if (Common.doesPluginExist("DiscordSRV")) {
            try {
                Class.forName("github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel");
                discordSRVHook = new DiscordSRVHook();
            } catch (ClassNotFoundException var7) {
                Common.error(var7, new String[]{"&c" + SimplePlugin.getNamed() + " failed to hook DiscordSRV because the plugin is outdated (1.18.x is supported)!"});
            }
        }

        if (Common.doesPluginExist("Essentials")) {
            essentialsHook = new EssentialsHook();
        }

        Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");
        String ver;
        if (Common.doesPluginExist("FactionsX") && factions == null) {
            Common.log(new String[]{"Note: If you want FactionX integration, install FactionsUUIDAPIProxy."});
        } else if (factions != null) {
            ver = factions.getDescription().getVersion();
            String main = factions.getDescription().getMain();
            if (!ver.startsWith("1.6") && !main.contains("FactionsUUIDAPIProxy")) {
                if (ver.startsWith("2.")) {
                    Class mplayer = null;

                    try {
                        mplayer = Class.forName("com.massivecraft.factions.entity.MPlayer");
                    } catch (ClassNotFoundException var6) {
                    }

                    if (mplayer != null) {
                        factionsHook = new FactionsMassive();
                    } else {
                        Common.log(new String[]{"&cWarning: &fRecognized MCore Factions, but not hooked! Check if you have the latest version!"});
                    }
                }
            } else {
                factionsHook = new FactionsUUID();
            }
        }

        if (Common.doesPluginExist("ItemsAdder")) {
            itemsAdderHook = new ItemsAdderHook();
        }

        if (Common.doesPluginExist("Lands")) {
            landsHook = new LandsHook();
        }

        if (Common.doesPluginExist("LiteBans")) {
            liteBansHook = new LiteBansHook();
        }

        if (Common.doesPluginExist("Lockette")) {
            locketteProHook = new LocketteProHook();
        }

        if (Common.doesPluginExist("LWC")) {
            lwcHook = new LWCHook();
        }

        if (Common.doesPluginExist("mcMMO")) {
            ver = Bukkit.getPluginManager().getPlugin("mcMMO").getDescription().getVersion();
            if (ver.startsWith("2.")) {
                mcmmoHook = new McMMOHook();
            } else {
                Common.log(new String[]{"&cWarning: &fCould not hook into mcMMO, version 2.x required, you have " + ver});
            }
        }

        if (Common.doesPluginExist("Multiverse-Core")) {
            multiverseHook = new MultiverseHook();
        }

        if (Common.doesPluginExist("MVdWPlaceholderAPI")) {
            MVdWPlaceholderHook = new MVdWPlaceholderHook();
        }

        if (Common.doesPluginExist("MythicMobs")) {
            mythicMobsHook = new MythicMobsHook();
        }

        if (Common.doesPluginExist("Nicky")) {
            nickyHook = new NickyHook();
        }

        if (Common.doesPluginExist("PlaceholderAPI")) {
            placeholderAPIHook = new PlaceholderAPIHook();
        }

        if (Common.doesPluginExist("PlotSquared")) {
            ver = Bukkit.getPluginManager().getPlugin("PlotSquared").getDescription().getVersion();
            if (!ver.startsWith("5.") && !ver.startsWith("3.")) {
                Common.log(new String[]{"&cWarning: &fCould not hook into PlotSquared, version 3.x or 5.x required, you have " + ver});
            } else {
                plotSquaredHook = new PlotSquaredHook();
            }
        }

        if (Common.doesPluginExist("ProtocolLib")) {
            protocolLibHook = new ProtocolLibHook();

            try {
                if (MinecraftVersion.newerThan(V.v1_6)) {
                    Class.forName("com.comphenix.protocol.wrappers.WrappedChatComponent");
                }
            } catch (Throwable var5) {
                protocolLibHook = null;
                Common.throwError(var5, new String[]{"You are running an old and unsupported version of ProtocolLib, please update it."});
            }
        }

        if (Common.doesPluginExist("Residence")) {
            residenceHook = new ResidenceHook();
        }

        if (Common.doesPluginExist("Towny")) {
            townyHook = new TownyHook();
        }

        if (Common.doesPluginExist("Vault")) {
            vaultHook = new VaultHook();
        }

        if (Common.doesPluginExist("WorldEdit") || Common.doesPluginExist("FastAsyncWorldEdit")) {
            worldeditHook = new WorldEditHook();
        }

        if (Common.doesPluginExist("WorldGuard")) {
            worldguardHook = new WorldGuardHook(worldeditHook);
        }

        if (Common.doesPluginExist("NBTAPI")) {
            nbtAPIDummyHook = true;
        }

        if (Common.doesPluginExist("Votifier")) {
            nuVotifierDummyHook = true;
        }

        if (Common.doesPluginExist("TownyChat")) {
            townyChatDummyHook = true;
        }

    }

    /** @deprecated */
    @Deprecated
    public static void unloadDependencies(Plugin plugin) {
        if (isProtocolLibLoaded()) {
            protocolLibHook.removePacketListeners(plugin);
        }

        if (isPlaceholderAPILoaded()) {
            placeholderAPIHook.unregister();
        }

    }

    public static boolean isAuthMeLoaded() {
        return authMeHook != null;
    }

    public static boolean isBanManagerLoaded() {
        return banManagerHook != null;
    }

    public static boolean isBossLoaded() {
        return bossHook != null;
    }

    public static boolean isCMILoaded() {
        return CMIHook != null;
    }

    public static boolean isCitizensLoaded() {
        return citizensHook != null;
    }

    public static boolean isDiscordSRVLoaded() {
        return discordSRVHook != null;
    }

    public static boolean isEssentialsLoaded() {
        return essentialsHook != null;
    }

    public static boolean isFactionsLoaded() {
        return factionsHook != null;
    }

    public static boolean isFAWELoaded() {
        Plugin fawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        if (fawe != null && fawe.isEnabled()) {
            return true;
        } else {
            Plugin worldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit");
            return worldEdit != null && worldEdit.isEnabled() && "Fast Async WorldEdit plugin".equals(worldEdit.getDescription().getDescription());
        }
    }

    public static boolean isItemsAdderLoaded() {
        return itemsAdderHook != null;
    }

    public static boolean isLandsLoaded() {
        return landsHook != null;
    }

    public static boolean isLiteBansLoaded() {
        return liteBansHook != null;
    }

    public static boolean isLocketteProLoaded() {
        return locketteProHook != null;
    }

    public static boolean isLWCLoaded() {
        return lwcHook != null;
    }

    public static boolean isMcMMOLoaded() {
        return mcmmoHook != null;
    }

    public static boolean isMultiverseCoreLoaded() {
        return multiverseHook != null;
    }

    public static boolean isMVdWPlaceholderAPILoaded() {
        return MVdWPlaceholderHook != null;
    }

    public static boolean isMythicMobsLoaded() {
        return mythicMobsHook != null;
    }

    public static boolean isNbtAPILoaded() {
        return nbtAPIDummyHook;
    }

    public static boolean isNickyLoaded() {
        return nickyHook != null;
    }

    public static boolean isNuVotifierLoaded() {
        return nuVotifierDummyHook;
    }

    public static boolean isPlaceholderAPILoaded() {
        return placeholderAPIHook != null;
    }

    public static boolean isPlotSquaredLoaded() {
        return plotSquaredHook != null;
    }

    public static boolean isProtocolLibLoaded() {
        return protocolLibHook != null;
    }

    public static boolean isResidenceLoaded() {
        return residenceHook != null;
    }

    public static boolean isTownyLoaded() {
        return townyHook != null;
    }

    public static boolean isTownyChatLoaded() {
        return townyHook != null && townyChatDummyHook;
    }

    public static boolean isVaultLoaded() {
        return vaultHook != null;
    }

    public static boolean isWorldEditLoaded() {
        return worldeditHook != null || isFAWELoaded();
    }

    public static boolean isWorldGuardLoaded() {
        return worldguardHook != null;
    }

    public static boolean isLogged(Player player) {
        return !isAuthMeLoaded() || authMeHook.isLogged(player);
    }

    public static String getBossName(Entity entity) {
        return isBossLoaded() ? bossHook.getBossName(entity) : null;
    }

    public static String getMythicMobName(Entity entity) {
        return isMythicMobsLoaded() ? mythicMobsHook.getBossName(entity) : null;
    }

    public static Collection<Player> getLandPlayers(Player player) {
        return (Collection)(isLandsLoaded() ? landsHook.getLandPlayers(player) : new ArrayList());
    }

    public static boolean isAfk(Player player) {
        boolean essAFK = isEssentialsLoaded() && essentialsHook.isAfk(player.getName());
        boolean cmiAFK = isCMILoaded() && CMIHook.isAfk(player);
        return essAFK || cmiAFK;
    }

    /** @deprecated */
    @Deprecated
    public static boolean isVanished(Player player) {
        if (isEssentialsLoaded() && essentialsHook.isVanished(player.getName())) {
            return true;
        } else {
            return isCMILoaded() && CMIHook.isVanished(player);
        }
    }

    public static boolean isMuted(Player player) {
        if (isEssentialsLoaded() && essentialsHook.isMuted(player.getName())) {
            return true;
        } else if (isCMILoaded() && CMIHook.isMuted(player)) {
            return true;
        } else if (isBanManagerLoaded() && banManagerHook.isMuted(player)) {
            return true;
        } else {
            return isLiteBansLoaded() && liteBansHook.isMuted(player);
        }
    }

    public static void setLiteBansMute(Player player, String durationTokenized, String reason) {
        if (isLiteBansLoaded()) {
            Common.dispatchCommand(player, "lmute {player} " + durationTokenized + (reason != null && !reason.isEmpty() ? " " + reason : ""));
        }

    }

    public static void setLiteBansUnmute(Player player) {
        if (isLiteBansLoaded()) {
            Common.dispatchCommand(player, "lunmute {player}");
        }

    }

    public static void setGodMode(Player player, boolean godMode) {
        if (isEssentialsLoaded()) {
            essentialsHook.setGodMode(player, godMode);
        }

        if (isCMILoaded()) {
            CMIHook.setGodMode(player, godMode);
        }

    }

    public static void setBackLocation(Player player, Location location) {
        if (isEssentialsLoaded()) {
            essentialsHook.setBackLocation(player.getName(), location);
        }

        if (isCMILoaded()) {
            CMIHook.setLastTeleportLocation(player, location);
        }

    }

    public static void setIgnore(UUID player, UUID who, boolean ignore) {
        if (isEssentialsLoaded()) {
            essentialsHook.setIgnore(player, who, ignore);
        }

        if (isCMILoaded()) {
            CMIHook.setIgnore(player, who, ignore);
        }

    }

    public static boolean isIgnoring(UUID player, UUID who) {
        Valid.checkBoolean(player != null, "Player to check ignore from cannot be null/empty", new Object[0]);
        Valid.checkBoolean(who != null, "Player to check ignore to cannot be null/empty", new Object[0]);
        return isEssentialsLoaded() ? essentialsHook.isIgnoring(player, who) : (isCMILoaded() ? CMIHook.isIgnoring(player, who) : false);
    }

    public static String getNickColored(CommandSender sender) {
        return getNick(sender, false);
    }

    public static String getNickColorless(CommandSender sender) {
        return getNick(sender, true);
    }

    private static String getNick(CommandSender sender, boolean stripColors) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player != null && isNPC(player)) {
            Common.log(new String[]{"&eWarn: Called getNick for NPC " + player.getName() + "! Notify the developers to add an ignore check at " + Debugger.traceRoute(true)});
            return player.getName();
        } else if (player == null) {
            return sender.getName();
        } else {
            String nickyNick = isNickyLoaded() ? nickyHook.getNick(player) : null;
            String essNick = isEssentialsLoaded() ? essentialsHook.getNick(player.getName()) : null;
            String cmiNick = isCMILoaded() ? CMIHook.getNick(player) : null;
            String nick = nickyNick != null ? nickyNick : (cmiNick != null ? cmiNick : (essNick != null ? essNick : sender.getName()));
            return stripColors ? Common.stripColors(Common.revertColorizing(nick).replace("§x", "")) : nick;
        }
    }

    public static void setNick(@NonNull UUID playerId, @Nullable String nick) {
        if (playerId == null) {
            throw new NullPointerException("playerId is marked non-null but is null");
        } else {
            if (isEssentialsLoaded()) {
                essentialsHook.setNick(playerId, nick);
            }

            if (isCMILoaded()) {
                CMIHook.setNick(playerId, nick);
            }

        }
    }

    public static String getNameFromNick(@NonNull String nick) {
        if (nick == null) {
            throw new NullPointerException("nick is marked non-null but is null");
        } else {
            String essNick = isEssentialsLoaded() ? essentialsHook.getNameFromNick(nick) : nick;
            String cmiNick = isCMILoaded() ? CMIHook.getNameFromNick(nick) : nick;
            return !essNick.equals(nick) && !"".equals(essNick) ? essNick : (!cmiNick.equals(nick) && !"".equals(cmiNick) ? cmiNick : nick);
        }
    }

    public static Player getReplyTo(Player player) {
        return isEssentialsLoaded() ? essentialsHook.getReplyTo(player.getName()) : null;
    }

    public static String replaceFontImages(String message) {
        return replaceFontImages((Player)null, message);
    }

    public static String replaceFontImages(@Nullable Player player, String message) {
        return isItemsAdderLoaded() ? itemsAdderHook.replaceFontImages(player, message) : message;
    }

    public static String getWorldAlias(World world) {
        return isMultiverseCoreLoaded() ? multiverseHook.getWorldAlias(world.getName()) : world.getName();
    }

    public static String getNation(Player player) {
        return isTownyLoaded() ? townyHook.getNationName(player) : null;
    }

    public static String getTownName(Player player) {
        return isTownyLoaded() ? townyHook.getTownName(player) : null;
    }

    public static Collection<? extends Player> getTownResidentsOnline(Player player) {
        return (Collection)(isTownyLoaded() ? townyHook.getTownResidentsOnline(player) : new ArrayList());
    }

    public static Collection<? extends Player> getNationPlayersOnline(Player player) {
        return (Collection)(isTownyLoaded() ? townyHook.getNationPlayersOnline(player) : new ArrayList());
    }

    public static String getTownOwner(Location location) {
        return isTownyLoaded() ? townyHook.getTownOwner(location) : null;
    }

    public static String getTown(Location location) {
        return isTownyLoaded() ? townyHook.getTownName(location) : null;
    }

    public static List<String> getTowns() {
        return (List)(isTownyLoaded() ? townyHook.getTowns() : new ArrayList());
    }

    public static String getPlayerPrefix(Player player) {
        return isVaultLoaded() ? vaultHook.getPlayerPrefix(player) : "";
    }

    public static String getPlayerSuffix(Player player) {
        return isVaultLoaded() ? vaultHook.getPlayerSuffix(player) : "";
    }

    public static String getPlayerPermissionGroup(Player player) {
        return isVaultLoaded() ? vaultHook.getPlayerGroup(player) : "";
    }

    public static double getBalance(Player player) {
        return isVaultLoaded() ? vaultHook.getBalance(player) : 0.0D;
    }

    public static String getCurrencySingular() {
        return isVaultLoaded() ? vaultHook.getCurrencyNameSG() : null;
    }

    public static String getCurrencyPlural() {
        return isVaultLoaded() ? vaultHook.getCurrencyNamePL() : null;
    }

    public static void withdraw(Player player, double amount) {
        if (isVaultLoaded()) {
            vaultHook.withdraw(player, amount);
        }

    }

    public static void deposit(Player player, double amount) {
        if (isVaultLoaded()) {
            vaultHook.deposit(player, amount);
        }

    }

    public static boolean hasProtocolLibPermission(Player player, String perm) {
        return isProtocolLibLoaded() && protocolLibHook.isTemporaryPlayer(player) ? hasVaultPermission(player, perm) : PlayerUtil.hasPerm(player, perm);
    }

    public static boolean hasVaultPermission(OfflinePlayer offlinePlayer, String perm) {
        Valid.checkBoolean(isVaultLoaded(), "hasVaultPermission called - Please install Vault to enable this functionality!", new Object[0]);
        return vaultHook.hasPerm(offlinePlayer, perm);
    }

    public static String getPlayerPrimaryGroup(Player player) {
        return isVaultLoaded() ? vaultHook.getPrimaryGroup(player) : "";
    }

    public static boolean isChatIntegrated() {
        return isVaultLoaded() ? vaultHook.isChatIntegrated() : false;
    }

    public static boolean isEconomyIntegrated() {
        return isVaultLoaded() ? vaultHook.isEconomyIntegrated() : false;
    }

    /** @deprecated */
    @Deprecated
    public static void updateVaultIntegration() {
        if (isVaultLoaded()) {
            vaultHook.setIntegration();
        }

    }

    public static String replacePlaceholders(Player player, String message) {
        if (message != null && !"".equals(message.trim())) {
            message = isPlaceholderAPILoaded() ? placeholderAPIHook.replacePlaceholders(player, message) : message;
            message = isMVdWPlaceholderAPILoaded() ? MVdWPlaceholderHook.replacePlaceholders(player, message) : message;
            return message;
        } else {
            return message;
        }
    }

    public static String replaceRelationPlaceholders(Player one, Player two, String message) {
        if (message != null && !"".equals(message.trim())) {
            return isPlaceholderAPILoaded() ? placeholderAPIHook.replaceRelationPlaceholders(one, two, message) : message;
        } else {
            return message;
        }
    }

    public static void addPlaceholder(final String variable, final Function<Player, String> value) {
        Variables.addExpansion(new SimpleExpansion() {
            protected String onReplace(@NonNull CommandSender sender, String identifier) {
                if (sender == null) {
                    throw new NullPointerException("sender is marked non-null but is null");
                } else {
                    return variable.equalsIgnoreCase(identifier) && sender instanceof Player ? (String)value.apply((Player)sender) : null;
                }
            }
        });
    }

    public static Collection<String> getFactions() {
        return isFactionsLoaded() ? factionsHook.getFactions() : null;
    }

    public static String getFaction(Player player) {
        return isFactionsLoaded() ? factionsHook.getFaction(player) : null;
    }

    public static Collection<? extends Player> getOnlineFactionPlayers(Player player) {
        return (Collection)(isFactionsLoaded() ? factionsHook.getSameFactionPlayers(player) : new ArrayList());
    }

    public static String getFaction(Location location) {
        return isFactionsLoaded() ? factionsHook.getFaction(location) : null;
    }

    public static String getFactionOwner(Location location) {
        return isFactionsLoaded() ? factionsHook.getFactionOwner(location) : null;
    }

    public static void addPacketListener(Object adapter) {
        Valid.checkBoolean(isProtocolLibLoaded(), "Cannot add packet listeners if ProtocolLib isn't installed", new Object[0]);
        protocolLibHook.addPacketListener(adapter);
    }

    public static void sendPacket(Player player, Object packetContainer) {
        Valid.checkBoolean(isProtocolLibLoaded(), "Sending packets requires ProtocolLib installed and loaded", new Object[0]);
        protocolLibHook.sendPacket(player, packetContainer);
    }

    public static String getLWCOwner(Block block) {
        return isLWCLoaded() ? lwcHook.getOwner(block) : null;
    }

    public static boolean isLocketteOwner(Block block, Player player) {
        return isLocketteProLoaded() ? locketteProHook.isOwner(block, player) : false;
    }

    public static Collection<String> getResidences() {
        return (Collection)(isResidenceLoaded() ? residenceHook.getResidences() : new ArrayList());
    }

    public static String getResidence(Location location) {
        return isResidenceLoaded() ? residenceHook.getResidence(location) : null;
    }

    public static String getResidenceOwner(Location location) {
        return isResidenceLoaded() ? residenceHook.getResidenceOwner(location) : null;
    }

    public static List<String> getRegions(Location loc) {
        return (List)(isWorldGuardLoaded() ? worldguardHook.getRegionsAt(loc) : new ArrayList());
    }

    public static List<String> getRegions() {
        return (List)(isWorldGuardLoaded() ? worldguardHook.getAllRegions() : new ArrayList());
    }

    public static Region getRegion(String name) {
        return isWorldGuardLoaded() ? worldguardHook.getRegion(name) : null;
    }

    public static Collection<? extends Player> getPlotPlayers(Player players) {
        return (Collection)(isPlotSquaredLoaded() ? plotSquaredHook.getPlotPlayers(players) : new ArrayList());
    }

    public static String getActivePartyChat(Player player) {
        return isMcMMOLoaded() ? mcmmoHook.getActivePartyChat(player) : null;
    }

    public static List<Player> getMcMMOPartyRecipients(Player player) {
        return (List)(isMcMMOLoaded() ? mcmmoHook.getPartyRecipients(player) : new ArrayList());
    }

    public static boolean isNPC(Entity entity) {
        return isCitizensLoaded() ? citizensHook.isNPC(entity) : false;
    }

    public static Set<String> getDiscordChannels() {
        return (Set)(isDiscordSRVLoaded() ? discordSRVHook.getChannels() : new HashSet());
    }

    public static void sendDiscordMessage(CommandSender sender, String channel, @NonNull String message) {
        if (message == null) {
            throw new NullPointerException("message is marked non-null but is null");
        } else {
            if (isDiscordSRVLoaded() && !Common.stripColors(message).isEmpty()) {
                discordSRVHook.sendMessage(sender, channel, message);
            }

        }
    }

    public static void sendDiscordMessage(String channel, @NonNull String message) {
        if (message == null) {
            throw new NullPointerException("message is marked non-null but is null");
        } else {
            if (isDiscordSRVLoaded() && !Common.stripColors(message).isEmpty()) {
                discordSRVHook.sendMessage(channel, message);
            }

        }
    }

    private HookManager() {
    }
}