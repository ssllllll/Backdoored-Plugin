package me.ego.ezbd.lib.fo.conversation;

import java.util.function.Consumer;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Commands;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class SimpleDecimalPrompt extends SimplePrompt {
    private String question = null;
    private Consumer<Double> successAction;

    protected String getPrompt(ConversationContext ctx) {
        Valid.checkNotNull(this.question, "Please either call setQuestion or override getPrompt");
        return "&6" + this.question;
    }

    protected boolean isInputValid(ConversationContext context, String input) {
        return Valid.isDecimal(input) || Valid.isInteger(input);
    }

    protected String getFailedValidationText(ConversationContext context, String invalidInput) {
        return Commands.INVALID_NUMBER.replace("{input}", invalidInput);
    }

    protected final Prompt acceptValidatedInput(@NonNull ConversationContext context, @NonNull String input) {
        if (context == null) {
            throw new NullPointerException("context is marked non-null but is null");
        } else if (input == null) {
            throw new NullPointerException("input is marked non-null but is null");
        } else {
            return this.acceptValidatedInput(context, Double.parseDouble(input));
        }
    }

    protected Prompt acceptValidatedInput(ConversationContext context, double input) {
        Valid.checkNotNull(this.question, "Please either call setSuccessAction or override acceptValidatedInput");
        this.successAction.accept(input);
        return Prompt.END_OF_CONVERSATION;
    }

    public static void show(Player player, String question, Consumer<Double> successAction) {
        (new SimpleDecimalPrompt(question, successAction)).show(player);
    }

    public SimpleDecimalPrompt() {
    }

    public SimpleDecimalPrompt(String question, Consumer<Double> successAction) {
        this.question = question;
        this.successAction = successAction;
    }

    protected void setQuestion(String question) {
        this.question = question;
    }

    protected void setSuccessAction(Consumer<Double> successAction) {
        this.successAction = successAction;
    }
}
