package me.ego.ezbd.lib.fo.model;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.server.TemporaryPlayer;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

class ProtocolLibHook {
    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    ProtocolLibHook() {
    }

    final void addPacketListener(Object listener) {
        Valid.checkBoolean(listener instanceof PacketListener, "Listener must extend or implements PacketListener or PacketAdapter", new Object[0]);

        try {
            this.manager.addPacketListener((PacketListener)listener);
        } catch (Throwable var3) {
            Common.error(var3, new String[]{"Failed to register ProtocolLib packet listener! Ensure you have the latest ProtocolLib. If you reloaded, try a fresh startup (some ProtocolLib esp. for 1.8.8 fails on reload)."});
        }

    }

    final void removePacketListeners(Plugin plugin) {
        this.manager.removePacketListeners(plugin);
    }

    final void sendPacket(PacketContainer packet) {
        Iterator var2 = Remain.getOnlinePlayers().iterator();

        while(var2.hasNext()) {
            Player player = (Player)var2.next();
            this.sendPacket(player, packet);
        }

    }

    final void sendPacket(Player player, Object packet) {
        Valid.checkNotNull(player);
        Valid.checkBoolean(packet instanceof PacketContainer, "Packet must be instance of PacketContainer from ProtocolLib", new Object[0]);

        try {
            this.manager.sendServerPacket(player, (PacketContainer)packet);
        } catch (InvocationTargetException var4) {
            Common.error(var4, new String[]{"Failed to send " + ((PacketContainer)packet).getType() + " packet to " + player.getName()});
        }

    }

    final boolean isTemporaryPlayer(Player player) {
        try {
            return player instanceof TemporaryPlayer;
        } catch (NoClassDefFoundError var3) {
            return false;
        }
    }
}