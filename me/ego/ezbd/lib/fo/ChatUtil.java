package me.ego.ezbd.lib.fo;

import java.awt.Color;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.model.Whiteblacklist;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

public final class ChatUtil {
    public static final int CENTER_PX = 152;
    public static final int VISIBLE_CHAT_LINES = 20;

    public static String center(String message) {
        return center(message, ' ');
    }

    public static String center(String message, int centerPx) {
        return center(message, ' ', centerPx);
    }

    public static String center(String message, char space) {
        return center(message, space, 152);
    }

    public static String center(String message, char space, int centerPx) {
        if (message != null && !message.equals("")) {
            int messagePxSize = 0;
            boolean previousCode = false;
            boolean isBold = false;
            char[] var6 = message.toCharArray();
            int halvedMessageSize = var6.length;

            int toCompensate;
            for(toCompensate = 0; toCompensate < halvedMessageSize; ++toCompensate) {
                char c = var6[toCompensate];
                if (c != '&' && c != 167) {
                    if (previousCode) {
                        previousCode = false;
                        if (c != 'l' && c != 'L') {
                            isBold = false;
                        } else {
                            isBold = true;
                        }
                    } else {
                        DefaultFontInfo defaultFont = DefaultFontInfo.getDefaultFontInfo(c);
                        messagePxSize += isBold ? defaultFont.getBoldLength() : defaultFont.getLength();
                        ++messagePxSize;
                    }
                } else {
                    previousCode = true;
                }
            }

            StringBuilder builder = new StringBuilder();
            halvedMessageSize = messagePxSize / 2;
            toCompensate = centerPx - halvedMessageSize;
            int spaceLength = DefaultFontInfo.getDefaultFontInfo(space).getLength() + (isBold ? 2 : 1);

            for(int compensated = 0; compensated < toCompensate; compensated += spaceLength) {
                builder.append(space);
            }

            return builder.toString() + " " + message + " " + builder.toString();
        } else {
            return "";
        }
    }

    public static String[] verticalCenter(String... messages) {
        return verticalCenter((Collection)Arrays.asList(messages));
    }

    public static String[] verticalCenter(Collection<String> messages) {
        List<String> lines = new ArrayList();
        long padding = MathUtil.ceiling((double)((20 - messages.size()) / 2));

        int i;
        for(i = 0; (long)i < padding; ++i) {
            lines.add(RandomUtil.nextColorOrDecoration());
        }

        Iterator var6 = messages.iterator();

        while(var6.hasNext()) {
            String message = (String)var6.next();
            lines.add(message);
        }

        for(i = 0; (long)i < padding; ++i) {
            lines.add(RandomUtil.nextColorOrDecoration());
        }

        return (String[])lines.toArray(new String[lines.size()]);
    }

    public static String insertDot(String message) {
        if (message.isEmpty()) {
            return "";
        } else {
            String lastChar = message.substring(message.length() - 1);
            String[] words = message.split("\\s");
            String lastWord = words[words.length - 1];
            if (!isDomain(lastWord) && lastChar.matches("(?i)[a-zЀ-ӿ]")) {
                message = message + ".";
            }

            return message;
        }
    }

    public static String capitalize(String message) {
        if (message.isEmpty()) {
            return "";
        } else {
            String[] sentences = message.split("(?<=[!?\\.])\\s");
            String tempMessage = "";
            String[] var3 = sentences;
            int var4 = sentences.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String sentence = var3[var5];

                try {
                    String word = message.split("\\s")[0];
                    if (!isDomain(word)) {
                        sentence = sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
                    }

                    tempMessage = tempMessage + sentence + " ";
                } catch (ArrayIndexOutOfBoundsException var8) {
                }
            }

            return tempMessage.trim();
        }
    }

    public static String lowercaseSecondChar(String message) {
        if (message.isEmpty()) {
            return "";
        } else {
            String[] sentences = message.split("(?<=[!?\\.])\\s");
            String tempMessage = "";
            String[] var3 = sentences;
            int var4 = sentences.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String sentence = var3[var5];

                try {
                    if (sentence.length() > 2 && !isDomain(message.split("\\s")[0]) && sentence.length() > 2 && Character.isUpperCase(sentence.charAt(0)) && Character.isLowerCase(sentence.charAt(2))) {
                        sentence = sentence.substring(0, 1) + sentence.substring(1, 2).toLowerCase() + sentence.substring(2);
                    }

                    tempMessage = tempMessage + sentence + " ";
                } catch (NullPointerException var8) {
                }
            }

            return tempMessage.trim();
        }
    }

    public static String removeEmoji(String message) {
        if (message == null) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < message.length(); ++i) {
                if (i < message.length() - 1 && Character.isSurrogatePair(message.charAt(i), message.charAt(i + 1))) {
                    ++i;
                } else {
                    builder.append(message.charAt(i));
                }
            }

            return builder.toString();
        }
    }

    public static double getCapsPercentage(String message) {
        if (message.isEmpty()) {
            return 0.0D;
        } else {
            String[] sentences = Common.stripColors(message).split(" ");
            String messageToCheck = "";
            double upperCount = 0.0D;
            String[] var5 = sentences;
            int var6 = sentences.length;

            int var7;
            for(var7 = 0; var7 < var6; ++var7) {
                String sentence = var5[var7];
                if (!isDomain(sentence)) {
                    messageToCheck = messageToCheck + sentence + " ";
                }
            }

            char[] var9 = messageToCheck.toCharArray();
            var6 = var9.length;

            for(var7 = 0; var7 < var6; ++var7) {
                char ch = var9[var7];
                if (Character.isUpperCase(ch)) {
                    ++upperCount;
                }
            }

            return upperCount / (double)messageToCheck.length();
        }
    }

    public static int getCapsInRow(String message, List<String> ignored) {
        if (message.isEmpty()) {
            return 0;
        } else {
            int[] caps = splitCaps(Common.stripColors(message), ignored);
            int sum = 0;
            int sumTemp = 0;
            int[] var5 = caps;
            int var6 = caps.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                int i = var5[var7];
                if (i == 1) {
                    ++sumTemp;
                    sum = Math.max(sum, sumTemp);
                } else {
                    sumTemp = 0;
                }
            }

            return sum;
        }
    }

    public static int getCapsInRow(String message, Whiteblacklist list) {
        if (message.isEmpty()) {
            return 0;
        } else {
            int[] caps = splitCaps(Common.stripColors(message), list);
            int sum = 0;
            int sumTemp = 0;
            int[] var5 = caps;
            int var6 = caps.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                int i = var5[var7];
                if (i == 1) {
                    ++sumTemp;
                    sum = Math.max(sum, sumTemp);
                } else {
                    sumTemp = 0;
                }
            }

            return sum;
        }
    }

    public static double getSimilarityPercentage(String first, String second) {
        if (first.isEmpty() && second.isEmpty()) {
            return 1.0D;
        } else {
            first = removeSimilarity(first);
            second = removeSimilarity(second);
            String longer = first;
            String shorter = second;
            if (first.length() < second.length()) {
                longer = second;
                shorter = first;
            }

            int longerLength = longer.length();
            return longerLength == 0 ? 0.0D : (double)(longerLength - editDistance(longer, shorter)) / (double)longerLength;
        }
    }

    private static String removeSimilarity(String message) {
        if (SimplePlugin.getInstance().similarityStripAccents()) {
            message = replaceDiacritic(message);
        }

        message = Common.stripColors(message);
        message = message.toLowerCase();
        return message;
    }

    public static boolean isDomain(String message) {
        return Common.regExMatch("(https?:\\/\\/(?:www\\.|(?!www))[^\\s\\.]+\\.[^\\s]{2,}|www\\.[^\\s]+\\.[^\\s]{2,})", message);
    }

    public static String replaceDiacritic(String message) {
        return Normalizer.normalize(message, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static boolean isInteractive(String msg) {
        return msg.startsWith("[JSON]") || msg.startsWith("<toast>") || msg.startsWith("<title>") || msg.startsWith("<actionbar>") || msg.startsWith("<bossbar>");
    }

    public static String generateGradient(String message, CompChatColor from, CompChatColor to) {
        if (!MinecraftVersion.atLeast(V.v1_16)) {
            return message;
        } else {
            Color color1 = from.getColor();
            Color color2 = to.getColor();
            char[] letters = message.toCharArray();
            String gradient = "";
            ChatColor lastDecoration = null;

            for(int i = 0; i < letters.length; ++i) {
                char letter = letters[i];
                if (letter == 167 && i + 1 < letters.length) {
                    char decoration = letters[i + 1];
                    if (decoration == 'k') {
                        lastDecoration = ChatColor.MAGIC;
                    } else if (decoration == 'l') {
                        lastDecoration = ChatColor.BOLD;
                    } else if (decoration == 'm') {
                        lastDecoration = ChatColor.STRIKETHROUGH;
                    } else if (decoration == 'n') {
                        lastDecoration = ChatColor.UNDERLINE;
                    } else if (decoration == 'o') {
                        lastDecoration = ChatColor.ITALIC;
                    } else if (decoration == 'r') {
                        lastDecoration = null;
                    }

                    ++i;
                } else {
                    float ratio = (float)i / (float)letters.length;
                    int red = (int)((float)color2.getRed() * ratio + (float)color1.getRed() * (1.0F - ratio));
                    int green = (int)((float)color2.getGreen() * ratio + (float)color1.getGreen() * (1.0F - ratio));
                    int blue = (int)((float)color2.getBlue() * ratio + (float)color1.getBlue() * (1.0F - ratio));
                    Color stepColor = new Color(red, green, blue);
                    gradient = gradient + CompChatColor.of(stepColor).toString() + (lastDecoration == null ? "" : lastDecoration.toString()) + letters[i];
                }
            }

            return gradient;
        }
    }

    private static int editDistance(String first, String second) {
        first = first.toLowerCase();
        second = second.toLowerCase();
        int[] costs = new int[second.length() + 1];

        for(int i = 0; i <= first.length(); ++i) {
            int lastValue = i;

            for(int j = 0; j <= second.length(); ++j) {
                if (i == 0) {
                    costs[j] = j;
                } else if (j > 0) {
                    int newValue = costs[j - 1];
                    if (first.charAt(i - 1) != second.charAt(j - 1)) {
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                    }

                    costs[j - 1] = lastValue;
                    lastValue = newValue;
                }
            }

            if (i > 0) {
                costs[second.length()] = lastValue;
            }
        }

        return costs[second.length()];
    }

    private static int[] splitCaps(String message, List<String> ignored) {
        int[] editedMsg = new int[message.length()];
        String[] parts = message.split(" ");

        int i;
        for(i = 0; i < parts.length; ++i) {
            Iterator var5 = ignored.iterator();

            while(var5.hasNext()) {
                String whitelisted = (String)var5.next();
                if (whitelisted.equalsIgnoreCase(parts[i])) {
                    parts[i] = parts[i].toLowerCase();
                }
            }
        }

        for(i = 0; i < parts.length; ++i) {
            if (isDomain(parts[i])) {
                parts[i] = parts[i].toLowerCase();
            }
        }

        String msg = StringUtils.join(parts, " ");

        for(int i = 0; i < msg.length(); ++i) {
            if (Character.isUpperCase(msg.charAt(i)) && Character.isLetter(msg.charAt(i))) {
                editedMsg[i] = 1;
            } else {
                editedMsg[i] = 0;
            }
        }

        return editedMsg;
    }

    private static int[] splitCaps(String message, Whiteblacklist list) {
        int[] editedMsg = new int[message.length()];
        String[] parts = message.split(" ");

        int i;
        for(i = 0; i < parts.length; ++i) {
            if (list.isInList(parts[i])) {
                parts[i] = parts[i].toLowerCase();
            }
        }

        for(i = 0; i < parts.length; ++i) {
            if (isDomain(parts[i])) {
                parts[i] = parts[i].toLowerCase();
            }
        }

        String msg = StringUtils.join(parts, " ");

        for(int i = 0; i < msg.length(); ++i) {
            if (Character.isUpperCase(msg.charAt(i)) && Character.isLetter(msg.charAt(i))) {
                editedMsg[i] = 1;
            } else {
                editedMsg[i] = 0;
            }
        }

        return editedMsg;
    }

    private ChatUtil() {
    }
}