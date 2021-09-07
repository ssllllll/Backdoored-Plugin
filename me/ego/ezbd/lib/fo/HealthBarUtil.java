package me.ego.ezbd.lib.fo;

import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class HealthBarUtil {
    private static String prefix = "&8[";
    private static String suffix = "&8]";
    private static ChatColor remainingColor;
    private static ChatColor totalColor;
    private static ChatColor deadColor;

    public static void display(Player displayTo, LivingEntity displayAbout, double damage) {
        int maxHealth = Remain.getMaxHealth(displayAbout);
        int health = Remain.getHealth(displayAbout);
        String name = Common.getOrEmpty(displayAbout.getCustomName());
        String formatted = (name.isEmpty() ? ItemUtil.bountifyCapitalized(displayAbout.getType()) : name) + " - " + getHealthMessage(health, maxHealth, (int)damage);
        Remain.sendActionBar(displayTo, formatted);
    }

    private static String getHealthMessage(int health, int maxHealth, int damage) {
        int remainingHealth = health - damage;
        return remainingHealth > 0 ? formatHealth(remainingHealth, maxHealth) : formatDeath(maxHealth);
    }

    private static String formatHealth(int remainingHealth, int maxHealth) {
        if (maxHealth > 30) {
            return formatMuchHealth(remainingHealth, maxHealth);
        } else {
            String left = "";

            for(int i = 0; i < remainingHealth; ++i) {
                left = left + "|";
            }

            String max = "";

            for(int i = 0; i < maxHealth - left.length(); ++i) {
                max = max + "|";
            }

            return prefix + remainingColor + left + totalColor + max + suffix;
        }
    }

    private static String formatMuchHealth(int remaining, int max) {
        return prefix + remainingColor + remaining + " &8/ " + totalColor + max + suffix;
    }

    private static String formatDeath(int maxHealth) {
        String max = "";
        if (maxHealth > 30) {
            max = "-0-";
        } else {
            for(int i = 0; i < maxHealth; ++i) {
                max = max + "|";
            }
        }

        return prefix + deadColor + max + suffix;
    }

    private HealthBarUtil() {
    }

    public static void setPrefix(String prefix) {
        HealthBarUtil.prefix = prefix;
    }

    public static void setSuffix(String suffix) {
        HealthBarUtil.suffix = suffix;
    }

    public static void setRemainingColor(ChatColor remainingColor) {
        HealthBarUtil.remainingColor = remainingColor;
    }

    public static void setTotalColor(ChatColor totalColor) {
        HealthBarUtil.totalColor = totalColor;
    }

    public static void setDeadColor(ChatColor deadColor) {
        HealthBarUtil.deadColor = deadColor;
    }

    static {
        remainingColor = ChatColor.DARK_RED;
        totalColor = ChatColor.GRAY;
        deadColor = ChatColor.BLACK;
    }
}