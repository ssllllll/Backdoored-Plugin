package me.ego.ezbd.lib.fo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public final class RandomUtil {
    private static final Random random = new Random();
    private static final char[] COLORS_AND_DECORATION = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'n', 'o'};
    private static final char[] CHAT_COLORS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] LETTERS = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'y', 'z', 'ó'};

    public static Random getRandom() {
        return random;
    }

    public static boolean chance(long percent) {
        return chance((int)percent);
    }

    public static boolean chance(int percent) {
        return random.nextDouble() * 100.0D < (double)percent;
    }

    public static boolean chanceD(double percent) {
        return random.nextDouble() < percent;
    }

    public static DyeColor nextDyeColor() {
        return DyeColor.values()[random.nextInt(DyeColor.values().length)];
    }

    public static String nextColorOrDecoration() {
        return "&" + COLORS_AND_DECORATION[nextInt(COLORS_AND_DECORATION.length)];
    }

    public static String nextString(int length) {
        String text = "";

        for(int i = 0; i < length; ++i) {
            text = text + LETTERS[nextInt(LETTERS.length)];
        }

        return text;
    }

    public static ChatColor nextChatColor() {
        char letter = CHAT_COLORS[nextInt(CHAT_COLORS.length)];
        return ChatColor.getByChar(letter);
    }

    public static int nextBetween(int min, int max) {
        Valid.checkBoolean(min <= max, "Min !< max", new Object[0]);
        return min + nextInt(max - min + 1);
    }

    public static int nextInt(int boundExclusive) {
        Valid.checkBoolean(boundExclusive > 0, "Getting a random number must have the bound above 0, got: " + boundExclusive, new Object[0]);
        return random.nextInt(boundExclusive);
    }

    public static boolean nextBoolean() {
        return random.nextBoolean();
    }

    public static <T> T nextItem(T... items) {
        return items[nextInt(items.length)];
    }

    public static <T> T nextItem(Iterable<T> items) {
        return nextItem(items, (Predicate)null);
    }

    public static <T> T nextItem(Iterable<T> items, Predicate<T> condition) {
        List<T> list = items instanceof List ? new ArrayList((List)items) : Common.toList(items);
        if (condition != null) {
            Iterator it = ((List)list).iterator();

            while(it.hasNext()) {
                T item = it.next();
                if (!condition.test(item)) {
                    it.remove();
                }
            }
        }

        return ((List)list).get(nextInt(((List)list).size()));
    }

    public static Location nextLocation(Location origin, double radius, boolean is3D) {
        double randomRadius = random.nextDouble() * radius;
        double theta = Math.toRadians(random.nextDouble() * 360.0D);
        double phi = Math.toRadians(random.nextDouble() * 180.0D - 90.0D);
        double x = randomRadius * Math.cos(theta) * Math.sin(phi);
        double z = randomRadius * Math.cos(phi);
        Location newLoc = origin.clone().add(x, is3D ? randomRadius * Math.sin(theta) * Math.cos(phi) : 0.0D, z);
        return newLoc;
    }

    public static int nextChunkX(Chunk chunk) {
        return nextInt(16) + (chunk.getX() << 4) - 16;
    }

    public static int nextChunkZ(Chunk chunk) {
        return nextInt(16) + (chunk.getZ() << 4) - 16;
    }

    private RandomUtil() {
    }
}