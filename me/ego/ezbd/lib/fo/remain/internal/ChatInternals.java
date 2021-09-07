package me.ego.ezbd.lib.fo.remain.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.ReflectionUtil.ReflectionException;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** @deprecated */
@Deprecated
public class ChatInternals {
    private static Object enumTitle;
    private static Object enumSubtitle;
    private static Object enumReset;
    private static Constructor<?> tabConstructor;
    private static Constructor<?> titleTimesConstructor;
    private static Constructor<?> titleConstructor;
    private static Constructor<?> subtitleConstructor;
    private static Constructor<?> resetTitleConstructor;
    private static Method componentSerializer;
    private static Constructor<?> chatMessageConstructor;

    public ChatInternals() {
    }

    public static void sendTitleLegacy(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle) {
        Valid.checkBoolean(MinecraftVersion.olderThan(V.v1_12), "This method is unsupported on MC 1.13 and later", new Object[0]);

        try {
            if (titleConstructor != null) {
                resetTitleLegacy(player);
                Object chatSubtitle;
                if (titleTimesConstructor != null) {
                    chatSubtitle = titleTimesConstructor.newInstance(fadeIn, stay, fadeOut);
                    Remain.sendPacket(player, chatSubtitle);
                }

                Object packet;
                if (title != null) {
                    chatSubtitle = serializeText(title);
                    packet = titleConstructor.newInstance(enumTitle, chatSubtitle);
                    Remain.sendPacket(player, packet);
                }

                if (subtitle != null) {
                    chatSubtitle = serializeText(subtitle);
                    packet = subtitleConstructor.newInstance(enumSubtitle, chatSubtitle);
                    Remain.sendPacket(player, packet);
                }

            }
        } catch (ReflectiveOperationException var8) {
            throw new ReflectionException("Error sending title to: " + player.getName(), var8);
        }
    }

    public static void resetTitleLegacy(Player player) {
        Valid.checkBoolean(MinecraftVersion.olderThan(V.v1_12), "This method is unsupported on MC 1.13 and later", new Object[0]);

        try {
            if (resetTitleConstructor != null) {
                Object packet = resetTitleConstructor.newInstance(enumReset, null);
                Remain.sendPacket(player, packet);
            }
        } catch (ReflectiveOperationException var2) {
            throw new ReflectionException("Error resetting title to: " + player.getName());
        }
    }

    public static void sendTablistLegacy(Player player, String headerRaw, String footerRaw) {
        Valid.checkBoolean(MinecraftVersion.olderThan(V.v1_12), "This method is unsupported on MC 1.13 and later", new Object[0]);

        try {
            if (tabConstructor == null) {
                return;
            }

            Object header = serializeText(headerRaw);
            Object packet = tabConstructor.newInstance(header);
            if (footerRaw != null) {
                Object footer = serializeText(footerRaw);
                Field f = packet.getClass().getDeclaredField("b");
                f.setAccessible(true);
                f.set(packet, footer);
            }

            Remain.sendPacket(player, packet);
        } catch (ReflectiveOperationException var7) {
            Common.error(var7, new String[]{"Failed to send tablist to " + player.getName() + ", title: " + headerRaw + " " + footerRaw});
        }

    }

    public static void sendActionBarLegacy(Player player, String message) {
        Valid.checkBoolean(MinecraftVersion.olderThan(V.v1_12), "This method is unsupported on MC 1.13 and later", new Object[0]);
        sendChat(player, message, (byte)2);
    }

    private static void sendChat(Player pl, String text, byte type) {
        try {
            Object message = serializeText(text);
            Valid.checkNotNull(message, "Message cannot be null!");
            Object packet;
            if (MinecraftVersion.atLeast(V.v1_12)) {
                Class<?> chatMessageTypeEnum = ReflectionUtil.getNMSClass("ChatMessageType");
                packet = chatMessageConstructor.newInstance(message, chatMessageTypeEnum.getMethod("a", Byte.TYPE).invoke((Object)null, type));
            } else {
                packet = chatMessageConstructor.newInstance(message, type);
            }

            Remain.sendPacket(pl, packet);
        } catch (ReflectiveOperationException var6) {
            Common.error(var6, new String[]{"Failed to send chat packet type " + type + " to " + pl.getName() + ", message: " + text});
        }

    }

    private static Object serializeText(String text) throws ReflectiveOperationException {
        text = removeBracketsAndColorize(text);

        try {
            return componentSerializer.invoke((Object)null, SerializedMap.of("text", text).toJson());
        } catch (Throwable var2) {
            throw new FoException(var2, "Failed to serialize text: " + text);
        }
    }

    private static String removeBracketsAndColorize(String text) {
        if (text == null) {
            return "";
        } else {
            if (text.startsWith("\"") && text.endsWith("\"") || text.startsWith("'") && text.endsWith("'")) {
                text = text.substring(1, text.length() - 1);
            }

            return Common.colorize(text);
        }
    }

    public static void callStatic() {
    }

    static {
        if (MinecraftVersion.olderThan(V.v1_12) && MinecraftVersion.newerThan(V.v1_6)) {
            try {
                Class<?> chatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");
                Class<?> serializer = null;
                if (MinecraftVersion.newerThan(V.v1_7)) {
                    serializer = chatBaseComponent.getDeclaredClasses()[0];
                } else {
                    serializer = ReflectionUtil.getNMSClass("ChatSerializer");
                }

                componentSerializer = serializer.getMethod("a", String.class);
                Class<?> chatPacket = ReflectionUtil.getNMSClass("PacketPlayOutChat");
                if (MinecraftVersion.newerThan(V.v1_11)) {
                    chatMessageConstructor = chatPacket.getConstructor(chatBaseComponent, ReflectionUtil.getNMSClass("ChatMessageType"));
                } else {
                    chatMessageConstructor = MinecraftVersion.newerThan(V.v1_7) ? chatPacket.getConstructor(chatBaseComponent, Byte.TYPE) : chatPacket.getConstructor(chatBaseComponent);
                }

                if (MinecraftVersion.newerThan(V.v1_7)) {
                    Class<?> titlePacket = ReflectionUtil.getNMSClass("PacketPlayOutTitle");
                    Class<?> enumAction = titlePacket.getDeclaredClasses()[0];
                    enumTitle = enumAction.getField("TITLE").get((Object)null);
                    enumSubtitle = enumAction.getField("SUBTITLE").get((Object)null);
                    enumReset = enumAction.getField("RESET").get((Object)null);
                    tabConstructor = ReflectionUtil.getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(chatBaseComponent);
                    titleTimesConstructor = titlePacket.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
                    titleConstructor = titlePacket.getConstructor(enumAction, chatBaseComponent);
                    subtitleConstructor = titlePacket.getConstructor(enumAction, chatBaseComponent);
                    resetTitleConstructor = titlePacket.getConstructor(enumAction, chatBaseComponent);
                }
            } catch (Exception var5) {
                if (!MinecraftVersion.olderThan(V.v1_8)) {
                    var5.printStackTrace();
                    throw new ReflectionException("Error initiating Chat/Title/ActionBAR API (incompatible Craftbukkit? - " + Bukkit.getVersion() + " / " + Bukkit.getBukkitVersion() + " / " + MinecraftVersion.getServerVersion() + ")", var5);
                }

                Common.log(new String[]{"Error initiating Chat/Title/ActionBAR API. Assuming Thermos or modded. Some features will not work."});
            }
        }

    }
}