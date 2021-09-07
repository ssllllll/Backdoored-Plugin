package me.ego.ezbd.lib.fo.menu;

import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.constants.FoConstants.NBT;
import me.ego.ezbd.lib.fo.menu.button.Button;
import me.ego.ezbd.lib.fo.menu.model.MenuClickLocation;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class MenuListener implements Listener {
    public MenuListener() {
    }

    @EventHandler(
        priority = EventPriority.NORMAL,
        ignoreCancelled = true
    )
    public void onMenuClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player)event.getPlayer();
            Menu menu = Menu.getMenu(player);
            if (menu != null) {
                menu.onMenuClose(player, event.getInventory());
                player.removeMetadata(NBT.TAG_MENU_CURRENT, SimplePlugin.getInstance());
            }

        }
    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player)event.getWhoClicked();
            Menu menu = Menu.getMenu(player);
            if (menu != null) {
                ItemStack slotItem = event.getCurrentItem();
                ItemStack cursor = event.getCursor();
                Inventory clickedInv = Remain.getClickedInventory(event);
                InventoryAction action = event.getAction();
                MenuClickLocation whereClicked = clickedInv != null ? (clickedInv.getType() == InventoryType.CHEST ? MenuClickLocation.MENU : MenuClickLocation.PLAYER_INVENTORY) : MenuClickLocation.OUTSIDE;
                boolean allowed = menu.isActionAllowed(whereClicked, event.getSlot(), slotItem, cursor);
                if (!action.toString().contains("PICKUP") && !action.toString().contains("PLACE") && !action.toString().equals("SWAP_WITH_CURSOR") && action != InventoryAction.CLONE_STACK) {
                    if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || whereClicked != MenuClickLocation.PLAYER_INVENTORY) {
                        event.setResult(Result.DENY);
                        player.updateInventory();
                    }
                } else {
                    if (whereClicked == MenuClickLocation.MENU) {
                        try {
                            Button button = menu.getButton(slotItem);
                            if (button != null) {
                                menu.onButtonClick(player, event.getSlot(), action, event.getClick(), button);
                            } else {
                                menu.onMenuClick(player, event.getSlot(), action, event.getClick(), cursor, slotItem, !allowed);
                            }
                        } catch (Throwable var11) {
                            Common.tell(player, new String[]{me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.ERROR});
                            player.closeInventory();
                            Common.error(var11, new String[]{"Error clicking in menu " + menu});
                        }
                    }

                    if (!allowed) {
                        event.setResult(Result.DENY);
                        player.updateInventory();
                    }
                }
            }

        }
    }
}