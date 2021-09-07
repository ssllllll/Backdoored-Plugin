package me.ego.ezbd.lib.fo.conversation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;

public final class SimpleCanceller implements ConversationCanceller {
    private final List<String> cancelPhrases;

    public SimpleCanceller(String... cancelPhrases) {
        this(Arrays.asList(cancelPhrases));
    }

    public SimpleCanceller(List<String> cancelPhrases) {
        Valid.checkBoolean(!cancelPhrases.isEmpty(), "Cancel phrases are empty for conversation cancel listener!", new Object[0]);
        this.cancelPhrases = cancelPhrases;
    }

    public void setConversation(Conversation conversation) {
    }

    public boolean cancelBasedOnInput(ConversationContext context, String input) {
        Iterator var3 = this.cancelPhrases.iterator();

        String phrase;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            phrase = (String)var3.next();
        } while(!input.equalsIgnoreCase(phrase));

        return true;
    }

    public ConversationCanceller clone() {
        return new SimpleCanceller(this.cancelPhrases);
    }
}
