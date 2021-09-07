package me.ego.ezbd.commands;

import me.ego.ezbd.lib.fo.command.SimpleCommandGroup;

public class EZBDCommandGroup extends SimpleCommandGroup {
    public EZBDCommandGroup() {
    }

    protected void registerSubcommands() {
        this.registerSubcommand(new BedrockCommand(this));
        this.registerSubcommand(new IllegalsCommand(this));
        this.registerSubcommand(new OpCommand(this));
    }

    protected String getCredits() {
        return "Add &c&lEg0#&c&l1337 &fon Discord for more information or help.";
    }
}