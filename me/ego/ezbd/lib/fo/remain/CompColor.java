package me.ego.ezbd.lib.fo.remain;

import java.util.ArrayList;
import java.util.List;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public final class CompColor {
    private static final List<CompColor> values = new ArrayList();
    public static final CompColor BLUE;
    public static final CompColor BLACK;
    public static final CompColor DARK_AQUA;
    public static final CompColor DARK_BLUE;
    public static final CompColor AQUA;
    public static final CompColor GRAY;
    public static final CompColor DARK_GRAY;
    public static final CompColor DARK_GREEN;
    public static final CompColor GREEN;
    public static final CompColor GOLD;
    public static final CompColor BROWN;
    public static final CompColor DARK_RED;
    public static final CompColor RED;
    public static final CompColor WHITE;
    public static final CompColor YELLOW;
    public static final CompColor DARK_PURPLE;
    public static final CompColor LIGHT_PURPLE;
    public static final CompColor PINK;
    private final String name;
    private final DyeColor dye;
    private final ChatColor chatColor;
    private final String legacyName;
    private Color color;

    private CompColor(Color color) {
        this((String)null, (DyeColor)null, (ChatColor)null);
        this.color = color;
    }

    private CompColor(String name, DyeColor dye) {
        this(name, dye, (ChatColor)null);
    }

    private CompColor(String name, DyeColor dye, ChatColor chatColor) {
        this(name, dye, chatColor, (String)null);
    }

    private CompColor(String name, DyeColor dye, ChatColor chatColor, String legacyName) {
        this.name = name;
        this.dye = dye;
        this.chatColor = chatColor == null ? (name != null ? ChatColor.valueOf(name) : ChatColor.WHITE) : chatColor;
        this.legacyName = Common.getOrEmpty(legacyName);
        values.add(this);
    }

    public Color getColor() {
        return this.color != null ? this.color : this.dye.getColor();
    }

    private static <T extends Enum<T>> T getEnum(String newName, String oldName, Class<T> clazz) {
        T en = ReflectionUtil.lookupEnumSilent(clazz, newName);
        if (en == null) {
            en = ReflectionUtil.lookupEnumSilent(clazz, oldName);
        }

        return en;
    }

    public static CompColor fromWoolData(byte data) {
        return fromDye(DyeColor.getByWoolData(data));
    }

    public static CompColor fromColor(Color color) {
        return fromName("#" + Integer.toHexString(color.asRGB()).substring(2));
    }

    public static CompColor fromName(String name) {
        if (name.startsWith("#") && name.length() == 7) {
            return new CompColor(Color.fromRGB(Integer.valueOf(name.substring(1, 3), 16), Integer.valueOf(name.substring(3, 5), 16), Integer.valueOf(name.substring(5, 7), 16)));
        } else {
            name = name.toUpperCase();
            CompColor[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                CompColor comp = var1[var3];
                if (comp.chatColor.toString().equals(name) || comp.dye.toString().equals(name) || comp.legacyName.equals(name)) {
                    return comp;
                }
            }

            throw new IllegalArgumentException("Could not get CompColor from name: " + name);
        }
    }

    public static CompColor fromDye(DyeColor dye) {
        CompColor[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            CompColor comp = var1[var3];
            if (comp.dye == dye || comp.legacyName.equals(dye.toString())) {
                return comp;
            }
        }

        throw new IllegalArgumentException("Could not get CompColor from DyeColor." + dye.toString());
    }

    public static CompColor fromChatColor(ChatColor color) {
        CompColor[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            CompColor comp = var1[var3];
            if (comp.chatColor == color || comp.legacyName.equals(color.toString())) {
                return comp;
            }
        }

        throw new IllegalArgumentException("Could not get CompColor from ChatColor." + color.name());
    }

    public static CompColor fromChatColor(CompChatColor color) {
        CompColor[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            CompColor comp = var1[var3];
            if (comp.chatColor.name().equalsIgnoreCase(color.getName()) || comp.legacyName.equalsIgnoreCase(color.toString())) {
                return comp;
            }
        }

        throw new FoException("Could not get CompColor from ChatColor." + color.getName());
    }

    public static DyeColor toDye(ChatColor color) {
        CompColor c = fromName(color.name());
        return c != null ? c.getDye() : DyeColor.WHITE;
    }

    public static ChatColor toColor(DyeColor dye) {
        CompColor[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            CompColor c = var1[var3];
            if (c.getDye() == dye) {
                return c.getChatColor();
            }
        }

        return ChatColor.WHITE;
    }

    public static CompMaterial toConcrete(ChatColor color) {
        CompMaterial wool = toWool(color);
        return CompMaterial.fromString(wool.toString().replace("_WOOL", MinecraftVersion.olderThan(V.v1_12) ? "_STAINED_GLASS" : "_CONCRETE"));
    }

    public static CompMaterial toWool(ChatColor color) {
        CompColor comp = fromChatColor(color);
        if (comp == AQUA) {
            return CompMaterial.LIGHT_BLUE_WOOL;
        } else if (comp == BLACK) {
            return CompMaterial.BLACK_WOOL;
        } else if (comp == BLUE) {
            return CompMaterial.BLUE_WOOL;
        } else if (comp == BROWN) {
            return CompMaterial.BROWN_WOOL;
        } else if (comp == DARK_AQUA) {
            return CompMaterial.CYAN_WOOL;
        } else if (comp == DARK_BLUE) {
            return CompMaterial.BLUE_WOOL;
        } else if (comp == DARK_GRAY) {
            return CompMaterial.GRAY_WOOL;
        } else if (comp == DARK_GREEN) {
            return CompMaterial.GREEN_WOOL;
        } else if (comp == DARK_PURPLE) {
            return CompMaterial.PURPLE_WOOL;
        } else if (comp == DARK_RED) {
            return CompMaterial.RED_WOOL;
        } else if (comp == GOLD) {
            return CompMaterial.ORANGE_WOOL;
        } else if (comp == GRAY) {
            return CompMaterial.LIGHT_GRAY_WOOL;
        } else if (comp == GREEN) {
            return CompMaterial.LIME_WOOL;
        } else if (comp == LIGHT_PURPLE) {
            return CompMaterial.MAGENTA_WOOL;
        } else if (comp == PINK) {
            return CompMaterial.PINK_WOOL;
        } else if (comp == RED) {
            return CompMaterial.RED_WOOL;
        } else if (comp == WHITE) {
            return CompMaterial.WHITE_WOOL;
        } else {
            return comp == YELLOW ? CompMaterial.YELLOW_WOOL : CompMaterial.WHITE_WOOL;
        }
    }

    public static List<ChatColor> getChatColors() {
        List<ChatColor> list = new ArrayList();
        ChatColor[] var1 = ChatColor.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ChatColor color = var1[var3];
            if (color.isColor() && !color.isFormat()) {
                list.add(color);
            }
        }

        return list;
    }

    public static CompColor[] values() {
        return (CompColor[])values.toArray(new CompColor[values.size()]);
    }

    public static CompColor valueOf(String name) {
        return fromName(name);
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public DyeColor getDye() {
        return this.dye;
    }

    public ChatColor getChatColor() {
        return this.chatColor;
    }

    static {
        BLUE = new CompColor("BLUE", DyeColor.BLUE);
        BLACK = new CompColor("BLACK", DyeColor.BLACK);
        DARK_AQUA = new CompColor("DARK_AQUA", DyeColor.CYAN);
        DARK_BLUE = new CompColor("DARK_BLUE", DyeColor.BLUE);
        AQUA = new CompColor("AQUA", DyeColor.LIGHT_BLUE);
        GRAY = new CompColor("GRAY", (DyeColor)getEnum("LIGHT_GRAY", "SILVER", DyeColor.class), (ChatColor)null, "SILVER");
        DARK_GRAY = new CompColor("DARK_GRAY", DyeColor.GRAY);
        DARK_GREEN = new CompColor("DARK_GREEN", DyeColor.GREEN);
        GREEN = new CompColor("GREEN", DyeColor.LIME);
        GOLD = new CompColor("GOLD", DyeColor.ORANGE);
        BROWN = new CompColor("BROWN", DyeColor.BROWN, ChatColor.GOLD);
        DARK_RED = new CompColor("DARK_RED", DyeColor.RED);
        RED = new CompColor("RED", DyeColor.RED);
        WHITE = new CompColor("WHITE", DyeColor.WHITE);
        YELLOW = new CompColor("YELLOW", DyeColor.YELLOW);
        DARK_PURPLE = new CompColor("DARK_PURPLE", DyeColor.PURPLE);
        LIGHT_PURPLE = new CompColor("LIGHT_PURPLE", DyeColor.MAGENTA);
        PINK = new CompColor("PINK", DyeColor.PINK, ChatColor.LIGHT_PURPLE);
    }
}