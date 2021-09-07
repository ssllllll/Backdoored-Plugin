package me.ego.ezbd.lib.fo.conversation;

import java.util.concurrent.TimeUnit;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.expiringmap.ExpiringMap;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.model.BoxedMessage;
import me.ego.ezbd.lib.fo.model.Variables;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompSound;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationCanceller;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public abstract class SimpleConversation implements ConversationAbandonedListener {
    private Menu menuToReturnTo;

    protected SimpleConversation() {
        this((Menu)null);
    }

    protected SimpleConversation(Menu menuToReturnTo) {
        this.menuToReturnTo = menuToReturnTo;
    }

    public final void start(Player player) {
        Valid.checkBoolean(!player.isConversing(), "Player " + player.getName() + " is already conversing!", new Object[0]);
        player.closeInventory();
        SimpleConversation.CustomConversation conversation = new SimpleConversation.CustomConversation(player);
        SimpleConversation.CustomCanceller canceller = new SimpleConversation.CustomCanceller();
        canceller.setConversation(conversation);
        conversation.getCancellers().add(canceller);
        conversation.getCancellers().add(this.getCanceller());
        conversation.addConversationAbandonedListener(this);
        conversation.begin();
    }

    protected abstract Prompt getFirstPrompt();

    public final void conversationAbandoned(ConversationAbandonedEvent event) {
        ConversationContext context = event.getContext();
        Conversable conversing = context.getForWhom();
        Object source = event.getSource();
        boolean timeout = (Boolean)context.getAllSessionData().getOrDefault("FLP#TIMEOUT", false);
        context.getAllSessionData().remove("FLP#TIMEOUT");
        if (source instanceof SimpleConversation.CustomConversation) {
            SimplePrompt lastPrompt = ((SimpleConversation.CustomConversation)source).getLastSimplePrompt();
            if (lastPrompt != null) {
                lastPrompt.onConversationEnd(this, event);
            }
        }

        this.onConversationEnd(event, timeout);
        if (conversing instanceof Player) {
            Player player = (Player)conversing;
            (event.gracefulExit() ? CompSound.SUCCESSFUL_HIT : CompSound.NOTE_BASS).play(player, 1.0F, 1.0F);
            if (this.menuToReturnTo != null && this.reopenMenu()) {
                this.menuToReturnTo.newInstance().displayTo(player);
            }
        }

    }

    protected void onConversationEnd(ConversationAbandonedEvent event, boolean canceledFromInactivity) {
        this.onConversationEnd(event);
    }

    protected void onConversationEnd(ConversationAbandonedEvent event) {
    }

    protected ConversationPrefix getPrefix() {
        return new SimplePrefix(Common.ADD_TELL_PREFIX ? this.addLastSpace(Common.getTellPrefix()) : "");
    }

    private final String addLastSpace(String prefix) {
        return prefix.endsWith(" ") ? prefix : prefix + " ";
    }

    protected ConversationCanceller getCanceller() {
        return new SimpleCanceller(new String[]{"quit", "cancel", "exit"});
    }

    protected boolean insertPrefix() {
        return true;
    }

    protected boolean reopenMenu() {
        return true;
    }

    protected int getTimeout() {
        return 60;
    }

    public void setMenuToReturnTo(Menu menu) {
        this.menuToReturnTo = menu;
    }

    protected static final void tellBoxed(int delayTicks, Conversable conversable, String... messages) {
        Common.runLater(delayTicks, () -> {
            tellBoxed(conversable, messages);
        });
    }

    protected static final void tellBoxed(Conversable conversable, String... messages) {
        BoxedMessage.tell((Player)conversable, messages);
    }

    protected static final void tell(Conversable conversable, String message) {
        Common.tellConversing(conversable, Variables.replace(message, (Player)conversable));
    }

    protected static final void tellLater(int delayTicks, Conversable conversable, String message) {
        Common.tellLaterConversing(delayTicks, conversable, Variables.replace(message, (Player)conversable));
    }

    private final class CustomConversation extends Conversation {
        private SimplePrompt lastSimplePrompt;

        private CustomConversation(Conversable forWhom) {
            super(SimplePlugin.getInstance(), forWhom, SimpleConversation.this.getFirstPrompt());
            this.localEchoEnabled = false;
            if (SimpleConversation.this.insertPrefix() && SimpleConversation.this.getPrefix() != null) {
                this.prefix = SimpleConversation.this.getPrefix();
            }

        }

        public void outputNextPrompt() {
            if (this.currentPrompt == null) {
                this.abandon(new ConversationAbandonedEvent(this));
            } else {
                String promptClass = this.currentPrompt.getClass().getSimpleName();
                String question = this.currentPrompt.getPromptText(this.context);

                try {
                    ExpiringMap<String, Void> askedQuestions = (ExpiringMap)this.context.getAllSessionData().getOrDefault("Asked_" + promptClass, ExpiringMap.builder().expiration((long)SimpleConversation.this.getTimeout(), TimeUnit.SECONDS).build());
                    if (!askedQuestions.containsKey(question)) {
                        askedQuestions.put(question, (Object)null);
                        this.context.setSessionData("Asked_" + promptClass, askedQuestions);
                        this.context.getForWhom().sendRawMessage(this.prefix.getPrefix(this.context) + question);
                    }
                } catch (NoSuchMethodError var4) {
                }

                if (this.currentPrompt instanceof SimplePrompt) {
                    this.lastSimplePrompt = ((SimplePrompt)this.currentPrompt).clone();
                }

                if (!this.currentPrompt.blocksForInput(this.context)) {
                    this.currentPrompt = this.currentPrompt.acceptInput(this.context, (String)null);
                    this.outputNextPrompt();
                }
            }

        }

        private SimplePrompt getLastSimplePrompt() {
            return this.lastSimplePrompt;
        }
    }

    private final class CustomCanceller extends InactivityConversationCanceller {
        public CustomCanceller() {
            super(SimplePlugin.getInstance(), SimpleConversation.this.getTimeout());
        }

        protected void cancelling(Conversation conversation) {
            conversation.getContext().setSessionData("FLP#TIMEOUT", true);
        }
    }
}