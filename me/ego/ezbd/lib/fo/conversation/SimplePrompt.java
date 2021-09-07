package me.ego.ezbd.lib.fo.conversation;

import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.model.Variables;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;

public abstract class SimplePrompt extends ValidatingPrompt implements Cloneable {
    private boolean openMenu = true;
    private Player player = null;

    protected SimplePrompt() {
    }

    protected SimplePrompt(boolean openMenu) {
        this.openMenu = openMenu;
    }

    protected String getCustomPrefix() {
        return null;
    }

    public final String getPromptText(ConversationContext context) {
        return String.join("\n", Common.splitNewline(Variables.replace(this.getPrompt(context), this.getPlayer(context))));
    }

    protected abstract String getPrompt(ConversationContext var1);

    protected boolean isInputValid(ConversationContext context, String input) {
        return true;
    }

    protected String getFailedValidationText(ConversationContext context, String invalidInput) {
        return null;
    }

    protected final Player getPlayer(ConversationContext ctx) {
        Valid.checkBoolean(ctx.getForWhom() instanceof Player, "Conversable is not a player but: " + ctx.getForWhom(), new Object[0]);
        return (Player)ctx.getForWhom();
    }

    protected final void tell(String message) {
        Valid.checkNotNull(this.player, "Cannot use tell() when player not yet set!");
        this.tell((Conversable)this.player, message);
    }

    protected final void tell(ConversationContext context, String message) {
        this.tell((Conversable)this.getPlayer(context), message);
    }

    protected final void tell(Conversable conversable, String message) {
        Common.tellConversing(conversable, (this.getCustomPrefix() != null ? this.getCustomPrefix() : "") + message);
    }

    protected final void tellLater(int delayTicks, Conversable conversable, String message) {
        Common.tellLaterConversing(delayTicks, conversable, (this.getCustomPrefix() != null ? this.getCustomPrefix() : "") + message);
    }

    public void onConversationEnd(SimpleConversation conversation, ConversationAbandonedEvent event) {
    }

    public final Prompt acceptInput(ConversationContext context, String input) {
        if (this.isInputValid(context, input)) {
            return this.acceptValidatedInput(context, input);
        } else {
            String failPrompt = this.getFailedValidationText(context, input);
            if (failPrompt != null) {
                this.tellLater(1, context.getForWhom(), Variables.replace("&c" + failPrompt, this.getPlayer(context)));
            }

            return this;
        }
    }

    public final SimpleConversation show(Player player) {
        Valid.checkBoolean(!player.isConversing(), "Player " + player.getName() + " is already conversing! Show them their next prompt in acceptValidatedInput() in " + this.getClass().getSimpleName() + " instead!", new Object[0]);
        this.player = player;
        SimpleConversation conversation = new SimpleConversation() {
            protected Prompt getFirstPrompt() {
                return SimplePrompt.this;
            }

            protected ConversationPrefix getPrefix() {
                String prefix = SimplePrompt.this.getCustomPrefix();
                return (ConversationPrefix)(prefix != null ? new SimplePrefix(prefix) : super.getPrefix());
            }
        };
        if (this.openMenu) {
            Menu menu = Menu.getMenu(player);
            if (menu != null) {
                conversation.setMenuToReturnTo(menu);
            }
        }

        conversation.start(player);
        return conversation;
    }

    public SimplePrompt clone() {
        try {
            return (SimplePrompt)super.clone();
        } catch (Throwable var2) {
            throw var2;
        }
    }

    public static final void show(Player player, SimplePrompt prompt) {
        prompt.show(player);
    }
}
