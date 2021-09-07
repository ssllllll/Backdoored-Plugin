package me.ego.ezbd.commands;

import me.ego.ezbd.lib.fo.command.SimpleCommandGroup;
import me.ego.ezbd.lib.fo.command.SimpleSubCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IllegalsCommand extends SimpleSubCommand {
    protected IllegalsCommand(SimpleCommandGroup parent) {
        super(parent, "illegals|illegal|i");
        this.setMinArguments(0);
        this.setUsage("&fGive 64 bedrock, command blocks and barriers to player.");
    }

    protected void onCommand() {
        this.checkConsole();
        if (this.args.length == 0) {
            Player player = (Player)this.sender;
            ItemStack bedrock = new ItemStack(Material.BEDROCK, 64);
            ItemStack barriers = new ItemStack(Material.COMMAND_BLOCK, 64);
            ItemStack commandblocks = new ItemStack(Material.BARRIER, 64);
            player.getInventory().addItem(new ItemStack[]{bedrock, barriers, commandblocks});
        }
    }
}