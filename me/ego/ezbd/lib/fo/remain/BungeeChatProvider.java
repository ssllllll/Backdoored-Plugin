package me.ego.ezbd.lib.fo.remain;

import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class BungeeChatProvider {
    BungeeChatProvider() {
    }

    static void sendComponent(CommandSender sender, Object comps) {
        if (comps instanceof TextComponent) {
            sendComponent0(sender, (TextComponent)comps);
        } else {
            sendComponent0(sender, (BaseComponent[])((BaseComponent[])comps));
        }

    }

    private static void sendComponent0(CommandSender sender, BaseComponent... comps) {
        StringBuilder plainMessage = new StringBuilder();
        BaseComponent[] var3 = comps;
        int var4 = comps.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            BaseComponent comp = var3[var5];
            plainMessage.append(comp.toLegacyText().replaceAll("§x", ""));
        }

        if (!(sender instanceof Player)) {
            tell0(sender, plainMessage.toString());
        } else {
            try {
                if (MinecraftVersion.equals(V.v1_7)) {
                    Class<?> chatBaseComponentClass = ReflectionUtil.getNMSClass("IChatBaseComponent");
                    Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutChat");
                    Object chatBaseComponent = Remain.toIChatBaseComponent(comps);
                    Object packet = ReflectionUtil.instantiate(ReflectionUtil.getConstructor(packetClass, new Class[]{chatBaseComponentClass}), new Object[]{chatBaseComponent});
                    Remain.sendPacket((Player)sender, packet);
                } else {
                    ((Player)sender).spigot().sendMessage(comps);
                }
            } catch (Throwable var7) {
                if (MinecraftVersion.atLeast(V.v1_7) && !Bukkit.getName().contains("Cauldron")) {
                    Common.throwError(var7, new String[]{"Failed to send component: " + plainMessage.toString() + " to " + sender.getName()});
                }

                tell0(sender, plainMessage.toString());
            }

        }
    }

    private static void tell0(CommandSender sender, String msg) {
        Valid.checkNotNull(sender, "Sender cannot be null");
        if (!msg.isEmpty() && !"none".equals(msg)) {
            String stripped = msg.startsWith("[JSON]") ? msg.replaceFirst("\\[JSON\\]", "").trim() : msg;
            String[] var3 = stripped.split("\n");
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String part = var3[var5];
                sender.sendMessage(part);
            }

        }
    }
}