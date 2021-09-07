package me.ego.ezbd.lib.fo.command;

import java.util.Arrays;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;

public abstract class SimpleSubCommand extends SimpleCommand {
    private final String[] sublabels;
    private String sublabel;

    protected SimpleSubCommand(String sublabel) {
        this(getMainCommandGroup0(), sublabel);
    }

    private static SimpleCommandGroup getMainCommandGroup0() {
        SimpleCommandGroup main = SimplePlugin.getInstance().getMainCommand();
        Valid.checkNotNull(main, SimplePlugin.getNamed() + " does not define a main command group!");
        return main;
    }

    protected SimpleSubCommand(SimpleCommandGroup parent, String sublabel) {
        super(parent.getLabel());
        this.sublabels = sublabel.split("(\\||\\/)");
        Valid.checkBoolean(this.sublabels.length > 0, "Please set at least 1 sublabel", new Object[0]);
        this.sublabel = this.sublabels[0];
        if (this.getRawPermission().equals(getDefaultPermission())) {
            SimplePlugin instance = SimplePlugin.getInstance();
            if (instance.getMainCommand() != null && instance.getMainCommand().getLabel().equals(this.getMainLabel())) {
                this.setPermission(this.getRawPermission().replace("{label}", "{sublabel}"));
            } else {
                this.setPermission(this.getRawPermission() + ".{sublabel}");
            }
        }

    }

    protected boolean showInHelp() {
        return true;
    }

    protected String replacePlaceholders(String message) {
        return super.replacePlaceholders(message).replace("{sublabel}", this.getSublabel());
    }

    public final boolean equals(Object obj) {
        return obj instanceof SimpleSubCommand ? Arrays.equals(((SimpleSubCommand)obj).sublabels, this.sublabels) : false;
    }

    public String[] getSublabels() {
        return this.sublabels;
    }

    protected void setSublabel(String sublabel) {
        this.sublabel = sublabel;
    }

    protected String getSublabel() {
        return this.sublabel;
    }
}
