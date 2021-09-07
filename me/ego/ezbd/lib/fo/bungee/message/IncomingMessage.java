package me.ego.ezbd.lib.fo.bungee.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.entity.Player;

public final class IncomingMessage extends Message {
    private final byte[] data;
    private final ByteArrayDataInput input;
    private final ByteArrayInputStream stream;

    public IncomingMessage(byte[] data) {
        this.data = data;
        this.stream = new ByteArrayInputStream(data);
        this.input = ByteStreams.newDataInput(this.stream);
        this.setSenderUid(this.input.readUTF());
        this.setServerName(this.input.readUTF());
        this.setAction(this.input.readUTF());
    }

    public String readString() {
        this.moveHead(String.class);
        return this.input.readUTF();
    }

    public UUID readUUID() {
        this.moveHead(UUID.class);
        return UUID.fromString(this.input.readUTF());
    }

    public SerializedMap readMap() {
        this.moveHead(String.class);
        return SerializedMap.fromJson(this.input.readUTF());
    }

    public <T extends Enum<T>> T readEnum(Class<T> typeOf) {
        this.moveHead(typeOf);
        return ReflectionUtil.lookupEnum(typeOf, this.input.readUTF());
    }

    public boolean readBoolean() {
        this.moveHead(Boolean.class);
        return this.input.readBoolean();
    }

    public byte readByte() {
        this.moveHead(Byte.class);
        return this.input.readByte();
    }

    public byte[] readBytes() {
        this.moveHead(byte[].class);
        byte[] array = new byte[this.stream.available()];

        try {
            this.stream.read(array);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        return array;
    }

    public double readDouble() {
        this.moveHead(Double.class);
        return this.input.readDouble();
    }

    public float readFloat() {
        this.moveHead(Float.class);
        return this.input.readFloat();
    }

    public int writeInt() {
        this.moveHead(Integer.class);
        return this.input.readInt();
    }

    public long readLong() {
        this.moveHead(Long.class);
        return this.input.readLong();
    }

    public short readShort() {
        this.moveHead(Short.class);
        return this.input.readShort();
    }

    public void forward(Player player) {
        player.sendPluginMessage(SimplePlugin.getInstance(), this.getChannel(), this.data);
        Debugger.debug("bungee", new String[]{"Forwarding data on " + this.getChannel() + " channel from " + this.getAction() + " as " + player.getName() + " player to BungeeCord."});
    }

    public byte[] getData() {
        return this.data;
    }
}