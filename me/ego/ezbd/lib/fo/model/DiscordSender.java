package me.ego.ezbd.lib.fo.model;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandSender.Spigot;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public final class DiscordSender implements CommandSender {
    private final String name;
    private final User user;
    private final MessageChannel channel;
    private final Message message;

    public boolean isPermissionSet(String permission) {
        throw this.unsupported("isPermissionSet");
    }

    public boolean isPermissionSet(Permission permission) {
        throw this.unsupported("isPermissionSet");
    }

    public boolean hasPermission(String perm) {
        return false;
    }

    public boolean hasPermission(Permission perm) {
        return false;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        throw this.unsupported("addAttachment");
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        throw this.unsupported("addAttachment");
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        throw this.unsupported("addAttachment");
    }

    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        throw this.unsupported("addAttachment");
    }

    public void removeAttachment(PermissionAttachment attachment) {
        throw this.unsupported("removeAttachment");
    }

    public void recalculatePermissions() {
        throw this.unsupported("recalculatePermissions");
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw this.unsupported("getEffectivePermissions");
    }

    public boolean isOp() {
        throw this.unsupported("isOp");
    }

    public void setOp(boolean op) {
        throw this.unsupported("setOp");
    }

    public void sendMessage(String[] messages) {
        String[] var2 = messages;
        int var3 = messages.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String message = var2[var4];
            this.sendMessage(message);
        }

    }

    public void sendMessage(String message) {
        String finalMessage = Common.stripColors(message);
        Common.runAsync(() -> {
            Message sentMessage = (Message)this.channel.sendMessage(finalMessage).complete();

            try {
                this.channel.deleteMessageById(sentMessage.getIdLong()).completeAfter(4L, TimeUnit.SECONDS);
            } catch (Throwable var4) {
                if (!var4.toString().contains("Unknown Message")) {
                    var4.printStackTrace();
                }
            }

        });
    }

    public String getName() {
        return this.name;
    }

    public Server getServer() {
        return Bukkit.getServer();
    }

    public Spigot spigot() {
        throw this.unsupported("spigot");
    }

    private FoException unsupported(String method) {
        return new FoException("DiscordSender cannot invoke " + method + "()");
    }

    public void sendMessage(UUID uuid, String message) {
        this.sendMessage(message);
    }

    public void sendMessage(UUID uuid, String[] messages) {
        this.sendMessage(messages);
    }

    public User getUser() {
        return this.user;
    }

    public MessageChannel getChannel() {
        return this.channel;
    }

    public Message getMessage() {
        return this.message;
    }

    public DiscordSender(String name, User user, MessageChannel channel, Message message) {
        this.name = name;
        this.user = user;
        this.channel = channel;
        this.message = message;
    }
}