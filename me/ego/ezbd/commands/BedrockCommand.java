package me.ego.ezbd.commands;

import me.ego.ezbd.lib.fo.command.SimpleCommandGroup;
import me.ego.ezbd.lib.fo.command.SimpleSubCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BedrockCommand extends SimpleSubCommand {
    protected BedrockCommand(SimpleCommandGroup parent) {
        super(parent, "bedrock|bed|br");
        this.setMinArguments(0);
        this.setUsage("&fGive 64 bedrock to player.");
    }

    protected void onCommand() {
        this.checkConsole();
        if (this.args.length == 0) {
            Player player = (Player)this.sender;
            ItemStack bedrock = new ItemStack(Material.BEDROCK, 64);
            player.getInventory().addItem(new ItemStack[]{bedrock});
        }
    }
}