package me.ego.ezbd.lib.fo.menu;

import me.ego.ezbd.lib.fo.menu.button.Button;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.menu.model.MenuQuantity;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface MenuQuantitable {
    MenuQuantity getQuantity();

    void setQuantity(MenuQuantity var1);

    default int getNextQuantity(ClickType clickType) {
        return clickType == ClickType.LEFT ? -this.getQuantity().getAmount() : this.getQuantity().getAmount();
    }

    default Button getEditQuantityButton(final Menu menu) {
        return new Button() {
            public final void onClickedInMenu(Player pl, Menu clickedMenu, ClickType click) {
                MenuQuantitable.this.setQuantity(click == ClickType.LEFT ? MenuQuantitable.this.getQuantity().previous() : MenuQuantitable.this.getQuantity().next());
                menu.redraw();
                menu.animateTitle("&9Editing quantity set to " + MenuQuantitable.this.getQuantity().getAmount());
            }

            public ItemStack getItem() {
                return ItemCreator.of(CompMaterial.STRING, "Edit Quantity: &7" + MenuQuantitable.this.getQuantity().getAmount(), new String[]{"", "&8< &7Left click to decrease", "&8> &7Right click to increase"}).build().make();
            }
        };
    }
}