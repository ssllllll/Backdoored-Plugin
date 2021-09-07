package me.ego.ezbd.lib.fo.bungee.message;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.bungee.BungeeAction;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.entity.Player;

public final class OutgoingMessage extends Message {
    private final List<Object> queue = new ArrayList();

    public OutgoingMessage(UUID senderUid, BungeeAction action) {
        this.setSenderUid(senderUid.toString());
        this.setServerName(Remain.getServerName());
        this.setAction(action);
        this.queue.add(senderUid);
        this.queue.add(this.getServerName());
        this.queue.add(this.getAction());
    }

    public void writeString(String... messages) {
        String[] var2 = messages;
        int var3 = messages.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String message = var2[var4];
            this.write(message, String.class);
        }

    }

    public void writeBoolean(boolean bool) {
        this.write(bool, Boolean.class);
    }

    public void writeByte(byte number) {
        this.write(number, Byte.class);
    }

    public void writeDouble(double number) {
        this.write(number, Double.class);
    }

    public void writeFloat(float number) {
        this.write(number, Float.class);
    }

    public void writeInt(int number) {
        this.write(number, Integer.class);
    }

    public void writeLong(long number) {
        this.write(number, Long.class);
    }

    public void writeShort(short number) {
        this.write(number, Short.class);
    }

    private void write(Object object, Class<?> typeOf) {
        Valid.checkNotNull(object, "Added object must not be null!");
        this.moveHead(typeOf);
        this.queue.add(object);
    }

    public void send(Player player) {
        player.sendPluginMessage(SimplePlugin.getInstance(), this.getChannel(), this.compileData());
        Debugger.debug("bungee", new String[]{"Sending data on " + this.getChannel() + " channel from " + this.getAction() + " as " + player.getName() + " player to BungeeCord."});
    }

    private byte[] compileData() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        Iterator var2 = this.queue.iterator();

        while(var2.hasNext()) {
            Object object = var2.next();
            if (object instanceof String) {
                out.writeUTF((String)object);
            } else if (object instanceof Boolean) {
                out.writeBoolean((Boolean)object);
            } else if (object instanceof Byte) {
                out.writeByte((Byte)object);
            } else if (object instanceof Double) {
                out.writeDouble((Double)object);
            } else if (object instanceof Float) {
                out.writeFloat((Float)object);
            } else if (object instanceof Integer) {
                out.writeInt((Integer)object);
            } else if (object instanceof Long) {
                out.writeLong((Long)object);
            } else if (object instanceof Short) {
                out.writeShort((Short)object);
            } else {
                if (!(object instanceof byte[])) {
                    throw new FoException("Unsupported write of " + object.getClass().getSimpleName() + " to channel " + this.getChannel() + " with action " + this.getAction().toString());
                }

                out.write((byte[])((byte[])object));
            }
        }

        return out.toByteArray();
    }
}