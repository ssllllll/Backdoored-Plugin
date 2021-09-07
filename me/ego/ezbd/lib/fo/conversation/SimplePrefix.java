package me.ego.ezbd.lib.fo.conversation;

import me.ego.ezbd.lib.fo.Common;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;

public final class SimplePrefix implements ConversationPrefix {
    private final String prefix;

    public String getPrefix(ConversationContext context) {
        return Common.colorize(this.prefix);
    }

    public SimplePrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }
}
