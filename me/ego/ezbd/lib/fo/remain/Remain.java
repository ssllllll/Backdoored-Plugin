package me.ego.ezbd.lib.fo.remain;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.ItemUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.ReflectionUtil.ReflectionException;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.UUIDToNameConverter;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.internal.BossBarInternals;
import me.ego.ezbd.lib.fo.remain.internal.ChatInternals;
import me.ego.ezbd.lib.fo.remain.internal.ParticleInternals;
import me.ego.ezbd.lib.fo.remain.nbt.NBTInternals;
import me.ego.ezbd.lib.fo.settings.SimpleYaml;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.Statistic.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public final class Remain {
    private static final Pattern RGB_HEX_ENCODED_REGEX = Pattern.compile("(?i)(§x)((§[0-9A-F]){6})");
    private static final Gson gson = new Gson();
    private static final Method getPlayersMethod;
    private static final Method getHealthMethod;
    private static Method getHandle;
    private static Field fieldPlayerConnection;
    private static Method sendPacket;
    private static boolean isGetPlayersCollection = false;
    private static boolean isGetHealthDouble = false;
    private static boolean hasExtendedPlayerTitleAPI = false;
    private static boolean hasParticleAPI = true;
    private static boolean newScoreboardAPI = true;
    private static boolean hasBookEvent = true;
    private static boolean hasInventoryLocation = true;
    private static boolean hasScoreboardTags = true;
    private static boolean hasSpawnEggMeta = true;
    private static boolean hasAdvancements = true;
    private static boolean hasYamlReaderLoad = true;
    private static boolean bungeeApiPresent = true;
    private static boolean hasItemMeta = true;
    private static final StrictMap<UUID, StrictMap<Material, Integer>> cooldowns = new StrictMap();
    private static String serverName;

    private Remain() {
    }

    public static Object getHandleWorld(World world) {
        Object nms = null;
        Method handle = ReflectionUtil.getMethod(world.getClass(), "getHandle");

        try {
            nms = handle.invoke(world);
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
        }

        return nms;
    }

    public static Object getHandleEntity(Entity entity) {
        Object nms_entity = null;
        Method handle = ReflectionUtil.getMethod(entity.getClass(), "getHandle");

        try {
            nms_entity = handle.invoke(entity);
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
        }

        return nms_entity;
    }

    public static boolean isProtocol18Hack() {
        try {
            ReflectionUtil.getNMSClass("PacketPlayOutEntityTeleport").getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Byte.TYPE, Byte.TYPE, Boolean.TYPE, Boolean.TYPE);
            return true;
        } catch (Throwable var1) {
            return false;
        }
    }

    public static void sendPacket(Player player, Object packet) {
        if (getHandle != null && fieldPlayerConnection != null && sendPacket != null) {
            try {
                Object handle = getHandle.invoke(player);
                Object playerConnection = fieldPlayerConnection.get(handle);
                sendPacket.invoke(playerConnection, packet);
            } catch (ReflectiveOperationException var4) {
                throw new ReflectionException("Could not send " + packet.getClass().getSimpleName() + " to " + player.getName(), var4);
            }
        } else {
            Common.log(new String[]{"Cannot send packet " + packet.getClass().getSimpleName() + " on your server sofware (known to be broken on Cauldron)."});
        }
    }

    public static int getHealth(LivingEntity entity) {
        return isGetHealthDouble ? (int)entity.getHealth() : getHealhLegacy(entity);
    }

    public static int getMaxHealth(LivingEntity entity) {
        return isGetHealthDouble ? (int)entity.getMaxHealth() : getMaxHealhLegacy(entity);
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        return (Collection)(isGetPlayersCollection ? Bukkit.getOnlinePlayers() : Arrays.asList(getPlayersLegacy()));
    }

    public static FallingBlock spawnFallingBlock(Block block) {
        return spawnFallingBlock(block.getLocation().add(0.5D, 0.0D, 0.5D), block.getType(), block.getData());
    }

    public static FallingBlock spawnFallingBlock(Location loc, Block block) {
        if (MinecraftVersion.atLeast(V.v1_13)) {
            return loc.getWorld().spawnFallingBlock(loc, block.getBlockData());
        } else {
            try {
                return (FallingBlock)loc.getWorld().getClass().getMethod("spawnFallingBlock", Location.class, Integer.TYPE, Byte.TYPE).invoke(loc.getWorld(), loc, ReflectionUtil.invoke("getTypeId", block, new Object[0]), block.getData());
            } catch (ReflectiveOperationException var3) {
                var3.printStackTrace();
                return null;
            }
        }
    }

    public static FallingBlock spawnFallingBlock(Location loc, Material material) {
        return spawnFallingBlock(loc, material, (byte)0);
    }

    public static FallingBlock spawnFallingBlock(Location loc, Material material, byte data) {
        if (MinecraftVersion.atLeast(V.v1_13)) {
            return loc.getWorld().spawnFallingBlock(loc, material, data);
        } else {
            try {
                return (FallingBlock)loc.getWorld().getClass().getMethod("spawnFallingBlock", Location.class, Integer.TYPE, Byte.TYPE).invoke(loc.getWorld(), loc, material.getId(), data);
            } catch (ReflectiveOperationException var4) {
                var4.printStackTrace();
                return null;
            }
        }
    }

    /** @deprecated */
    @Deprecated
    public static Item spawnItem(Location location, ItemStack item, Consumer<Item> modifier) {
        try {
            Class<?> nmsWorldClass = ReflectionUtil.getNMSClass("World");
            Class<?> nmsStackClass = ReflectionUtil.getNMSClass("ItemStack");
            Class<?> nmsEntityClass = ReflectionUtil.getNMSClass("Entity");
            Class<?> nmsItemClass = ReflectionUtil.getNMSClass("EntityItem");
            Constructor<?> entityConstructor = nmsItemClass.getConstructor(nmsWorldClass, Double.TYPE, Double.TYPE, Double.TYPE, nmsStackClass);
            Object nmsWorld = location.getWorld().getClass().getMethod("getHandle").invoke(location.getWorld());
            Method asNmsCopy = ReflectionUtil.getOBCClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
            Object nmsEntity = entityConstructor.newInstance(nmsWorld, location.getX(), location.getY(), location.getZ(), asNmsCopy.invoke((Object)null, item));
            Class<?> craftItemClass = ReflectionUtil.getOBCClass("entity.CraftItem");
            Class<?> craftServerClass = ReflectionUtil.getOBCClass("CraftServer");
            Object bukkitItem = craftItemClass.getConstructor(craftServerClass, nmsItemClass).newInstance(Bukkit.getServer(), nmsEntity);
            Valid.checkBoolean(bukkitItem instanceof Item, "Failed to make an dropped item, got " + bukkitItem.getClass().getSimpleName(), new Object[0]);
            modifier.accept((Item)bukkitItem);
            Method addEntity = location.getWorld().getClass().getMethod("addEntity", nmsEntityClass, SpawnReason.class);
            addEntity.invoke(location.getWorld(), nmsEntity, SpawnReason.CUSTOM);
            return (Item)bukkitItem;
        } catch (ReflectiveOperationException var15) {
            Common.error(var15, new String[]{"Error spawning item " + item.getType() + " at " + location});
            return null;
        }
    }

    public static Object asNMSCopy(ItemStack itemStack) {
        try {
            Method asNmsCopy = ReflectionUtil.getOBCClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
            return asNmsCopy.invoke((Object)null, itemStack);
        } catch (ReflectiveOperationException var2) {
            Common.throwError(var2, new String[]{"Unable to convert item to NMS item: " + itemStack});
            return null;
        }
    }

    public static void setData(Block block, int data) {
        try {
            Block.class.getMethod("setData", Byte.TYPE).invoke(block, (byte)data);
        } catch (NoSuchMethodException var3) {
            block.setBlockData(Bukkit.getUnsafe().fromLegacy(block.getType(), (byte)data), true);
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
        }

    }

    public static void setTypeAndData(Block block, CompMaterial material, byte data) {
        setTypeAndData(block, material.getMaterial(), data);
    }

    public static void setTypeAndData(Block block, Material material, byte data) {
        setTypeAndData(block, material, data, true);
    }

    public static void setTypeAndData(Block block, Material material, byte data, boolean physics) {
        if (MinecraftVersion.atLeast(V.v1_13)) {
            block.setType(material);
            block.setBlockData(Bukkit.getUnsafe().fromLegacy(material, data), physics);
        } else {
            try {
                block.getClass().getMethod("setTypeIdAndData", Integer.TYPE, Byte.TYPE, Boolean.TYPE).invoke(block, material.getId(), data, physics);
            } catch (ReflectiveOperationException var5) {
                var5.printStackTrace();
            }
        }

    }

    public static String toLegacyText(String json) throws Remain.InteractiveTextFoundException {
        return toLegacyText(json, true);
    }

    public static String toLegacyText(String json, boolean denyEvents) throws Remain.InteractiveTextFoundException {
        Valid.checkBoolean(bungeeApiPresent, "(Un)packing chat requires Spigot 1.7.10 or newer", new Object[0]);
        StringBuilder text = new StringBuilder();
        if (json.contains("\"translate\"")) {
            return text.append("").toString();
        } else {
            try {
                BaseComponent[] var3 = ComponentSerializer.parse(json);
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    BaseComponent comp = var3[var5];
                    if ((comp.getHoverEvent() != null || comp.getClickEvent() != null) && denyEvents) {
                        throw new Remain.InteractiveTextFoundException();
                    }

                    text.append(comp.toLegacyText());
                }
            } catch (Throwable var7) {
                if (var7 instanceof Remain.InteractiveTextFoundException) {
                    throw var7;
                }
            }

            return text.toString();
        }
    }

    public static String toJson(Collection<String> list) {
        return gson.toJson(list);
    }

    public static List<String> fromJsonList(String json) {
        return (List)gson.fromJson(json, List.class);
    }

    public static String toJson(String message) {
        Valid.checkBoolean(bungeeApiPresent, "(Un)packing chat requires Spigot 1.7.10 or newer", new Object[0]);
        return toJson(TextComponent.fromLegacyText(message));
    }

    public static String toJson(BaseComponent... comps) {
        Valid.checkBoolean(bungeeApiPresent, "(Un)packing chat requires Spigot 1.7.10 or newer", new Object[0]);

        String json;
        try {
            json = ComponentSerializer.toString(comps);
        } catch (Throwable var3) {
            json = (new Gson()).toJson((new TextComponent(comps)).toLegacyText());
        }

        return json;
    }

    public static String toJson(ItemStack item) {
        Class<?> craftItemstack = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemstack, "asNMSCopy", new Class[]{ItemStack.class});
        Class<?> nmsItemStack = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompound = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveItemstackMethod = ReflectionUtil.getMethod(nmsItemStack, "save", new Class[]{nbtTagCompound});
        Object nmsNbtTagCompoundObj = ReflectionUtil.instantiate(nbtTagCompound);
        Object nmsItemStackObj = ReflectionUtil.invoke(asNMSCopyMethod, (Object)null, new Object[]{item});
        Object itemAsJsonObject = ReflectionUtil.invoke(saveItemstackMethod, nmsItemStackObj, new Object[]{nmsNbtTagCompoundObj});
        return itemAsJsonObject.toString();
    }

    public static BaseComponent[] toComponent(String json) {
        Valid.checkBoolean(bungeeApiPresent, "(Un)packing chat requires Spigot 1.7.10 or newer", new Object[0]);

        try {
            return ComponentSerializer.parse(json);
        } catch (Throwable var2) {
            Common.throwError(var2, new String[]{"Failed to call toComponent!", "Json: " + json, "Error: %error%"});
            return null;
        }
    }

    public static void sendJson(CommandSender sender, String json, SerializedMap placeholders) {
        try {
            BaseComponent[] components = ComponentSerializer.parse(json);
            if (MinecraftVersion.atLeast(V.v1_16)) {
                replaceHexPlaceholders(Arrays.asList(components), placeholders);
            }

            sendComponent(sender, components);
        } catch (RuntimeException var4) {
            Common.error(var4, new String[]{"Malformed JSON when sending message to " + sender.getName() + " with JSON: " + json});
        }

    }

    private static void replaceHexPlaceholders(List<BaseComponent> components, SerializedMap placeholders) {
        Iterator var2 = components.iterator();

        while(var2.hasNext()) {
            BaseComponent component = (BaseComponent)var2.next();
            if (component instanceof TextComponent) {
                TextComponent textComponent = (TextComponent)component;
                String text = textComponent.getText();
                Iterator var6 = placeholders.entrySet().iterator();

                while(var6.hasNext()) {
                    Entry<String, Object> entry = (Entry)var6.next();
                    String key = (String)entry.getKey();
                    String value = Common.simplify(entry.getValue());
                    Matcher match = RGB_HEX_ENCODED_REGEX.matcher(text);

                    while(match.find()) {
                        String color = "#" + match.group(2).replace("§", "");
                        value = match.replaceAll("");
                        textComponent.setColor(ChatColor.of(color));
                    }

                    key = key.charAt(0) != '{' ? "{" + key : key;
                    key = key.charAt(key.length() - 1) != '}' ? key + "}" : key;
                    text = text.replace(key, value);
                    textComponent.setText(text);
                }
            }

            if (component.getExtra() != null) {
                replaceHexPlaceholders(component.getExtra(), placeholders);
            }

            if (component.getHoverEvent() != null) {
                replaceHexPlaceholders(Arrays.asList(component.getHoverEvent().getValue()), placeholders);
            }
        }

    }

    public static void sendJson(CommandSender sender, String json) {
        try {
            sendComponent(sender, ComponentSerializer.parse(json));
        } catch (Throwable var3) {
            if (!var3.toString().contains("missing 'text' property")) {
                throw new RuntimeException("Malformed JSON when sending message to " + sender.getName() + " with JSON: " + json, var3);
            }
        }
    }

    public static void sendComponent(CommandSender sender, Object comps) {
        BungeeChatProvider.sendComponent(sender, comps);
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, 20, 60, 20, title, subtitle);
    }

    public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle) {
        if (MinecraftVersion.newerThan(V.v1_7)) {
            if (hasExtendedPlayerTitleAPI) {
                player.sendTitle(Common.colorize(title), Common.colorize(subtitle), fadeIn, stay, fadeOut);
            } else {
                ChatInternals.sendTitleLegacy(player, fadeIn, stay, fadeOut, title, subtitle);
            }
        } else {
            Common.tell(player, new String[]{title});
            Common.tell(player, new String[]{subtitle});
        }

    }

    public static void resetTitle(Player player) {
        if (hasExtendedPlayerTitleAPI) {
            player.resetTitle();
        } else {
            ChatInternals.resetTitleLegacy(player);
        }

    }

    public static void sendTablist(Player player, String header, String footer) {
        Valid.checkBoolean(MinecraftVersion.newerThan(V.v1_7), "Sending tab list requires Minecraft 1.8x or newer!", new Object[0]);
        if (MinecraftVersion.atLeast(V.v1_13)) {
            player.setPlayerListHeaderFooter(Common.colorize(header), Common.colorize(footer));
        } else {
            ChatInternals.sendTablistLegacy(player, header, footer);
        }

    }

    public static void sendActionBar(Player player, String text) {
        if (!MinecraftVersion.newerThan(V.v1_7)) {
            Common.tell(player, new String[]{text});
        } else {
            try {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Common.colorize(text)));
            } catch (NoSuchMethodError var3) {
                ChatInternals.sendActionBarLegacy(player, text);
            }

        }
    }

    public static void sendBossbarPercent(Player player, String message, float percent) {
        sendBossbarPercent(player, message, percent, (CompBarColor)null, (CompBarStyle)null);
    }

    public static void sendBossbarPercent(Player player, String message, float percent, CompBarColor color, CompBarStyle style) {
        BossBarInternals.setMessage(player, message, percent, color, style);
    }

    public static void sendBossbarTimed(Player player, String message, int seconds) {
        sendBossbarTimed(player, message, seconds, (CompBarColor)null, (CompBarStyle)null);
    }

    public static void sendBossbarTimed(Player player, String message, int seconds, CompBarColor color, CompBarStyle style) {
        BossBarInternals.setMessage(player, message, seconds, color, style);
    }

    public static void removeBar(Player player) {
        BossBarInternals.removeBar(player);
    }

    public static PluginCommand newCommand(String label) {
        try {
            Constructor<PluginCommand> con = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            con.setAccessible(true);
            return (PluginCommand)con.newInstance(label, SimplePlugin.getInstance());
        } catch (ReflectiveOperationException var2) {
            throw new FoException(var2, "Unable to create command: /" + label);
        }
    }

    public static void setCommandName(PluginCommand command, String name) {
        try {
            command.setName(name);
        } catch (NoSuchMethodError var3) {
        }

    }

    public static void registerCommand(Command command) {
        CommandMap commandMap = getCommandMap();
        commandMap.register(command.getLabel(), command);
        Valid.checkBoolean(command.isRegistered(), "Command /" + command.getLabel() + " could not have been registered properly!", new Object[0]);
    }

    public static void unregisterCommand(String label) {
        unregisterCommand(label, true);
    }

    public static void unregisterCommand(String label, boolean removeAliases) {
        try {
            PluginCommand command = Bukkit.getPluginCommand(label);
            Field commandField;
            if (command != null) {
                commandField = Command.class.getDeclaredField("commandMap");
                commandField.setAccessible(true);
                if (command.isRegistered()) {
                    command.unregister((CommandMap)commandField.get(command));
                }
            }

            commandField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            commandField.setAccessible(true);
            Map<String, Command> cmdMap = (Map)commandField.get(getCommandMap());
            cmdMap.remove(label);
            if (command != null && removeAliases) {
                Iterator var5 = command.getAliases().iterator();

                while(var5.hasNext()) {
                    String alias = (String)var5.next();
                    cmdMap.remove(alias);
                }
            }

        } catch (ReflectiveOperationException var7) {
            throw new FoException(var7, "Failed to unregister command /" + label);
        }
    }

    public static SimpleCommandMap getCommandMap() {
        try {
            return (SimpleCommandMap)ReflectionUtil.getOBCClass("CraftServer").getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException var1) {
            throw new FoException(var1, "Unable to get the command map");
        }
    }

    public static void registerEnchantment(Enchantment enchantment) {
        unregisterEnchantment(enchantment);
        ReflectionUtil.setStaticField(Enchantment.class, "acceptingNew", true);
        Enchantment.registerEnchantment(enchantment);
    }

    public static void unregisterEnchantment(Enchantment enchantment) {
        Map byName;
        if (MinecraftVersion.atLeast(V.v1_13)) {
            byName = (Map)ReflectionUtil.getStaticFieldContent(Enchantment.class, "byKey");
            byName.remove(enchantment.getKey());
        }

        byName = (Map)ReflectionUtil.getStaticFieldContent(Enchantment.class, "byName");
        byName.remove(enchantment.getName());
    }

    public static Location getLocation(Inventory inv) {
        if (hasInventoryLocation) {
            try {
                return inv.getLocation();
            } catch (NullPointerException var2) {
                return null;
            }
        } else {
            return inv.getHolder() instanceof BlockState ? ((BlockState)inv.getHolder()).getLocation() : (!inv.getViewers().isEmpty() ? ((HumanEntity)inv.getViewers().iterator().next()).getLocation() : null);
        }
    }

    public static String getLocale(Player player) {
        try {
            return player.getLocale();
        } catch (Throwable var5) {
            try {
                Spigot spigot = player.spigot();
                Method method = ReflectionUtil.getMethod(spigot.getClass(), "getLocale");
                return (String)ReflectionUtil.invoke(method, spigot, new Object[0]);
            } catch (Throwable var4) {
                return null;
            }
        }
    }

    public static String getNMSStatisticName(Statistic stat, @Nullable Material mat, @Nullable EntityType en) {
        Class<?> craftStatistic = ReflectionUtil.getOBCClass("CraftStatistic");
        Object nmsStatistic = null;

        try {
            if (stat.getType() == Type.UNTYPED) {
                nmsStatistic = craftStatistic.getMethod("getNMSStatistic", stat.getClass()).invoke((Object)null, stat);
            } else if (stat.getType() == Type.ENTITY) {
                nmsStatistic = craftStatistic.getMethod("getEntityStatistic", stat.getClass(), en.getClass()).invoke((Object)null, stat, en);
            } else {
                nmsStatistic = craftStatistic.getMethod("getMaterialStatistic", stat.getClass(), mat.getClass()).invoke((Object)null, stat, mat);
            }

            Valid.checkNotNull(nmsStatistic, "Could not get NMS statistic from Bukkit's " + stat);
            if (MinecraftVersion.equals(V.v1_8)) {
                Field f = nmsStatistic.getClass().getField("name");
                f.setAccessible(true);
                return f.get(nmsStatistic).toString();
            } else {
                return (String)nmsStatistic.getClass().getMethod("getName").invoke(nmsStatistic);
            }
        } catch (Throwable var6) {
            throw new FoException(var6, "Error getting NMS statistic name from " + stat);
        }
    }

    public static void respawn(Player player) {
        respawn(player, 2);
    }

    public static void respawn(Player player, int delayTicks) {
        Common.runLater(delayTicks, () -> {
            try {
                player.spigot().respawn();
            } catch (NoSuchMethodError var11) {
                try {
                    Object respawnEnum = ReflectionUtil.getNMSClass("EnumClientCommand").getEnumConstants()[0];
                    Constructor<?>[] constructors = ReflectionUtil.getNMSClass("PacketPlayInClientCommand").getConstructors();
                    Constructor[] var4 = constructors;
                    int var5 = constructors.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        Constructor<?> constructor = var4[var6];
                        Class<?>[] args = constructor.getParameterTypes();
                        if (args.length == 1 && args[0] == respawnEnum.getClass()) {
                            Object packet = ReflectionUtil.getNMSClass("PacketPlayInClientCommand").getConstructor(args).newInstance(respawnEnum);
                            sendPacket(player, packet);
                            break;
                        }
                    }
                } catch (Throwable var10) {
                    throw new FoException(var10, "Failed to send respawn packet to " + player.getName());
                }
            }

        });
    }

    public static void openBook(Player player, ItemStack book) {
        Valid.checkBoolean(MinecraftVersion.atLeast(V.v1_8), "Opening books is only supported on MC 1.8 and greater", new Object[0]);

        try {
            player.openBook(book);
        } catch (NoSuchMethodError var6) {
            ItemStack oldItem = player.getItemInHand();
            player.setItemInHand(book);
            Object craftPlayer = getHandleEntity(player);
            Object nmsItemstack = asNMSCopy(book);
            Common.runLater(() -> {
                Method openInventory = ReflectionUtil.getMethod(craftPlayer.getClass(), "openBook", new Class[]{nmsItemstack.getClass()});
                ReflectionUtil.invoke(openInventory, craftPlayer, new Object[]{nmsItemstack});
                player.setItemInHand(oldItem);
            });
        }

    }

    /** @deprecated */
    @Deprecated
    public static void updateInventoryTitle(Player player, String title) {
        try {
            if (MinecraftVersion.olderThan(V.v1_9) && title.length() > 32) {
                title = title.substring(0, 32);
            }

            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object activeContainer = entityPlayer.getClass().getField("activeContainer").get(entityPlayer);
            Object windowId = activeContainer.getClass().getField("windowId").get(activeContainer);
            Object packet;
            Constructor openWindow;
            if (MinecraftVersion.atLeast(V.v1_8)) {
                openWindow = ReflectionUtil.getNMSClass("ChatMessage").getConstructor(String.class, Object[].class);
                Object chatMessage = openWindow.newInstance(org.bukkit.ChatColor.translateAlternateColorCodes('&', title), new Object[0]);
                if (MinecraftVersion.newerThan(V.v1_13)) {
                    int inventorySize = player.getOpenInventory().getTopInventory().getSize() / 9;
                    if (inventorySize < 1 || inventorySize > 6) {
                        Common.log(new String[]{"Cannot update title for " + player.getName() + " as their inventory has non typical size: " + inventorySize + " rows"});
                        return;
                    }

                    Class<?> containersClass = ReflectionUtil.getNMSClass("Containers");
                    Constructor<?> packetConst = ReflectionUtil.getNMSClass("PacketPlayOutOpenWindow").getConstructor(Integer.TYPE, containersClass, ReflectionUtil.getNMSClass("IChatBaseComponent"));
                    Object container = containersClass.getField("GENERIC_9X" + inventorySize).get((Object)null);
                    packet = packetConst.newInstance(windowId, container, chatMessage);
                } else {
                    Constructor<?> packetConst = ReflectionUtil.getNMSClass("PacketPlayOutOpenWindow").getConstructor(Integer.TYPE, String.class, ReflectionUtil.getNMSClass("IChatBaseComponent"), Integer.TYPE);
                    packet = packetConst.newInstance(windowId, "minecraft:chest", chatMessage, player.getOpenInventory().getTopInventory().getSize());
                }
            } else {
                openWindow = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass(MinecraftVersion.atLeast(V.v1_7) ? "PacketPlayOutOpenWindow" : "Packet100OpenWindow"), new Class[]{Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE, Boolean.TYPE});
                packet = ReflectionUtil.instantiate(openWindow, new Object[]{windowId, 0, org.bukkit.ChatColor.translateAlternateColorCodes('&', title), player.getOpenInventory().getTopInventory().getSize(), true});
            }

            sendPacket(player, packet);
            entityPlayer.getClass().getMethod("updateInventory", ReflectionUtil.getNMSClass("Container")).invoke(entityPlayer, activeContainer);
        } catch (ReflectiveOperationException var12) {
            Common.error(var12, new String[]{"Error updating " + player.getName() + " inventory title to '" + title + "'"});
        }

    }

    public static void sendBlockChange(int delayTicks, Player player, Location location, CompMaterial material) {
        if (delayTicks > 0) {
            Common.runLater(delayTicks, () -> {
                sendBlockChange0(player, location, material);
            });
        } else {
            sendBlockChange0(player, location, material);
        }

    }

    private static void sendBlockChange0(Player player, Location location, CompMaterial material) {
        try {
            player.sendBlockChange(location, material.getMaterial().createBlockData());
        } catch (NoSuchMethodError var4) {
            player.sendBlockChange(location, material.getMaterial(), (byte)material.getData());
        }

    }

    public static void sendBlockChange(int delayTicks, Player player, Block block) {
        if (delayTicks > 0) {
            Common.runLater(delayTicks, () -> {
                sendBlockChange0(player, block);
            });
        } else {
            sendBlockChange0(player, block);
        }

    }

    private static void sendBlockChange0(Player player, Block block) {
        try {
            player.sendBlockChange(block.getLocation(), block.getBlockData());
        } catch (NoSuchMethodError var3) {
            player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
        }

    }

    public static int getPlaytimeMinutes(Player player) {
        Statistic stat = getPlayTimeStatisticName();
        return player.getStatistic(stat) / (stat.name().contains("TICK") ? 1200 : 3600);
    }

    public static Statistic getPlayTimeStatisticName() {
        return Statistic.valueOf(MinecraftVersion.olderThan(V.v1_13) ? "PLAY_ONE_TICK" : "PLAY_ONE_MINUTE");
    }

    public static boolean isPlaytimeStatisticTicks() {
        return MinecraftVersion.olderThan(V.v1_13);
    }

    public static boolean isInteractEventPrimaryHand(PlayerInteractEvent e) {
        try {
            return e.getHand() != null && e.getHand() == EquipmentSlot.HAND;
        } catch (NoSuchMethodError var2) {
            return true;
        }
    }

    public static boolean isInteractEventPrimaryHand(PlayerInteractEntityEvent e) {
        try {
            return e.getHand() != null && e.getHand() == EquipmentSlot.HAND;
        } catch (NoSuchMethodError var2) {
            return true;
        }
    }

    public static Score getScore(Objective obj, String entry) {
        Valid.checkNotNull(obj, "Objective cannot be null");
        entry = Common.colorize(entry);

        try {
            return obj.getScore(entry);
        } catch (NoSuchMethodError var3) {
            return obj.getScore(Bukkit.getOfflinePlayer(entry));
        }
    }

    public static OfflinePlayer getOfflinePlayerByUUID(UUID id) {
        try {
            return Bukkit.getOfflinePlayer(id);
        } catch (NoSuchMethodError var5) {
            if (Bukkit.isPrimaryThread()) {
                Common.log(new String[]{"getOfflinePlayerByUUID required two blocking calls on main thread - please notify " + SimplePlugin.getNamed() + " plugin authors."});
            }

            UUIDToNameConverter f = new UUIDToNameConverter(id);

            try {
                String name = f.call();
                return Bukkit.getOfflinePlayer(name);
            } catch (Throwable var4) {
                return null;
            }
        }
    }

    public static Player getPlayerByUUID(UUID id) {
        try {
            return Bukkit.getPlayer(id);
        } catch (NoSuchMethodError var4) {
            Iterator var2 = getOnlinePlayers().iterator();

            Player online;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                online = (Player)var2.next();
            } while(!online.getUniqueId().equals(id));

            return online;
        }
    }

    public static double getFinalDamage(EntityDamageEvent e) {
        try {
            return e.getFinalDamage();
        } catch (NoSuchMethodError var2) {
            return e.getDamage();
        }
    }

    public static Inventory getClickedInventory(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        InventoryView view = e.getView();
        return slot < 0 ? null : (view.getTopInventory() != null && slot < view.getTopInventory().getSize() ? view.getTopInventory() : view.getBottomInventory());
    }

    public static List<BaseComponent[]> getPages(BookMeta meta) {
        try {
            return meta.spigot().getPages();
        } catch (NoSuchMethodError var5) {
            List<BaseComponent[]> list = new ArrayList();
            Iterator var3 = meta.getPages().iterator();

            while(var3.hasNext()) {
                String page = (String)var3.next();
                list.add(TextComponent.fromLegacyText(page));
            }

            return list;
        }
    }

    public static void setPages(BookMeta meta, List<BaseComponent[]> pages) {
        try {
            meta.spigot().setPages(pages);
        } catch (NoSuchMethodError var7) {
            try {
                List<Object> chatComponentPages = (List)ReflectionUtil.getFieldContent(ReflectionUtil.getOBCClass("inventory.CraftMetaBook"), "pages", meta);
                Iterator var4 = pages.iterator();

                while(var4.hasNext()) {
                    BaseComponent[] text = (BaseComponent[])var4.next();
                    chatComponentPages.add(toIChatBaseComponent(text));
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

    }

    public static Object toIChatBaseComponent(BaseComponent[] baseComponents) {
        return toIChatBaseComponent(toJson(baseComponents));
    }

    public static Object toIChatBaseComponent(String json) {
        Valid.checkBoolean(MinecraftVersion.atLeast(V.v1_7), "Serializing chat components requires Minecraft 1.7.10 and greater", new Object[0]);
        Class<?> chatSerializer = ReflectionUtil.getNMSClass((MinecraftVersion.equals(V.v1_7) ? "" : "IChatBaseComponent$") + "ChatSerializer");
        Method a = ReflectionUtil.getMethod(chatSerializer, "a", new Class[]{String.class});
        return ReflectionUtil.invoke(a, (Object)null, new Object[]{json});
    }

    public static String getName(Entity entity) {
        try {
            return entity.getName();
        } catch (NoSuchMethodError var2) {
            return entity instanceof Player ? ((Player)entity).getName() : ItemUtil.bountifyCapitalized(entity.getType());
        }
    }

    public static void setCustomName(Entity en, String name) {
        try {
            en.setCustomNameVisible(true);
            en.setCustomName(Common.colorize(name));
        } catch (NoSuchMethodError var3) {
        }

    }

    public static CompMaterial getMaterial(String material, CompMaterial fallback) {
        Material mat = null;

        try {
            mat = Material.getMaterial(material);
        } catch (Throwable var4) {
        }

        return mat != null ? CompMaterial.fromMaterial(mat) : fallback;
    }

    public static Material getMaterial(String newMaterial, String oldMaterial) {
        try {
            return Material.getMaterial(newMaterial);
        } catch (Throwable var3) {
            return Material.getMaterial(oldMaterial);
        }
    }

    public static Block getTargetBlock(LivingEntity en, int radius) {
        try {
            return en.getTargetBlock((Set)null, radius);
        } catch (Throwable var5) {
            if (var5 instanceof IllegalStateException) {
                return null;
            } else {
                try {
                    return (Block)en.getClass().getMethod("getTargetBlock", HashSet.class, Integer.TYPE).invoke(en, (HashSet)null, radius);
                } catch (ReflectiveOperationException var4) {
                    throw new FoException(var5, "Unable to get target block for " + en);
                }
            }
        }
    }

    public static void sendToast(Player receiver, String message) {
        sendToast(receiver, message, CompMaterial.BOOK);
    }

    public static void sendToast(Player receiver, String message, CompMaterial icon) {
        if (message != null && !message.isEmpty()) {
            String colorized = Common.colorize(message);
            if (!colorized.isEmpty()) {
                Valid.checkSync("Toasts may only be sent from the main thread");
                if (hasAdvancements) {
                    (new AdvancementAccessor(colorized, icon.toString().toLowerCase())).show(receiver);
                } else {
                    receiver.sendMessage(colorized);
                }
            }
        }

    }

    public static void setCooldown(Player player, Material material, int cooldownTicks) {
        try {
            player.setCooldown(material, cooldownTicks);
        } catch (Throwable var5) {
            StrictMap<Material, Integer> cooldown = getCooldown(player);
            cooldown.override(material, cooldownTicks);
            cooldowns.override(player.getUniqueId(), cooldown);
        }

    }

    public static boolean hasCooldown(Player player, Material material) {
        try {
            return player.hasCooldown(material);
        } catch (Throwable var4) {
            StrictMap<Material, Integer> cooldown = getCooldown(player);
            return cooldown.contains(material);
        }
    }

    public static int getCooldown(Player player, Material material) {
        try {
            return player.getCooldown(material);
        } catch (Throwable var4) {
            StrictMap<Material, Integer> cooldown = getCooldown(player);
            return (Integer)cooldown.getOrDefault(material, 0);
        }
    }

    private static StrictMap<Material, Integer> getCooldown(Player player) {
        return (StrictMap)cooldowns.getOrDefault(player.getUniqueId(), new StrictMap());
    }

    public static Entity getEntity(UUID uuid) {
        Valid.checkSync("Remain#getEntity must be called on the main thread");
        Iterator var1 = Bukkit.getWorlds().iterator();

        while(var1.hasNext()) {
            World world = (World)var1.next();
            Iterator var3 = world.getEntities().iterator();

            while(var3.hasNext()) {
                Entity entity = (Entity)var3.next();
                if (entity.getUniqueId().equals(uuid)) {
                    return entity;
                }
            }
        }

        return null;
    }

    public static Collection<Entity> getNearbyEntities(Location location, double radius) {
        try {
            return location.getWorld().getNearbyEntities(location, radius, radius, radius);
        } catch (Throwable var7) {
            List<Entity> found = new ArrayList();
            Iterator var5 = location.getWorld().getEntities().iterator();

            while(var5.hasNext()) {
                Entity e = (Entity)var5.next();
                if (e.getLocation().distance(location) <= radius) {
                    found.add(e);
                }
            }

            return found;
        }
    }

    public static void takeHandItem(Player player) {
        takeItemAndSetAsHand(player, player.getItemInHand());
    }

    public static void takeItemAndSetAsHand(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInHand(item);
        } else {
            player.getInventory().setItemInHand((ItemStack)null);
        }

        player.updateInventory();
    }

    public static void takeItemOnePiece(Player player, ItemStack item) {
        if (MinecraftVersion.atLeast(V.v1_15)) {
            item.setAmount(item.getAmount() - 1);
        } else {
            Common.runLater(() -> {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else if (MinecraftVersion.atLeast(V.v1_9)) {
                    item.setAmount(0);
                } else {
                    ItemStack[] content = player.getInventory().getContents();

                    for(int i = 0; i < content.length; ++i) {
                        ItemStack c = content[i];
                        if (c != null && c.equals(item)) {
                            content[i] = null;
                            break;
                        }
                    }

                    player.getInventory().setContents(content);
                }

                player.updateInventory();
            });
        }

    }

    public static void setPotion(ItemStack item, PotionEffectType type, int level) {
        if (hasItemMeta) {
            PotionSetter.setPotion(item, type, level);
        }

    }

    public static String getI18NDisplayName(ItemStack item) {
        try {
            return (String)item.getClass().getDeclaredMethod("getI18NDisplayName").invoke(item);
        } catch (Throwable var2) {
            return ItemUtil.bountifyCapitalized(item.getType());
        }
    }

    public static SimpleYaml loadConfiguration(InputStream is) {
        Valid.checkNotNull(is, "Could not load configuration from a null input stream!");
        SimpleYaml conf = null;

        try {
            conf = loadConfigurationStrict(is);
        } catch (Throwable var3) {
            var3.printStackTrace();
        }

        Valid.checkNotNull(conf, "Could not load configuration from " + is);
        return conf;
    }

    public static SimpleYaml loadConfigurationStrict(InputStream is) throws Throwable {
        SimpleYaml conf = new SimpleYaml();

        try {
            conf.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (NoSuchMethodError var3) {
            loadConfigurationFromString(is, conf);
        }

        return conf;
    }

    public static SimpleYaml loadConfigurationFromString(InputStream stream, SimpleYaml conf) throws IOException, InvalidConfigurationException {
        Valid.checkNotNull(stream, "Stream cannot be null");
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader input = new BufferedReader(reader);
        Throwable var5 = null;

        try {
            String line;
            try {
                while((line = input.readLine()) != null) {
                    builder.append(line);
                    builder.append('\n');
                }
            } catch (Throwable var14) {
                var5 = var14;
                throw var14;
            }
        } finally {
            if (input != null) {
                if (var5 != null) {
                    try {
                        input.close();
                    } catch (Throwable var13) {
                        var5.addSuppressed(var13);
                    }
                } else {
                    input.close();
                }
            }

        }

        conf.loadFromString(builder.toString());
        return conf;
    }

    public static double getMaxHealth() {
        try {
            String health = String.valueOf(Class.forName("org.spigotmc.SpigotConfig").getField("maxHealth").get((Object)null));
            return health.contains(".") ? Double.parseDouble(health) : (double)Integer.parseInt(health);
        } catch (Throwable var1) {
            return 2048.0D;
        }
    }

    public static boolean isStatSavingDisabled() {
        try {
            return (Boolean)Class.forName("org.spigotmc.SpigotConfig").getField("disableStatSaving").get((Object)null);
        } catch (ReflectiveOperationException var3) {
            try {
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File("spigot.yml"));
                return cfg.isSet("stats.disable-saving") ? cfg.getBoolean("stats.disable-saving") : false;
            } catch (Throwable var2) {
                return false;
            }
        }
    }

    public static void sneaky(Throwable throwable) {
        try {
            SneakyThrow.sneaky(throwable);
        } catch (NoSuchFieldError | NoSuchMethodError | NoClassDefFoundError var2) {
            throw new FoException(throwable);
        }
    }

    public static void setGameRule(World world, String gameRule, boolean value) {
        try {
            if (MinecraftVersion.newerThan(V.v1_13)) {
                GameRule rule = GameRule.getByName(gameRule);
                world.setGameRule(rule, value);
            } else {
                world.setGameRuleValue(gameRule, "" + value);
            }
        } catch (Throwable var4) {
            Common.error(var4, new String[]{"Game rule " + gameRule + " not found."});
        }

    }

    public static void injectServerName() {
        Properties properties = new Properties();
        File props = new File(SimplePlugin.getData().getParentFile().getParentFile(), "server.properties");
        File settingsFile = FileUtil.getFile("settings.yml");
        String previousName = null;
        if (settingsFile.exists()) {
            SimpleYaml settings = FileUtil.loadConfigurationStrict(settingsFile);
            String previousNameRaw = settings.getString("Bungee_Server_Name");
            if (previousNameRaw != null && !previousNameRaw.isEmpty() && !"none".equals(previousNameRaw) && !"undefined".equals(previousNameRaw)) {
                Common.log(new String[]{"&eWarning: Detected Bungee_Server_Name being used in your settings.yml that is now located in server.properties. It has been moved there and you can now delete this key from settings.yml if it was not deleted already."});
                previousName = previousNameRaw;
            }
        }

        try {
            FileReader fileReader = new FileReader(props);
            Throwable var38 = null;

            try {
                properties.load(fileReader);
                if (!properties.containsKey("server-name") || previousName != null) {
                    properties.setProperty("server-name", previousName != null ? previousName : "Undefined - see mineacademy.org/server-properties to configure");
                    FileWriter fileWriter = new FileWriter(props);
                    Throwable var7 = null;

                    try {
                        properties.store(fileWriter, "Minecraft server properties\nModified by " + SimplePlugin.getNamed() + ", see mineacademy.org/server-properties for more information");
                    } catch (Throwable var32) {
                        var7 = var32;
                        throw var32;
                    } finally {
                        if (fileWriter != null) {
                            if (var7 != null) {
                                try {
                                    fileWriter.close();
                                } catch (Throwable var31) {
                                    var7.addSuppressed(var31);
                                }
                            } else {
                                fileWriter.close();
                            }
                        }

                    }
                }

                serverName = properties.getProperty("server-name");
            } catch (Throwable var34) {
                var38 = var34;
                throw var34;
            } finally {
                if (fileReader != null) {
                    if (var38 != null) {
                        try {
                            fileReader.close();
                        } catch (Throwable var30) {
                            var38.addSuppressed(var30);
                        }
                    } else {
                        fileReader.close();
                    }
                }

            }
        } catch (Throwable var36) {
            var36.printStackTrace();
        }

    }

    public static String getServerName() {
        Valid.checkBoolean(isServerNameChanged(), "Detected getServerName call, please configure your 'server-name' in server.properties according to mineacademy.org/server-properties", new Object[0]);
        return serverName;
    }

    public static boolean isServerNameChanged() {
        return !"see mineacademy.org/server-properties to configure".contains(serverName) && !"undefined".equals(serverName) && !"Unknown Server".equals(serverName);
    }

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }

        return Integer.parseInt(version);
    }

    public static boolean isPaper() {
        try {
            Class.forName("co.aikar.timings.Timing");
            return true;
        } catch (Throwable var1) {
            return false;
        }
    }

    public static boolean isBungeeApiPresent() {
        return bungeeApiPresent;
    }

    public static boolean hasNewScoreboardAPI() {
        return newScoreboardAPI;
    }

    public static boolean hasParticleAPI() {
        return hasParticleAPI;
    }

    public static boolean hasBookEvent() {
        return hasBookEvent;
    }

    public static boolean hasScoreboardTags() {
        return hasScoreboardTags;
    }

    public static boolean hasSpawnEggMeta() {
        return hasSpawnEggMeta;
    }

    public static boolean hasYamlReaderLoad() {
        return hasYamlReaderLoad;
    }

    public static boolean hasItemMeta() {
        return hasItemMeta;
    }

    public static boolean hasHexColors() {
        return MinecraftVersion.atLeast(V.v1_16);
    }

    private static Player[] getPlayersLegacy() {
        try {
            return (Player[])((Player[])getPlayersMethod.invoke((Object)null));
        } catch (ReflectiveOperationException var1) {
            throw new FoException(var1, "Reflection malfunction");
        }
    }

    private static int getHealhLegacy(LivingEntity entity) {
        try {
            return (Integer)getHealthMethod.invoke(entity);
        } catch (ReflectiveOperationException var2) {
            throw new FoException(var2, "Reflection malfunction");
        }
    }

    private static int getMaxHealhLegacy(LivingEntity entity) {
        try {
            Object number = LivingEntity.class.getMethod("getMaxHealth").invoke(entity);
            if (number instanceof Double) {
                return ((Double)number).intValue();
            } else {
                return number instanceof Integer ? (Integer)number : (int)Double.parseDouble(number.toString());
            }
        } catch (ReflectiveOperationException var2) {
            throw new FoException(var2, "Reflection malfunction");
        }
    }

    static {
        Valid.checkBoolean(MinecraftVersion.getCurrent().isTested(), "Your Minecraft version " + MinecraftVersion.getCurrent() + " is unsupported by " + SimplePlugin.getNamed(), new Object[0]);

        try {
            ChatInternals.callStatic();
            if (MinecraftVersion.newerThan(V.v1_7)) {
                NBTInternals.checkCompatible();
            }

            ParticleInternals.ANGRY_VILLAGER.getClass();
            Material[] var0 = Material.values();
            int var1 = var0.length;

            int var2;
            for(var2 = 0; var2 < var1; ++var2) {
                Material bukkitMaterial = var0[var2];
                CompMaterial.fromString(bukkitMaterial.toString());
            }

            CompMaterial[] var18 = CompMaterial.values();
            var1 = var18.length;

            for(var2 = 0; var2 < var1; ++var2) {
                CompMaterial compMaterial = var18[var2];
                compMaterial.getMaterial();
            }

            ReflectionUtil.getNMSClass("Entity");
        } catch (Throwable var17) {
            Bukkit.getLogger().severe("** COMPATIBILITY TEST FAILED - THIS PLUGIN WILL NOT FUNCTION PROPERLY **");
            Bukkit.getLogger().severe("** YOUR MINECRAFT VERSION APPEARS UNSUPPORTED: " + MinecraftVersion.getCurrent() + " **");
            var17.printStackTrace();
            Bukkit.getLogger().severe("***************************************************************");
        }

        try {
            boolean hasNMS = MinecraftVersion.atLeast(V.v1_4);

            try {
                getHandle = ReflectionUtil.getOBCClass("entity.CraftPlayer").getMethod("getHandle");
                fieldPlayerConnection = ReflectionUtil.getNMSClass("EntityPlayer").getField(hasNMS ? "playerConnection" : "netServerHandler");
                sendPacket = ReflectionUtil.getNMSClass(hasNMS ? "PlayerConnection" : "NetServerHandler").getMethod("sendPacket", ReflectionUtil.getNMSClass("Packet"));
            } catch (Throwable var15) {
                Bukkit.getLogger().warning("Unable to find setup some parts of reflection. Plugin will still function.");
                Bukkit.getLogger().warning("Error: " + var15.getClass().getSimpleName() + ": " + var15.getMessage());
                Bukkit.getLogger().warning("Ignore this if using Cauldron. Otherwise check if your server is compatibible.");
                fieldPlayerConnection = null;
                sendPacket = null;
                getHandle = null;
            }

            getPlayersMethod = Bukkit.class.getMethod("getOnlinePlayers");
            isGetPlayersCollection = getPlayersMethod.getReturnType() == Collection.class;
            getHealthMethod = LivingEntity.class.getMethod("getHealth");
            isGetHealthDouble = getHealthMethod.getReturnType() == Double.TYPE;
            hasExtendedPlayerTitleAPI = MinecraftVersion.atLeast(V.v1_11);

            try {
                World.class.getMethod("spawnParticle", Particle.class, Location.class, Integer.TYPE);
            } catch (ReflectiveOperationException | NoClassDefFoundError var14) {
                hasParticleAPI = false;
            }

            try {
                Class.forName("net.md_5.bungee.chat.ComponentSerializer");
            } catch (ClassNotFoundException var13) {
                bungeeApiPresent = false;
                throw new FoException("&cYour server version (&f" + Bukkit.getBukkitVersion().replace("-SNAPSHOT", "") + "&c) doesn't\n &cinclude &elibraries required&c for this plugin to\n &crun. Install the following plugin for compatibility:\n &fhttps://mineacademy.org/plugins/#misc");
            }

            try {
                Objective.class.getMethod("getScore", String.class);
            } catch (NoSuchMethodException | NoClassDefFoundError var12) {
                newScoreboardAPI = false;
            }

            try {
                Class.forName("org.bukkit.event.player.PlayerEditBookEvent").getName();
            } catch (ClassNotFoundException var11) {
                hasBookEvent = false;
            }

            try {
                Inventory.class.getMethod("getLocation");
            } catch (ReflectiveOperationException var10) {
                hasInventoryLocation = false;
            }

            try {
                Entity.class.getMethod("getScoreboardTags");
            } catch (ReflectiveOperationException var9) {
                hasScoreboardTags = false;
            }

            try {
                Class.forName("org.bukkit.inventory.meta.SpawnEggMeta");
            } catch (ClassNotFoundException var8) {
                hasSpawnEggMeta = false;
            }

            try {
                Class.forName("org.bukkit.advancement.Advancement");
                Class.forName("org.bukkit.NamespacedKey");
            } catch (ClassNotFoundException var7) {
                hasAdvancements = false;
            }

            try {
                YamlConfiguration.class.getMethod("load", Reader.class);
            } catch (NoSuchMethodException var6) {
                hasYamlReaderLoad = false;
            }

            try {
                Class.forName("org.bukkit.inventory.meta.ItemMeta");
            } catch (Exception var5) {
                hasItemMeta = false;
            }

            if (MinecraftVersion.atLeast(V.v1_16)) {
                try {
                    Bukkit.getUnsafe().fromLegacy(Material.AIR);
                } catch (Throwable var4) {
                }
            }

        } catch (ReflectiveOperationException var16) {
            throw new UnsupportedOperationException("Failed to set up reflection, " + SimplePlugin.getNamed() + " won't work properly", var16);
        }
    }

    public static class InteractiveTextFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private InteractiveTextFoundException() {
        }
    }
}