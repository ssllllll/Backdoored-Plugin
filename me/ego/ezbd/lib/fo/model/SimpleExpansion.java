package me.ego.ezbd.lib.fo.model;

import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import org.bukkit.command.CommandSender;

public abstract class SimpleExpansion {
    protected static final String NO_REPLACE = null;
    protected String[] args;

    public SimpleExpansion() {
    }

    public final String replacePlaceholders(CommandSender sender, String params) {
        this.args = params.split("\\_");
        return this.onReplace(sender, params);
    }

    protected abstract String onReplace(@NonNull CommandSender var1, String var2);

    protected final String join(int startIndex) {
        return Common.joinRange(startIndex, this.args);
    }

    protected final String join(int startIndex, int stopIndex) {
        return Common.joinRange(startIndex, stopIndex, this.args);
    }
}