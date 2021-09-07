package me.ego.ezbd.lib.fo.model;

import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.ChatUtil;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BoxedMessage {
    public static ChatColor LINE_COLOR;
    private final Iterable<? extends CommandSender> recipients;
    private final Player sender;
    private final String[] messages;

    public BoxedMessage(String... messages) {
        this((Iterable)null, (Player)null, messages);
    }

    private BoxedMessage(@Nullable Iterable<? extends CommandSender> recipients, Player sender, String[] messages) {
        this.recipients = recipients == null ? null : Common.toList(recipients);
        this.sender = sender;
        this.messages = messages;
    }

    private void launch() {
        Common.runLater(2, () -> {
            boolean tellPrefixState = Common.ADD_TELL_PREFIX;
            Common.ADD_TELL_PREFIX = false;
            this.sendFrame();
            Common.ADD_TELL_PREFIX = tellPrefixState;
        });
    }

    private void sendFrame() {
        this.sendLine();
        this.sendFrameInternals0();
        this.sendLine();
    }

    private void sendFrameInternals0() {
        int i;
        for(i = 0; i < this.getTopLines(); ++i) {
            this.send("&r");
        }

        String[] var9 = this.messages;
        int var2 = var9.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String message = var9[var3];
            String[] var5 = message.split("\n");
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String part = var5[var7];
                this.send(part);
            }
        }

        for(i = 0; i < this.getBottomLines(); ++i) {
            this.send("&r");
        }

    }

    private int getTopLines() {
        switch(this.length()) {
        case 1:
            return 2;
        case 2:
        case 3:
        case 4:
            return 1;
        default:
            return 0;
        }
    }

    private int getBottomLines() {
        switch(this.length()) {
        case 1:
        case 2:
            return 2;
        case 3:
            return 1;
        default:
            return 0;
        }
    }

    private void sendLine() {
        this.send(LINE_COLOR + Common.chatLineSmooth());
    }

    private int length() {
        int length = 0;
        String[] var2 = this.messages;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String message = var2[var4];
            String[] var6 = message.split("\n");
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                String var10000 = var6[var8];
                ++length;
            }
        }

        return length;
    }

    private void send(String message) {
        message = this.centerMessage0(message);
        if (this.recipients == null) {
            this.broadcast0(message);
        } else {
            this.tell0(message);
        }

    }

    private String centerMessage0(String message) {
        return message.startsWith("<center>") ? ChatUtil.center(message.replaceFirst("\\<center\\>(\\s|)", "")) : message;
    }

    private void broadcast0(String message) {
        if (this.sender != null) {
            Common.broadcast(message, this.sender);
        } else {
            Common.broadcastTo(Remain.getOnlinePlayers(), new String[]{message});
        }

    }

    private void tell0(String message) {
        if (this.sender != null) {
            message = message.replace("{player}", Common.resolveSenderName(this.sender));
        }

        Common.broadcastTo(this.recipients, new String[]{message});
    }

    public BoxedMessage.Replacor find(String... variables) {
        return new BoxedMessage.Replacor(variables);
    }

    public String getMessage() {
        return StringUtils.join(this.messages, "\n");
    }

    public String toString() {
        return "Boxed{" + StringUtils.join(this.messages, ", ") + "}";
    }

    public void broadcast() {
        broadcast((Player)null, this.messages);
    }

    public void broadcastAs(Player sender) {
        (new BoxedMessage((Iterable)null, sender, this.messages)).launch();
    }

    public void tell(CommandSender recipient) {
        tell((Player)null, (Iterable)Arrays.asList(recipient), this.messages);
    }

    public void tell(Iterable<? extends CommandSender> recipients) {
        tell((Player)null, (Iterable)recipients, this.messages);
    }

    public void tellAs(CommandSender receiver, Player sender) {
        tell(sender, (Iterable)Arrays.asList(receiver), this.messages);
    }

    public void tellAs(Iterable<? extends CommandSender> receivers, Player sender) {
        (new BoxedMessage(receivers, sender, this.messages)).launch();
    }

    public static void broadcast(String... messages) {
        broadcast((Player)null, messages);
    }

    public static void broadcast(Player sender, String... messages) {
        (new BoxedMessage((Iterable)null, sender, messages)).launch();
    }

    public static void tell(CommandSender recipient, String... messages) {
        tell((Player)null, (Iterable)Arrays.asList(recipient), messages);
    }

    public static void tell(Iterable<? extends CommandSender> recipients, String... messages) {
        tell((Player)null, (Iterable)recipients, messages);
    }

    public static void tell(Player sender, CommandSender receiver, String... messages) {
        tell(sender, (Iterable)Arrays.asList(receiver), messages);
    }

    public static void tell(Player sender, Iterable<? extends CommandSender> receivers, String... messages) {
        (new BoxedMessage(receivers, sender, messages)).launch();
    }

    static {
        LINE_COLOR = ChatColor.DARK_GRAY;
    }

    public class Replacor {
        private final String[] variables;

        public final BoxedMessage replace(Object... replacements) {
            String message = StringUtils.join(BoxedMessage.this.messages, "%delimiter%");

            for(int i = 0; i < this.variables.length; ++i) {
                String find = this.variables[i];
                if (!find.startsWith("{")) {
                    find = "{" + find;
                }

                if (!find.endsWith("}")) {
                    find = find + "}";
                }

                Object rep = i < replacements.length ? replacements[i] : null;
                message = message.replace(find, rep != null ? Objects.toString(rep) : "");
            }

            String[] copy = message.split("%delimiter%");
            return new BoxedMessage(BoxedMessage.this.recipients, BoxedMessage.this.sender, copy);
        }

        public Replacor(String[] variables) {
            this.variables = variables;
        }
    }
}