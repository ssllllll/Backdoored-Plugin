package me.ego.ezbd.lib.fo;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;

public class ASCIIUtil {
    public static final int SMALL = 12;
    public static final int MEDIUM = 18;
    public static final int LARGE = 24;

    public ASCIIUtil() {
    }

    public static List<String> generate(String message) {
        return generate(message, 18, Arrays.asList("*"));
    }

    public static List<String> generate(String message, @NonNull String letterSymbols) {
        if (letterSymbols == null) {
            throw new NullPointerException("letterSymbols is marked non-null but is null");
        } else {
            return generate(message, 18, Arrays.asList(letterSymbols.split("\\|")));
        }
    }

    public static List<String> generate(String message, int textHeight, @NonNull List<String> letterSymbols) {
        if (letterSymbols == null) {
            throw new NullPointerException("letterSymbols is marked non-null but is null");
        } else {
            List<String> texts = new ArrayList();
            int imageWidth = findImageWidth(textHeight, message, "SansSerif");
            BufferedImage bufferedImage = new BufferedImage(imageWidth, textHeight, 1);
            Graphics2D graphics = (Graphics2D)bufferedImage.getGraphics();
            Font font = new Font("SansSerif", 1, textHeight);
            graphics.setFont(font);
            graphics.drawString(message, 0, getBaselinePosition(graphics, font));

            for(int y = 0; y < textHeight; ++y) {
                StringBuilder sb = new StringBuilder();

                for(int x = 0; x < imageWidth; ++x) {
                    sb.append(bufferedImage.getRGB(x, y) == Color.WHITE.getRGB() ? (String)RandomUtil.nextItem(letterSymbols) : " ");
                }

                if (!sb.toString().trim().isEmpty()) {
                    texts.add(sb.toString());
                }
            }

            return texts;
        }
    }

    private static int findImageWidth(int textHeight, String artText, String fontName) {
        BufferedImage bufferedImage = new BufferedImage(1, 1, 1);
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setFont(new Font(fontName, 1, textHeight));
        return graphics.getFontMetrics().stringWidth(artText);
    }

    private static int getBaselinePosition(Graphics g, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int y = metrics.getAscent() - metrics.getDescent();
        return y;
    }
}