package me.ego.ezbd.lib.fo.menu.button;

import java.util.Arrays;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.remain.CompColor;
import me.ego.ezbd.lib.fo.remain.CompItemFlag;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ButtonRemove extends Button {
    private final Menu parentMenu;
    private final String toRemoveType;
    private final String toRemoveName;
    private final ButtonRemove.ButtonRemoveAction removeAction;

    public ItemStack getItem() {
        return ItemCreator.of(CompMaterial.LAVA_BUCKET).name("&4&lRemove " + this.toRemoveName).lores(Arrays.asList("&r", "&7The selected " + this.toRemoveType + " will", "&7be removed permanently.")).flag(CompItemFlag.HIDE_ATTRIBUTES).build().make();
    }

    public ItemStack getRemoveConfirmItem() {
        return ItemCreator.ofWool(CompColor.RED).name("&6&lRemove " + this.toRemoveName).lores(Arrays.asList("&r", "&7Confirm that this " + this.toRemoveType + " will", "&7be removed permanently.", "&cCannot be undone.")).flag(CompItemFlag.HIDE_ATTRIBUTES).build().make();
    }

    public String getMenuTitle() {
        return "&0Confirm removal";
    }

    public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
        (new ButtonRemove.MenuDialogRemove(this.parentMenu, new ButtonRemove.RemoveConfirmButton())).displayTo(pl);
    }

    public ButtonRemove(Menu parentMenu, String toRemoveType, String toRemoveName, ButtonRemove.ButtonRemoveAction removeAction) {
        this.parentMenu = parentMenu;
        this.toRemoveType = toRemoveType;
        this.toRemoveName = toRemoveName;
        this.removeAction = removeAction;
    }

    final class MenuDialogRemove extends Menu {
        private final Button confirmButton;
        private final Button returnButton;

        public MenuDialogRemove(Menu parentMenu, ButtonRemove.RemoveConfirmButton confirmButton) {
            super(parentMenu);
            this.confirmButton = confirmButton;
            this.returnButton = new ButtonReturnBack(parentMenu);
            this.setSize(27);
            this.setTitle(ButtonRemove.this.getMenuTitle());
        }

        public ItemStack getItemAt(int slot) {
            if (slot == 12) {
                return this.confirmButton.getItem();
            } else {
                return slot == 14 ? this.returnButton.getItem() : null;
            }
        }

        protected boolean addReturnButton() {
            return false;
        }

        protected String[] getInfo() {
            return null;
        }
    }

    @FunctionalInterface
    public interface ButtonRemoveAction {
        void remove(String var1);
    }

    final class RemoveConfirmButton extends Button {
        public ItemStack getItem() {
            return ButtonRemove.this.getRemoveConfirmItem();
        }

        public void onClickedInMenu(Player player, Menu menu, ClickType click) {
            player.closeInventory();
            ButtonRemove.this.removeAction.remove(ButtonRemove.this.toRemoveName);
            Common.tell(player, new String[]{me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.ITEM_DELETED.replace("{item}", (!ButtonRemove.this.toRemoveType.isEmpty() ? ButtonRemove.this.toRemoveType + " " : "") + ButtonRemove.this.toRemoveName)});
        }

        private RemoveConfirmButton() {
        }
    }
}