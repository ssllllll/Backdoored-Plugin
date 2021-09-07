package me.ego.ezbd.lib.fo;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.jsonsimple.JSONObject;
import me.ego.ezbd.lib.fo.jsonsimple.JSONParser;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.model.HookManager;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompAttribute;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.CompProperty;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.Statistic.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class PlayerUtil {
    public static final int USABLE_PLAYER_INV_SIZE = 36;
    private static final Map<UUID, BukkitTask> titleRestoreTasks = new ConcurrentHashMap();

    public static void kick(Player player, String... message) {
        Common.runLater(() -> {
            player.kickPlayer(Common.colorize(message));
        });
    }

    public static int getPing(Player player) {
        Object entityPlayer = Remain.getHandleEntity(player);
        return (Integer)ReflectionUtil.getFieldContent(entityPlayer, "ping");
    }

    public static TreeMap<Long, OfflinePlayer> getStatistics(Statistic statistic) {
        return getStatistics(statistic, (Material)null, (EntityType)null);
    }

    public static TreeMap<Long, OfflinePlayer> getStatistics(Statistic statistic, Material material) {
        return getStatistics(statistic, material, (EntityType)null);
    }

    public static TreeMap<Long, OfflinePlayer> getStatistics(Statistic statistic, EntityType entityType) {
        return getStatistics(statistic, (Material)null, entityType);
    }

    public static TreeMap<Long, OfflinePlayer> getStatistics(Statistic statistic, Material material, EntityType entityType) {
        TreeMap<Long, OfflinePlayer> statistics = new TreeMap(Collections.reverseOrder());
        OfflinePlayer[] var4 = Bukkit.getOfflinePlayers();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            OfflinePlayer offline = var4[var6];
            long time = getStatistic(offline, statistic, material, entityType);
            statistics.put(time, offline);
        }

        return statistics;
    }

    public static long getStatistic(OfflinePlayer player, Statistic statistic) {
        return getStatistic(player, statistic, (Material)null, (EntityType)null);
    }

    public static long getStatistic(OfflinePlayer player, Statistic statistic, Material material) {
        return getStatistic(player, statistic, material, (EntityType)null);
    }

    public static long getStatistic(OfflinePlayer player, Statistic statistic, EntityType entityType) {
        return getStatistic(player, statistic, (Material)null, entityType);
    }

    private static long getStatistic(OfflinePlayer player, Statistic statistic, Material material, EntityType entityType) {
        if (player.isOnline()) {
            Player online = player.getPlayer();
            if (statistic.getType() == Type.UNTYPED) {
                return (long)online.getStatistic(statistic);
            } else {
                return statistic.getType() == Type.ENTITY ? (long)online.getStatistic(statistic, entityType) : (long)online.getStatistic(statistic, material);
            }
        } else {
            return getStatisticFile(player, statistic, material, entityType);
        }
    }

    private static long getStatisticFile(OfflinePlayer player, Statistic statistic, Material material, EntityType entityType) {
        File worldFolder = new File(((World)Bukkit.getServer().getWorlds().get(0)).getWorldFolder(), "stats");
        File statFile = new File(worldFolder, player.getUniqueId().toString() + ".json");
        if (statFile.exists()) {
            try {
                JSONObject json = (JSONObject)JSONParser.getInstance().parse(new FileReader(statFile));
                String name = Remain.getNMSStatisticName(statistic, material, entityType);
                JSONObject section = json.getObject("stats");
                long result = 0L;
                String[] var11 = name.split("\\:");
                int var12 = var11.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    String part = var11[var13];
                    part = part.replace(".", ":");
                    if (section != null) {
                        JSONObject nextSection = section.getObject(part);
                        if (nextSection == null) {
                            result = Long.parseLong(section.containsKey(part) ? section.get(part).toString() : "0");
                            break;
                        }

                        section = nextSection;
                    }
                }

                return result;
            } catch (Throwable var16) {
                throw new FoException(var16);
            }
        } else {
            return 0L;
        }
    }

    public static boolean hasPerm(Permissible sender, String permission) {
        Valid.checkNotNull(sender, "cannot call hasPerm for null sender!");
        if (permission == null) {
            Common.log(new String[]{"THIS IS NOT AN ACTUAL ERROR, YOUR PLUGIN WILL WORK FINE"});
            Common.log(new String[]{"Internal check got null permission as input, this is no longer allowed."});
            Common.log(new String[]{"We'll return true to prevent errors. Contact developers of " + SimplePlugin.getNamed()});
            Common.log(new String[]{"to get it solved and include the fake error below:"});
            (new Throwable()).printStackTrace();
            return true;
        } else {
            Valid.checkBoolean(!permission.contains("{plugin_name}") && !permission.contains("{plugin_name_lower}"), "Found {plugin_name} variable calling hasPerm(" + sender + ", " + permission + ").This is now disallowed, contact plugin authors to put " + SimplePlugin.getNamed().toLowerCase() + " in their permission.", new Object[0]);
            return sender.hasPermission(permission);
        }
    }

    public static void normalize(Player player, boolean cleanInventory) {
        normalize(player, cleanInventory, true);
    }

    public static void normalize(Player player, boolean cleanInventory, boolean removeVanish) {
        synchronized(titleRestoreTasks) {
            HookManager.setGodMode(player, false);
            player.setGameMode(GameMode.SURVIVAL);
            Iterator var4;
            if (cleanInventory) {
                cleanInventoryAndFood(player);
                player.resetMaxHealth();

                try {
                    player.setHealth(20.0D);
                } catch (Throwable var10) {
                    try {
                        double maxHealthAttr = CompAttribute.GENERIC_MAX_HEALTH.get(player);
                        player.setHealth(maxHealthAttr);
                    } catch (Throwable var9) {
                    }
                }

                player.setHealthScaled(false);
                var4 = player.getActivePotionEffects().iterator();

                while(var4.hasNext()) {
                    PotionEffect potion = (PotionEffect)var4.next();
                    player.removePotionEffect(potion.getType());
                }
            }

            player.setTotalExperience(0);
            player.setLevel(0);
            player.setExp(0.0F);
            player.resetPlayerTime();
            player.resetPlayerWeather();
            player.setFallDistance(0.0F);
            CompProperty.INVULNERABLE.apply(player, false);

            try {
                player.setGlowing(false);
                player.setSilent(false);
            } catch (NoSuchMethodError var8) {
            }

            player.setAllowFlight(false);
            player.setFlying(false);
            player.setFlySpeed(0.2F);
            player.setWalkSpeed(0.2F);
            player.setCanPickupItems(true);
            player.setVelocity(new Vector(0, 0, 0));
            player.eject();
            if (player.isInsideVehicle()) {
                player.getVehicle().remove();
            }

            try {
                var4 = player.getPassengers().iterator();

                while(var4.hasNext()) {
                    Entity passenger = (Entity)var4.next();
                    player.removePassenger(passenger);
                }
            } catch (NoSuchMethodError var13) {
            }

            if (removeVanish) {
                try {
                    if (player.hasMetadata("vanished")) {
                        Plugin plugin = ((MetadataValue)player.getMetadata("vanished").get(0)).getOwningPlugin();
                        player.removeMetadata("vanished", plugin);
                    }

                    var4 = Remain.getOnlinePlayers().iterator();

                    while(var4.hasNext()) {
                        Player other = (Player)var4.next();
                        if (!other.getName().equals(player.getName()) && !other.canSee(player)) {
                            other.showPlayer(player);
                        }
                    }
                } catch (NoSuchMethodError var11) {
                } catch (Exception var12) {
                    var12.printStackTrace();
                }
            }

        }
    }

    private static void cleanInventoryAndFood(Player player) {
        player.getInventory().setArmorContents((ItemStack[])null);
        player.getInventory().setContents(new ItemStack[player.getInventory().getContents().length]);

        try {
            player.getInventory().setExtraContents(new ItemStack[player.getInventory().getExtraContents().length]);
        } catch (NoSuchMethodError var2) {
        }

        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setExhaustion(0.0F);
        player.setSaturation(10.0F);
        player.setVelocity(new Vector(0, 0, 0));
    }

    public static boolean hasEmptyInventory(Player player) {
        ItemStack[] inv = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack[] everything = (ItemStack[])((ItemStack[])ArrayUtils.addAll(inv, armor));
        ItemStack[] var4 = everything;
        int var5 = everything.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            ItemStack i = var4[var6];
            if (i != null && i.getType() != Material.AIR) {
                return false;
            }
        }

        return true;
    }

    public static boolean isVanished(Player player, @Nullable Player otherPlayer) {
        return otherPlayer != null && !otherPlayer.canSee(player) ? true : isVanished(player);
    }

    public static boolean isVanished(Player player) {
        if (HookManager.isVanished(player)) {
            return true;
        } else {
            if (player.hasMetadata("vanished")) {
                Iterator var1 = player.getMetadata("vanished").iterator();

                while(var1.hasNext()) {
                    MetadataValue meta = (MetadataValue)var1.next();
                    if (meta.asBoolean()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static Player getPlayerByNickNoVanish(String name) {
        return getPlayerByNick(name, false);
    }

    public static Player getPlayerByNick(String name, boolean ignoreVanished) {
        Player found = lookupNickedPlayer0(name);
        return ignoreVanished && found != null && isVanished(found) ? null : found;
    }

    private static Player lookupNickedPlayer0(String name) {
        Player found = null;
        int delta = 2147483647;
        Iterator var3 = Remain.getOnlinePlayers().iterator();

        while(var3.hasNext()) {
            Player player = (Player)var3.next();
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }

            String nick = HookManager.getNickColorless(player);
            if (nick.toLowerCase().startsWith(name.toLowerCase())) {
                int curDelta = Math.abs(nick.length() - name.length());
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }

                if (curDelta == 0) {
                    break;
                }
            }
        }

        return found;
    }

    public static void lookupOfflinePlayerAsync(String name, Consumer<OfflinePlayer> syncCallback) {
        Common.runAsync(() -> {
            String parsedName = HookManager.getNameFromNick(name);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(parsedName);
            Common.runLater(() -> {
                syncCallback.accept(offlinePlayer);
            });
        });
    }

    public static void updateInventoryTitle(Menu menu, Player player, String temporaryTitle, String oldTitle, int duration) {
        Valid.checkNotNull(menu, "Menu == null");
        Valid.checkNotNull(player, "Player == null");
        Valid.checkNotNull(temporaryTitle, "Title == null");
        Valid.checkNotNull(oldTitle, "Old Title == null");
        updateInventoryTitle(player, MinecraftVersion.atLeast(V.v1_13) ? temporaryTitle.replace("%", "%%") : temporaryTitle);
        BukkitTask pending = (BukkitTask)titleRestoreTasks.get(player.getUniqueId());
        if (pending != null) {
            pending.cancel();
        }

        pending = Common.runLater(duration, () -> {
            Menu futureMenu = Menu.getMenu(player);
            if (futureMenu != null && futureMenu.getClass().getName().equals(menu.getClass().getName())) {
                updateInventoryTitle(player, oldTitle);
            }

        });
        UUID uid = player.getUniqueId();
        titleRestoreTasks.put(uid, pending);
        Common.runLater(duration + 1, () -> {
            if (titleRestoreTasks.containsKey(uid)) {
                titleRestoreTasks.remove(uid);
            }

        });
    }

    public static void updateInventoryTitle(Player player, String title) {
        Remain.updateInventoryTitle(player, title);
    }

    public static ItemStack getFirstItem(Player player, ItemStack item) {
        ItemStack[] var2 = player.getInventory().getContents();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            ItemStack otherItem = var2[var4];
            if (otherItem != null && ItemUtil.isSimilar(otherItem, item)) {
                return otherItem;
            }
        }

        return null;
    }

    public static boolean take(Player player, CompMaterial material, int amount) {
        if (!containsAtLeast(player, amount, material)) {
            return false;
        } else {
            for(int i = 0; i < amount; ++i) {
                takeFirstOnePiece(player, material);
            }

            return true;
        }
    }

    public static boolean takeFirstOnePiece(Player player, CompMaterial material) {
        ItemStack[] var2 = player.getInventory().getContents();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            ItemStack item = var2[var4];
            if (item != null && CompMaterial.fromLegacy(item.getType().toString(), item.getData().getData()) == material) {
                takeOnePiece(player, item);
                return true;
            }
        }

        return false;
    }

    public static void takeOnePiece(Player player, ItemStack item) {
        Remain.takeItemOnePiece(player, item);
    }

    public static boolean containsAtLeast(Player player, int atLeastSize, CompMaterial material) {
        int foundSize = 0;
        ItemStack[] var4 = player.getInventory().getContents();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            ItemStack item = var4[var6];
            if (item != null && item.getType() == material.getMaterial()) {
                foundSize += item.getAmount();
            }
        }

        return foundSize >= atLeastSize;
    }

    public static boolean updateInvSlot(Inventory inv, ItemStack search, ItemStack replaceWith) {
        Valid.checkNotNull(inv, "Inv = null");

        for(int i = 0; i < inv.getSize(); ++i) {
            ItemStack slot = inv.getItem(i);
            if (slot != null && ItemUtil.isSimilar(slot, search)) {
                inv.setItem(i, replaceWith);
                return true;
            }
        }

        return false;
    }

    public static Map<Integer, ItemStack> addItems(Inventory inventory, ItemStack... items) {
        return addItems(inventory, 0, items);
    }

    private static Map<Integer, ItemStack> addItems(Inventory inventory, int oversizedStacks, ItemStack... items) {
        int i;
        if (isCombinedInv(inventory)) {
            Inventory fakeInventory = makeTruncatedInv((PlayerInventory)inventory);
            Map<Integer, ItemStack> overflow = addItems(fakeInventory, oversizedStacks, items);

            for(i = 0; i < fakeInventory.getContents().length; ++i) {
                inventory.setItem(i, fakeInventory.getContents()[i]);
            }

            return overflow;
        } else {
            Map<Integer, ItemStack> left = new HashMap();
            ItemStack[] combined = new ItemStack[items.length];
            ItemStack[] var5 = items;
            int var6 = items.length;

            int maxAmount;
            int firstFree;
            for(maxAmount = 0; maxAmount < var6; ++maxAmount) {
                ItemStack item = var5[maxAmount];
                if (item != null && item.getAmount() >= 1) {
                    for(firstFree = 0; firstFree < combined.length; ++firstFree) {
                        if (combined[firstFree] == null) {
                            combined[firstFree] = item.clone();
                            break;
                        }

                        if (combined[firstFree].isSimilar(item)) {
                            combined[firstFree].setAmount(combined[firstFree].getAmount() + item.getAmount());
                            break;
                        }
                    }
                }
            }

            for(i = 0; i < combined.length; ++i) {
                ItemStack item = combined[i];
                if (item != null && item.getType() != Material.AIR) {
                    while(true) {
                        maxAmount = oversizedStacks > item.getType().getMaxStackSize() ? oversizedStacks : item.getType().getMaxStackSize();
                        int firstPartial = firstPartial(inventory, item, maxAmount);
                        if (firstPartial == -1) {
                            firstFree = inventory.firstEmpty();
                            if (firstFree == -1) {
                                left.put(i, item);
                                break;
                            }

                            if (item.getAmount() <= maxAmount) {
                                inventory.setItem(firstFree, item);
                                break;
                            }

                            ItemStack stack = item.clone();
                            stack.setAmount(maxAmount);
                            inventory.setItem(firstFree, stack);
                            item.setAmount(item.getAmount() - maxAmount);
                        } else {
                            ItemStack partialItem = inventory.getItem(firstPartial);
                            int amount = item.getAmount();
                            int partialAmount = partialItem.getAmount();
                            if (amount + partialAmount <= maxAmount) {
                                partialItem.setAmount(amount + partialAmount);
                                break;
                            }

                            partialItem.setAmount(maxAmount);
                            item.setAmount(amount + partialAmount - maxAmount);
                        }
                    }
                }
            }

            return left;
        }
    }

    private static int firstPartial(Inventory inventory, ItemStack item, int maxAmount) {
        if (item == null) {
            return -1;
        } else {
            ItemStack[] stacks = inventory.getContents();

            for(int i = 0; i < stacks.length; ++i) {
                ItemStack cItem = stacks[i];
                if (cItem != null && cItem.getAmount() < maxAmount && cItem.isSimilar(item)) {
                    return i;
                }
            }

            return -1;
        }
    }

    private static Inventory makeTruncatedInv(PlayerInventory playerInventory) {
        Inventory fake = Bukkit.createInventory((InventoryHolder)null, 36);
        fake.setContents((ItemStack[])Arrays.copyOf(playerInventory.getContents(), fake.getSize()));
        return fake;
    }

    private static boolean isCombinedInv(Inventory inventory) {
        return inventory instanceof PlayerInventory && inventory.getContents().length > 36;
    }

    private PlayerUtil() {
    }
}