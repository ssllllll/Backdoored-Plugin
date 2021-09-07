package me.ego.ezbd.lib.fo.remain;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.ItemUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;

public final class CompChatColor {
    public static final char COLOR_CHAR = '§';
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    private static final Map<Character, CompChatColor> BY_CHAR = new HashMap();
    private static final Map<String, CompChatColor> BY_NAME = new HashMap();
    public static final CompChatColor BLACK = new CompChatColor('0', "black", new Color(0));
    public static final CompChatColor DARK_BLUE = new CompChatColor('1', "dark_blue", new Color(170));
    public static final CompChatColor DARK_GREEN = new CompChatColor('2', "dark_green", new Color(43520));
    public static final CompChatColor DARK_AQUA = new CompChatColor('3', "dark_aqua", new Color(43690));
    public static final CompChatColor DARK_RED = new CompChatColor('4', "dark_red", new Color(11141120));
    public static final CompChatColor DARK_PURPLE = new CompChatColor('5', "dark_purple", new Color(11141290));
    public static final CompChatColor GOLD = new CompChatColor('6', "gold", new Color(16755200));
    public static final CompChatColor GRAY = new CompChatColor('7', "gray", new Color(11184810));
    public static final CompChatColor DARK_GRAY = new CompChatColor('8', "dark_gray", new Color(5592405));
    public static final CompChatColor BLUE = new CompChatColor('9', "blue", new Color(5592575));
    public static final CompChatColor GREEN = new CompChatColor('a', "green", new Color(5635925));
    public static final CompChatColor AQUA = new CompChatColor('b', "aqua", new Color(5636095));
    public static final CompChatColor RED = new CompChatColor('c', "red", new Color(16733525));
    public static final CompChatColor LIGHT_PURPLE = new CompChatColor('d', "light_purple", new Color(16733695));
    public static final CompChatColor YELLOW = new CompChatColor('e', "yellow", new Color(16777045));
    public static final CompChatColor WHITE = new CompChatColor('f', "white", new Color(16777215));
    public static final CompChatColor MAGIC = new CompChatColor('k', "obfuscated");
    public static final CompChatColor BOLD = new CompChatColor('l', "bold");
    public static final CompChatColor STRIKETHROUGH = new CompChatColor('m', "strikethrough");
    public static final CompChatColor UNDERLINE = new CompChatColor('n', "underline");
    public static final CompChatColor ITALIC = new CompChatColor('o', "italic");
    public static final CompChatColor RESET = new CompChatColor('r', "reset");
    private final char code;
    private final String name;
    private final Color color;
    private final String toString;

    private CompChatColor(char code, String name) {
        this(code, name, (Color)null);
    }

    private CompChatColor(char code, String name, Color color) {
        this.code = code;
        this.name = name;
        this.color = color;
        this.toString = new String(new char[]{'§', code});
        BY_CHAR.put(code, this);
        BY_NAME.put(name.toUpperCase(Locale.ROOT), this);
    }

    private CompChatColor(String name, String toString, int rgb) {
        this.code = '#';
        this.name = name;
        this.color = new Color(rgb);
        this.toString = toString;
    }

    public int hashCode() {
        int hash = 7;
        int hash = 53 * hash + Objects.hashCode(this.toString);
        return hash;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            return obj != null && this.getClass() == obj.getClass() ? Objects.equals(this.toString, ((CompChatColor)obj).toString) : false;
        }
    }

    public char getCode() {
        Valid.checkBoolean(this.code != '#', "Cannot retrieve color code for HEX colors", new Object[0]);
        return this.code;
    }

    public boolean isHex() {
        return this.code == '#';
    }

    public String toReadableString() {
        return this.isHex() ? this.toString + "\\" + this.getName() : ItemUtil.bountify(this.getName());
    }

    public String toSaveableString() {
        return this.isHex() ? this.getName() : this.getName();
    }

    public String toString() {
        return this.toString;
    }

    public static CompChatColor getByChar(char code) {
        return (CompChatColor)BY_CHAR.get(code);
    }

    public static CompChatColor of(Color color) {
        return of("#" + Integer.toHexString(color.getRGB()).substring(2));
    }

    public static CompChatColor of(@NonNull String string) {
        if (string == null) {
            throw new NullPointerException("string is marked non-null but is null");
        } else if (string.startsWith("#") && string.length() == 7) {
            if (!MinecraftVersion.atLeast(V.v1_16)) {
                throw new IllegalArgumentException("Only Minecraft 1.16+ supports # HEX color codes!");
            } else {
                int rgb;
                try {
                    rgb = Integer.parseInt(string.substring(1), 16);
                } catch (NumberFormatException var7) {
                    throw new IllegalArgumentException("Illegal hex string " + string);
                }

                StringBuilder magic = new StringBuilder("§x");
                char[] var3 = string.substring(1).toCharArray();
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    char c = var3[var5];
                    magic.append('§').append(c);
                }

                return new CompChatColor(string, magic.toString(), rgb);
            }
        } else {
            CompChatColor byChar;
            if (string.length() == 2) {
                if (string.charAt(0) != '&') {
                    throw new IllegalArgumentException("Invalid syntax, please use & + color code. Got: " + string);
                }

                byChar = (CompChatColor)BY_CHAR.get(string.charAt(1));
                if (byChar != null) {
                    return byChar;
                }
            } else {
                byChar = (CompChatColor)BY_NAME.get(string.toUpperCase(Locale.ROOT));
                if (byChar != null) {
                    return byChar;
                }

                if (string.equalsIgnoreCase("magic")) {
                    return MAGIC;
                }
            }

            throw new IllegalArgumentException("Could not parse CompChatColor " + string);
        }
    }

    public static CompChatColor[] values() {
        return (CompChatColor[])BY_CHAR.values().toArray(new CompChatColor[BY_CHAR.values().size()]);
    }

    public static List<CompChatColor> getColors() {
        return Arrays.asList(BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE);
    }

    public static List<CompChatColor> getDecorations() {
        return Arrays.asList(MAGIC, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC);
    }

    public String getName() {
        return this.name;
    }

    public Color getColor() {
        return this.color;
    }
}