package me.ego.ezbd.lib.fo.menu.button;

import java.util.concurrent.Callable;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator.ItemCreatorBuilder;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class ButtonMenu extends Button {
    private final Callable<Menu> menuLateBind;
    private final Menu menuToOpen;
    private final ItemStack item;
    private final boolean newInstance;

    public ButtonMenu(Class<? extends Menu> menuClass, CompMaterial material, String name, String... lore) {
        this((Menu)null, () -> {
            return (Menu)ReflectionUtil.instantiate(menuClass);
        }, ItemCreator.of(material, name, lore).hideTags(true).build().make(), false);
    }

    public ButtonMenu(Callable<Menu> menuLateBind, ItemCreatorBuilder item) {
        this((Menu)null, menuLateBind, item.hideTags(true).build().make(), false);
    }

    public ButtonMenu(Callable<Menu> menuLateBind, ItemStack item) {
        this((Menu)null, menuLateBind, item, false);
    }

    public ButtonMenu(Menu menu, CompMaterial material, String name, String... lore) {
        this(menu, ItemCreator.of(material, name, lore));
    }

    public ButtonMenu(Menu menu, ItemCreatorBuilder item) {
        this(menu, (Callable)null, item.hideTags(true).build().make(), false);
    }

    public ButtonMenu(Menu menu, ItemStack item) {
        this(menu, (Callable)null, item, false);
    }

    public ButtonMenu(Menu menu, ItemStack item, boolean newInstance) {
        this(menu, (Callable)null, item, newInstance);
    }

    private ButtonMenu(Menu menuToOpen, Callable<Menu> menuLateBind, ItemStack item, boolean newInstance) {
        this.menuToOpen = menuToOpen;
        this.menuLateBind = menuLateBind;
        this.item = item;
        this.newInstance = newInstance;
    }

    public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
        if (this.menuLateBind != null) {
            Menu menuToOpen = null;

            try {
                menuToOpen = (Menu)this.menuLateBind.call();
            } catch (Exception var6) {
                var6.printStackTrace();
                return;
            }

            if (this.newInstance) {
                menuToOpen = menuToOpen.newInstance();
            }

            menuToOpen.displayTo(pl);
        } else {
            Valid.checkNotNull(this.menuToOpen, "Report / ButtonTrigger requires either 'late bind menu' or normal menu to be set!");
            if (this.newInstance) {
                this.menuToOpen.newInstance().displayTo(pl);
            } else {
                this.menuToOpen.displayTo(pl);
            }
        }

    }

    public ItemStack getItem() {
        return this.item;
    }
}