package me.ego.ezbd.lib.fo.menu.button;

import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class ButtonReturnBack extends Button {
    private static CompMaterial material;
    private static String title;
    private static List<String> lore;
    @NonNull
    private final Menu parentMenu;
    private boolean makeNewInstance = false;

    public ItemStack getItem() {
        return ItemCreator.of(material).name(title).lores(lore).build().make();
    }

    public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
        if (this.makeNewInstance) {
            this.parentMenu.newInstance().displayTo(pl);
        } else {
            this.parentMenu.displayTo(pl);
        }

    }

    public ButtonReturnBack(@NonNull Menu parentMenu) {
        if (parentMenu == null) {
            throw new NullPointerException("parentMenu is marked non-null but is null");
        } else {
            this.parentMenu = parentMenu;
        }
    }

    public ButtonReturnBack(@NonNull Menu parentMenu, boolean makeNewInstance) {
        if (parentMenu == null) {
            throw new NullPointerException("parentMenu is marked non-null but is null");
        } else {
            this.parentMenu = parentMenu;
            this.makeNewInstance = makeNewInstance;
        }
    }

    public static CompMaterial getMaterial() {
        return material;
    }

    public static void setMaterial(CompMaterial material) {
        ButtonReturnBack.material = material;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        ButtonReturnBack.title = title;
    }

    public static List<String> getLore() {
        return lore;
    }

    public static void setLore(List<String> lore) {
        ButtonReturnBack.lore = lore;
    }

    static {
        material = CompMaterial.OAK_DOOR;
        title = me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.BUTTON_RETURN_TITLE;
        lore = Arrays.asList(me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.BUTTON_RETURN_LORE);
    }
}