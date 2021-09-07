package me.ego.ezbd.lib.fo;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.UUID;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.bungee.BungeeAction;
import me.ego.ezbd.lib.fo.bungee.SimpleBungee;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.MessageTooLargeException;

public final class BungeeUtil {
    @SafeVarargs
    public static <T> void tellBungee(BungeeAction action, T... datas) {
        SimpleBungee bungee = SimplePlugin.getInstance().getBungeeCord();
        Valid.checkNotNull(bungee, SimplePlugin.getNamed() + " does not implement getBungeeCord()!");
        tellBungee(bungee.getChannel(), action, datas);
    }

    @SafeVarargs
    public static <T> void tellBungee(String channel, BungeeAction action, T... datas) {
        Valid.checkBoolean(datas.length == action.getContent().length, "Data count != valid values count in " + action + "! Given data: " + datas.length + " vs needed: " + action.getContent().length, new Object[0]);
        Valid.checkBoolean(Remain.isServerNameChanged(), "Please configure your 'server-name' in server.properties according to mineacademy.org/server-properties first before using BungeeCord features", new Object[0]);
        Debugger.put("bungee", "Server '" + Remain.getServerName() + "' sent bungee message [" + channel + ", " + action + "]: ");
        Player recipient = getThroughWhomSendMessage();
        if (recipient == null) {
            Debugger.put("bungee", "Cannot send " + action + " bungee channel '" + channel + "' message because this server has no players");
        } else {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(recipient.getUniqueId().toString());
            out.writeUTF(Remain.getServerName());
            out.writeUTF(action.toString());
            int actionHead = 0;
            Object[] var6 = datas;
            int var7 = datas.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                Object data = var6[var8];

                try {
                    Valid.checkNotNull(data, "Bungee object in array is null! Array: " + Common.join(datas, ", ", (t) -> {
                        return t == null ? "null" : t.toString() + " (" + t.getClass().getSimpleName() + ")";
                    }));
                    if (data instanceof CommandSender) {
                        data = ((CommandSender)data).getName();
                    }

                    if (data instanceof Integer) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, Integer.class, datas);
                        out.writeInt((Integer)data);
                    } else if (data instanceof Double) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, Double.class, datas);
                        out.writeDouble((Double)data);
                    } else if (data instanceof Long) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, Long.class, datas);
                        out.writeLong((Long)data);
                    } else if (data instanceof Boolean) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, Boolean.class, datas);
                        out.writeBoolean((Boolean)data);
                    } else if (data instanceof String) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, String.class, datas);
                        out.writeUTF((String)data);
                    } else if (data instanceof SerializedMap) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, String.class, datas);
                        out.writeUTF(((SerializedMap)data).toJson());
                    } else if (data instanceof UUID) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, UUID.class, datas);
                        out.writeUTF(((UUID)data).toString());
                    } else if (data instanceof Enum) {
                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, Enum.class, datas);
                        out.writeUTF(((Enum)data).toString());
                    } else {
                        if (!(data instanceof byte[])) {
                            throw new FoException("Unknown type of data: " + data + " (" + data.getClass().getSimpleName() + ")");
                        }

                        Debugger.put("bungee", data.toString() + ", ");
                        moveHead(actionHead, action, String.class, datas);
                        out.write((byte[])((byte[])data));
                    }

                    ++actionHead;
                } catch (Throwable var12) {
                    var12.printStackTrace();
                    return;
                }
            }

            Debugger.push("bungee");
            byte[] byteArray = out.toByteArray();

            try {
                recipient.sendPluginMessage(SimplePlugin.getInstance(), channel, byteArray);
            } catch (MessageTooLargeException var11) {
                Common.log(new String[]{"Outgoing bungee message '" + action + "' was oversized, not sending. Max length: 32766 bytes, got " + byteArray.length + " bytes."});
            }

            int actionHead = false;
        }
    }

    public static void tellNative(Player sender, Object... datas) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        Object[] var3 = datas;
        int var4 = datas.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Object data = var3[var5];
            Valid.checkNotNull(data, "Bungee object in array is null! Array: " + Common.join(datas, ", ", (t) -> {
                return t == null ? "null" : t.toString() + "(" + t.getClass().getSimpleName() + ")";
            }));
            if (data instanceof Integer) {
                out.writeInt((Integer)data);
            } else if (data instanceof Double) {
                out.writeDouble((Double)data);
            } else if (data instanceof Boolean) {
                out.writeBoolean((Boolean)data);
            } else {
                if (!(data instanceof String)) {
                    throw new FoException("Unknown type of data: " + data + " (" + data.getClass().getSimpleName() + ")");
                }

                out.writeUTF((String)data);
            }
        }

        sender.sendPluginMessage(SimplePlugin.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void connect(@NonNull Player player, @NonNull String serverName) {
        if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
        } else if (serverName == null) {
            throw new NullPointerException("serverName is marked non-null but is null");
        } else {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArray);

            try {
                out.writeUTF("Connect");
                out.writeUTF(serverName);
            } catch (Throwable var5) {
                Common.error(var5, new String[]{"Unable to connect " + player.getName() + " to server " + serverName, "Error: %error"});
            }

            player.sendPluginMessage(SimplePlugin.getInstance(), "BungeeCord", byteArray.toByteArray());
        }
    }

    private static Player getThroughWhomSendMessage() {
        return Remain.getOnlinePlayers().isEmpty() ? null : (Player)Remain.getOnlinePlayers().iterator().next();
    }

    private static void moveHead(int actionHead, BungeeAction action, Class<?> typeOf, Object[] datas) throws Throwable {
        Valid.checkNotNull(action, "Action not set!");
        Class<?>[] content = action.getContent();
        Valid.checkBoolean(actionHead < content.length, "Head out of bounds! Max data size for " + action.name() + " is " + content.length + "! Set Debug to [bungee] in settings.yml and report. Data length: " + datas.length + " data: " + Common.join(datas), new Object[0]);
    }

    private BungeeUtil() {
    }
}