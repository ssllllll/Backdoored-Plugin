package me.ego.ezbd.lib.fo.menu.tool;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.inventory.ItemStack;

public final class ToolRegistry {
    private static final Collection<Tool> tools = new ConcurrentLinkedQueue();

    public ToolRegistry() {
    }

    static synchronized void register(Tool tool) {
        Valid.checkBoolean(!isRegistered(tool), "Tool with itemstack " + tool.getItem() + " already registered", new Object[0]);
        tools.add(tool);
    }

    static synchronized boolean isRegistered(Tool tool) {
        return getTool(tool.getItem()) != null;
    }

    public static Tool getTool(ItemStack item) {
        Iterator var1 = tools.iterator();

        Tool t;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            t = (Tool)var1.next();
        } while(!t.isTool(item));

        return t;
    }

    public static Tool[] getTools() {
        return (Tool[])tools.toArray(new Tool[tools.size()]);
    }
}