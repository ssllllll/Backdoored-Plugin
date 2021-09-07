package me.ego.ezbd.lib.fo.bungee.message;

import java.util.UUID;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.bungee.BungeeAction;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;

abstract class Message {
    @Nullable
    private UUID senderUid;
    private String serverName;
    private BungeeAction action;
    private int actionHead = 0;

    protected final void setSenderUid(String raw) {
        if (raw != null) {
            try {
                this.senderUid = UUID.fromString(raw);
            } catch (IllegalArgumentException var3) {
                throw new IllegalArgumentException("Expected UUID, got " + raw + " for packet " + this.action + " from server " + this.serverName);
            }
        }

    }

    protected final void setServerName(String serverName) {
        Valid.checkBoolean(this.serverName == null, "Server name already set", new Object[0]);
        Valid.checkNotNull(serverName, "Server name cannot be null!");
        this.serverName = serverName;
    }

    protected final void setAction(String actionName) {
        BungeeAction action = BungeeAction.getByName(actionName);
        Valid.checkNotNull(action, "Unknown action named: " + actionName + ". Available: " + Common.join(SimplePlugin.getInstance().getBungeeCord().getActions()));
        this.setAction(action);
    }

    protected final void setAction(BungeeAction action) {
        Valid.checkBoolean(this.action == null, "Action already set", new Object[0]);
        this.action = action;
    }

    public <T extends BungeeAction> T getAction() {
        return this.action;
    }

    protected final void moveHead(Class<?> typeOf) {
        Valid.checkNotNull(this.serverName, "Server name not set!");
        Valid.checkNotNull(this.action, "Action not set!");
        Class<?>[] content = this.action.getContent();
        Valid.checkBoolean(this.actionHead < content.length, "Head out of bounds! Max data size for " + this.action.name() + " is " + content.length, new Object[0]);
        ++this.actionHead;
    }

    public final String getChannel() {
        return SimplePlugin.getInstance().getBungeeCord().getChannel();
    }

    protected Message() {
    }

    @Nullable
    public UUID getSenderUid() {
        return this.senderUid;
    }

    public String getServerName() {
        return this.serverName;
    }
}