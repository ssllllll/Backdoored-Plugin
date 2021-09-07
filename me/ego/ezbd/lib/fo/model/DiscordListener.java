package me.ego.ezbd.lib.fo.model;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.collection.StrictSet;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class DiscordListener implements Listener {
    private static final StrictSet<DiscordListener> registeredListeners = new StrictSet();
    private Message message;

    public static final void clearRegisteredListeners() {
        registeredListeners.clear();
    }

    protected DiscordListener() {
        registeredListeners.add(this);
    }

    public void register() {
        if (!registeredListeners.contains(this)) {
            registeredListeners.add(this);
        }

    }

    private final void handleMessageReceived(DiscordGuildMessagePreProcessEvent event) {
        this.message = event.getMessage();
        this.onMessageReceived(event);
    }

    protected abstract void onMessageReceived(DiscordGuildMessagePreProcessEvent var1);

    protected void onMessageSent(GameChatMessagePreProcessEvent event) {
    }

    protected final Player findPlayer(String playerName, String offlineMessage) {
        Player player = Bukkit.getPlayer(playerName);
        this.checkBoolean(player != null, offlineMessage);
        return player;
    }

    protected final void checkBoolean(boolean value, String warningMessage) throws DiscordListener.RemovedMessageException {
        if (!value) {
            this.returnHandled(warningMessage);
        }

    }

    protected final void returnHandled(String message) {
        this.removeAndWarn(message);
        throw new DiscordListener.RemovedMessageException();
    }

    protected final void removeAndWarn(String warningMessage) {
        this.removeAndWarn(this.message, warningMessage);
    }

    protected final void removeAndWarn(Message message, String warningMessage) {
        this.removeAndWarn(message, warningMessage, 2);
    }

    protected final void removeAndWarn(Message message, String warningMessage, int warningDurationSeconds) {
        message.delete().complete();
        MessageChannel channel = message.getChannel();
        Message channelWarningMessage = (Message)channel.sendMessage(warningMessage).complete();
        channel.deleteMessageById(channelWarningMessage.getIdLong()).completeAfter((long)warningDurationSeconds, TimeUnit.SECONDS);
    }

    protected final boolean hasRole(Member member, String roleName) {
        Iterator var3 = member.getRoles().iterator();

        Role role;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            role = (Role)var3.next();
        } while(!role.getName().equalsIgnoreCase(roleName));

        return true;
    }

    protected final void sendMessage(Player sender, String channel, String message) {
        HookManager.sendDiscordMessage(sender, channel, message);
    }

    protected final void sendMessage(String channel, String message) {
        HookManager.sendDiscordMessage(channel, message);
    }

    protected final Set<String> getChannels() {
        return HookManager.getDiscordChannels();
    }

    /** @deprecated */
    @Deprecated
    public static final class DiscordListenerImpl implements Listener {
        private static volatile DiscordListener.DiscordListenerImpl instance = new DiscordListener.DiscordListenerImpl();

        public void resubscribe() {
            DiscordSRV.api.unsubscribe(this);
            DiscordSRV.api.subscribe(this);
        }

        public void registerHook() {
            try {
                DiscordSRV.getPlugin().getPluginHooks().add(SimplePlugin::getInstance);
            } catch (Error var2) {
            }

        }

        @Subscribe(
            priority = ListenerPriority.HIGHEST
        )
        public void onMessageReceived(DiscordGuildMessagePreProcessEvent event) {
            Iterator var2 = DiscordListener.registeredListeners.iterator();

            while(var2.hasNext()) {
                DiscordListener listener = (DiscordListener)var2.next();

                try {
                    listener.handleMessageReceived(event);
                } catch (DiscordListener.RemovedMessageException var5) {
                } catch (Throwable var6) {
                    Common.error(var6, new String[]{"Failed to handle DiscordSRV->Minecraft message!", "Sender: " + event.getAuthor().getName(), "Channel: " + event.getChannel().getName(), "Message: " + event.getMessage().getContentDisplay()});
                }
            }

        }

        @Subscribe(
            priority = ListenerPriority.HIGHEST
        )
        public void onMessageSend(GameChatMessagePreProcessEvent event) {
            Iterator var2 = DiscordListener.registeredListeners.iterator();

            while(var2.hasNext()) {
                DiscordListener listener = (DiscordListener)var2.next();

                try {
                    listener.onMessageSent(event);
                } catch (DiscordListener.RemovedMessageException var5) {
                } catch (Throwable var6) {
                    Common.error(var6, new String[]{"Failed to handle Minecraft->DiscordSRV message!", "Sender: " + event.getPlayer().getName(), "Channel: " + event.getChannel(), "Message: " + event.getMessage()});
                }
            }

        }

        private DiscordListenerImpl() {
        }

        public static DiscordListener.DiscordListenerImpl getInstance() {
            return instance;
        }
    }

    private static final class RemovedMessageException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private RemovedMessageException() {
        }
    }
}
