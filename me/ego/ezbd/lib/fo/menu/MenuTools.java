package me.ego.ezbd.lib.fo.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.menu.tool.Tool;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

public abstract class MenuTools extends Menu {
    private final List<ToggleableTool> tools;

    protected MenuTools() {
        this((Menu)null);
    }

    protected MenuTools(Menu parent) {
        super(parent);
        this.tools = this.compile0(this.compileTools());
        int items = this.tools.size();
        int pages = items < 9 ? 9 : (items < 18 ? 18 : (items < 27 ? 27 : (items < 36 ? 36 : 45)));
        this.setSize(pages);
        this.setTitle(me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.TITLE_TOOLS);
    }

    protected abstract Object[] compileTools();

    protected Object[] lookupTools(Class<? extends Tool> extendingClass) {
        List<Object> instances = new ArrayList();
        Iterator var3 = ReflectionUtil.getClasses(SimplePlugin.getInstance(), extendingClass).iterator();

        while(var3.hasNext()) {
            Class clazz = (Class)var3.next();

            try {
                Object instance = ReflectionUtil.getFieldContent(clazz, "instance", (Object)null);
                instances.add(instance);
            } catch (Throwable var6) {
            }
        }

        return instances.toArray();
    }

    private final List<ToggleableTool> compile0(Object... tools) {
        List<ToggleableTool> list = new ArrayList();
        if (tools != null) {
            Object[] var3 = tools;
            int var4 = tools.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Object tool = var3[var5];
                list.add(new ToggleableTool(tool));
            }
        }

        return list;
    }

    public final ItemStack getItemAt(int slot) {
        return slot < this.tools.size() ? ((ToggleableTool)this.tools.get(slot)).get(this.getViewer()) : null;
    }

    public final void onMenuClick(Player pl, int slot, InventoryAction action, ClickType click, ItemStack cursor, ItemStack item, boolean cancelled) {
        ItemStack it = this.getItemAt(slot);
        ToggleableTool tool = it != null ? this.findTool(it) : null;
        if (tool != null) {
            tool.giveOrTake(pl);
            this.redraw();
        }

    }

    private final ToggleableTool findTool(ItemStack item) {
        Iterator var2 = this.tools.iterator();

        ToggleableTool h;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            h = (ToggleableTool)var2.next();
        } while(!h.equals(item));

        return h;
    }

    protected int getInfoButtonPosition() {
        return this.getSize() - 1;
    }

    public static final MenuTools of(final Class<? extends Tool> pluginToolClasses, final String... description) {
        return new MenuTools() {
            protected Object[] compileTools() {
                return this.lookupTools(pluginToolClasses);
            }

            protected String[] getInfo() {
                return description;
            }
        };
    }
}