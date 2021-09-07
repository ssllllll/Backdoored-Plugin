package me.ego.ezbd.lib.fo.bungee;

import me.ego.ezbd.lib.fo.bungee.message.IncomingMessage;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

public abstract class BungeeListener implements Listener, PluginMessageListener {
    protected BungeeListener() {
    }

    public final void onPluginMessageReceived(String tag, Player player, byte[] data) {
        if (!Bukkit.getName().contains("Cauldron")) {
            if (tag.equals(SimplePlugin.getInstance().getBungeeCord().getChannel())) {
                IncomingMessage message = new IncomingMessage(data);
                Debugger.debug("bungee", new String[]{"Channel " + message.getChannel() + " received " + message.getAction() + " message from " + message.getServerName() + " server."});
                this.onMessageReceived(player, message);
            }

        }
    }

    public abstract void onMessageReceived(Player var1, IncomingMessage var2);
}
