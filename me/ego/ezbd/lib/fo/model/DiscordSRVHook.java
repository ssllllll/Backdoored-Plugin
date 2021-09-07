package me.ego.ezbd.lib.fo.model;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.io.File;
import java.util.Set;
import me.ego.ezbd.lib.fo.debug.Debugger;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

class DiscordSRVHook implements Listener {
    DiscordSRVHook() {
    }

    Set<String> getChannels() {
        return DiscordSRV.getPlugin().getChannels().keySet();
    }

    boolean sendMessage(String channel, String message) {
        return this.sendMessage((CommandSender)null, channel, message);
    }

    boolean sendMessage(CommandSender sender, String channel, String message) {
        TextChannel textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channel);
        if (textChannel == null) {
            Debugger.debug("discord", new String[]{"[MC->Discord] Could not find Discord channel '" + channel + "'. Available: " + String.join(", ", this.getChannels()) + ". Not sending: " + message});
            return false;
        } else {
            if (sender instanceof Player) {
                Debugger.debug("discord", new String[]{"[MC->Discord] " + sender.getName() + " send message to '" + channel + "' channel. Message: '" + message + "'"});
                DiscordSRV instance = (DiscordSRV)JavaPlugin.getPlugin(DiscordSRV.class);
                File file = new File(SimplePlugin.getData().getParent(), "DiscordSRV/config.yml");
                if (file.exists()) {
                    FileConfiguration discordConfig = YamlConfiguration.loadConfiguration(file);
                    if (discordConfig != null) {
                        String outMessageKey = "DiscordChatChannelMinecraftToDiscord";
                        boolean outMessageOldValue = discordConfig.getBoolean("DiscordChatChannelMinecraftToDiscord");
                        discordConfig.set("DiscordChatChannelMinecraftToDiscord", true);

                        try {
                            instance.processChatMessage((Player)sender, message, channel, false);
                        } finally {
                            discordConfig.set("DiscordChatChannelMinecraftToDiscord", outMessageOldValue);
                        }
                    }
                }
            } else {
                Debugger.debug("discord", new String[]{"[MC->Discord] " + (sender == null ? "No sender " : sender.getName() + " (generic)") + "sent message to '" + channel + "' channel. Message: '" + message + "'"});
                DiscordUtil.sendMessage(textChannel, message);
            }

            return true;
        }
    }
}