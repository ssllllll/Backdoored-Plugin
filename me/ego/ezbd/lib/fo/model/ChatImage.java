package me.ego.ezbd.lib.fo.model;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.remain.CompChatColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class ChatImage {
    private static final char TRANSPARENT_CHAR = ' ';
    private static final Color[] LEGACY_COLORS = new Color[]{new Color(0, 0, 0), new Color(0, 0, 170), new Color(0, 170, 0), new Color(0, 170, 170), new Color(170, 0, 0), new Color(170, 0, 170), new Color(255, 170, 0), new Color(170, 170, 170), new Color(85, 85, 85), new Color(85, 85, 255), new Color(85, 255, 85), new Color(85, 255, 255), new Color(255, 85, 85), new Color(255, 85, 255), new Color(255, 255, 85), new Color(255, 255, 255)};
    private String[] lines;

    public ChatImage appendText(String... text) {
        for(int y = 0; y < this.lines.length; ++y) {
            if (text.length > y) {
                String line = text[y];
                StringBuilder var10000 = new StringBuilder();
                String[] var10002 = this.lines;
                var10002[y] = var10000.append(var10002[y]).append(" ").append(line).toString();
            }
        }

        return this;
    }

    public ChatImage appendCenteredText(String... text) {
        for(int y = 0; y < this.lines.length; ++y) {
            if (text.length <= y) {
                return this;
            }

            int len = 65 - this.lines[y].length();
            this.lines[y] = this.lines[y] + this.center(text[y], len);
        }

        return this;
    }

    private String center(String message, int length) {
        if (message.length() > length) {
            return message.substring(0, length);
        } else if (message.length() == length) {
            return message;
        } else {
            int leftPadding = (length - message.length()) / 2;
            StringBuilder leftBuilder = new StringBuilder();

            for(int i = 0; i < leftPadding; ++i) {
                leftBuilder.append(" ");
            }

            return leftBuilder.toString() + message;
        }
    }

    public void sendToPlayer(CommandSender sender) {
        String[] var2 = this.lines;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String line = var2[var4];
            sender.sendMessage(Variables.replace(line, sender));
        }

    }

    public static ChatImage fromFile(@NonNull File file, int height, ChatImage.Type characterType) throws IOException {
        if (file == null) {
            throw new NullPointerException("file is marked non-null but is null");
        } else {
            Valid.checkBoolean(file.exists(), "Cannot load image from non existing file " + file.toPath(), new Object[0]);
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new NullPointerException("Unable to load image size " + file.length() + " bytes from " + file.toPath());
            } else {
                CompChatColor[][] chatColors = parseImage(image, height);
                ChatImage chatImage = new ChatImage();
                chatImage.lines = parseColors(chatColors, characterType);
                return chatImage;
            }
        }
    }

    public static ChatImage fromLines(String[] lines) {
        ChatImage chatImage = new ChatImage();
        chatImage.lines = lines;
        return chatImage;
    }

    private static CompChatColor[][] parseImage(BufferedImage image, int height) {
        double ratio = (double)image.getHeight() / (double)image.getWidth();
        int width = (int)((double)height / ratio);
        if (width > 10) {
            boolean var11 = true;
        }

        BufferedImage resized = resizeImage(image, (int)((double)height / ratio), height);
        CompChatColor[][] chatImg = new CompChatColor[resized.getWidth()][resized.getHeight()];

        for(int x = 0; x < resized.getWidth(); ++x) {
            for(int y = 0; y < resized.getHeight(); ++y) {
                int rgb = resized.getRGB(x, y);
                CompChatColor closest = getClosestChatColor(new Color(rgb, true));
                chatImg[x][y] = closest;
            }
        }

        return chatImg;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        AffineTransform af = new AffineTransform();
        af.scale((double)width / (double)originalImage.getWidth(), (double)height / (double)originalImage.getHeight());
        AffineTransformOp operation = new AffineTransformOp(af, 1);
        return operation.filter(originalImage, (BufferedImage)null);
    }

    private static CompChatColor getClosestChatColor(Color color) {
        if (!MinecraftVersion.olderThan(V.v1_16)) {
            return CompChatColor.of(color);
        } else if (color.getAlpha() < 128) {
            return null;
        } else {
            int index = 0;
            double best = -1.0D;

            int i;
            for(i = 0; i < LEGACY_COLORS.length; ++i) {
                if (areSimilar(LEGACY_COLORS[i], color)) {
                    return (CompChatColor)CompChatColor.getColors().get(i);
                }
            }

            for(i = 0; i < LEGACY_COLORS.length; ++i) {
                double distance = getDistance(color, LEGACY_COLORS[i]);
                if (distance < best || best == -1.0D) {
                    best = distance;
                    index = i;
                }
            }

            return (CompChatColor)CompChatColor.getColors().get(index);
        }
    }

    private static boolean areSimilar(Color first, Color second) {
        return Math.abs(first.getRed() - second.getRed()) <= 5 && Math.abs(first.getGreen() - second.getGreen()) <= 5 && Math.abs(first.getBlue() - second.getBlue()) <= 5;
    }

    private static double getDistance(Color first, Color second) {
        double rmean = (double)(first.getRed() + second.getRed()) / 2.0D;
        double r = (double)(first.getRed() - second.getRed());
        double g = (double)(first.getGreen() - second.getGreen());
        int b = first.getBlue() - second.getBlue();
        double weightR = 2.0D + rmean / 256.0D;
        double weightG = 4.0D;
        double weightB = 2.0D + (255.0D - rmean) / 256.0D;
        return weightR * r * r + 4.0D * g * g + weightB * (double)b * (double)b;
    }

    private static String[] parseColors(CompChatColor[][] colors, ChatImage.Type imgchar) {
        String[] lines = new String[colors[0].length];

        for(int y = 0; y < colors[0].length; ++y) {
            String line = "";

            for(int x = 0; x < colors.length; ++x) {
                CompChatColor color = colors[x][y];
                line = line + (color != null ? colors[x][y].toString() + imgchar : ' ');
            }

            lines[y] = line + ChatColor.RESET;
        }

        return lines;
    }

    private ChatImage() {
    }

    public String[] getLines() {
        return this.lines;
    }

    public static enum Type {
        BLOCK('█'),
        DARK_SHADE('▓'),
        MEDIUM_SHADE('▒'),
        LIGHT_SHADE('░');

        private char character;

        private Type(char c) {
            this.character = c;
        }

        public String toString() {
            return String.valueOf(this.character);
        }

        public char getCharacter() {
            return this.character;
        }
    }
}