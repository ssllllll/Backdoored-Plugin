package me.ego.ezbd.lib.fo.menu;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MathUtil;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.menu.button.Button;
import me.ego.ezbd.lib.fo.menu.model.InventoryDrawer;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public abstract class MenuPagged<T> extends Menu {
    private final Map<Integer, List<T>> pages;
    private int currentPage;
    private Button nextButton;
    private Button prevButton;

    protected MenuPagged(Iterable<T> pages) {
        this((Menu)null, pages);
    }

    protected MenuPagged(Menu parent, Iterable<T> pages) {
        this((Integer)null, parent, pages, false);
    }

    protected MenuPagged(Menu parent, Iterable<T> pages, boolean returnMakesNewInstance) {
        this((Integer)null, parent, pages, returnMakesNewInstance);
    }

    /** @deprecated */
    @Deprecated
    protected MenuPagged(int pageSize, Iterable<T> pages) {
        this(pageSize, (Menu)null, pages);
    }

    /** @deprecated */
    @Deprecated
    protected MenuPagged(int pageSize, Menu parent, Iterable<T> pages) {
        this(pageSize, parent, pages, false);
    }

    /** @deprecated */
    @Deprecated
    protected MenuPagged(int pageSize, Menu parent, Iterable<T> pages, boolean returnMakesNewInstance) {
        this(pageSize, parent, pages, returnMakesNewInstance);
    }

    private MenuPagged(Integer pageSize, Menu parent, Iterable<T> pages, boolean returnMakesNewInstance) {
        super(parent, returnMakesNewInstance);
        this.currentPage = 1;
        int items = this.getItemAmount(pages);
        int autoPageSize = pageSize != null ? pageSize : (items <= 9 ? 9 : (items <= 18 ? 18 : (items <= 27 ? 27 : (items <= 36 ? 36 : 45))));
        this.currentPage = 1;
        this.pages = Common.fillPages(autoPageSize, pages);
        this.setSize(9 + autoPageSize);
        this.setButtons();
    }

    private int getItemAmount(Iterable<T> pages) {
        int amount = 0;

        for(Iterator var3 = pages.iterator(); var3.hasNext(); ++amount) {
            T t = var3.next();
        }

        return amount;
    }

    private void setButtons() {
        boolean hasPages = this.pages.size() > 1;
        this.prevButton = (Button)(hasPages ? this.formPreviousButton() : Button.makeEmpty());
        this.nextButton = (Button)(hasPages ? this.formNextButton() : Button.makeEmpty());
    }

    public Button formPreviousButton() {
        return new Button() {
            final boolean canGo;

            {
                this.canGo = MenuPagged.this.currentPage > 1;
            }

            public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
                if (this.canGo) {
                    MenuPagged.this.currentPage = MathUtil.range(MenuPagged.this.currentPage - 1, 1, MenuPagged.this.pages.size());
                    MenuPagged.this.updatePage();
                }

            }

            public ItemStack getItem() {
                int previousPage = MenuPagged.this.currentPage - 1;
                return ItemCreator.of(this.canGo ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE).name(previousPage == 0 ? me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.PAGE_FIRST : me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.PAGE_PREVIOUS.replace("{page}", String.valueOf(previousPage))).build().make();
            }
        };
    }

    public Button formNextButton() {
        return new Button() {
            final boolean canGo;

            {
                this.canGo = MenuPagged.this.currentPage < MenuPagged.this.pages.size();
            }

            public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
                if (this.canGo) {
                    MenuPagged.this.currentPage = MathUtil.range(MenuPagged.this.currentPage + 1, 1, MenuPagged.this.pages.size());
                    MenuPagged.this.updatePage();
                }

            }

            public ItemStack getItem() {
                boolean lastPage = MenuPagged.this.currentPage == MenuPagged.this.pages.size();
                return ItemCreator.of(this.canGo ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE).name(lastPage ? me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.PAGE_LAST : me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.PAGE_NEXT.replace("{page}", String.valueOf(MenuPagged.this.currentPage + 1))).build().make();
            }
        };
    }

    private void updatePage() {
        this.setButtons();
        this.redraw();
        this.registerButtons();
        Menu.getSound().play(this.getViewer());
        PlayerUtil.updateInventoryTitle(this.getViewer(), this.compileTitle0());
    }

    private String compileTitle0() {
        boolean canAddNumbers = this.addPageNumbers() && this.pages.size() > 1;
        return this.getTitle() + (canAddNumbers ? " &8" + this.currentPage + "/" + this.pages.size() : "");
    }

    protected final void onDisplay(InventoryDrawer drawer) {
        drawer.setTitle(this.compileTitle0());
    }

    protected abstract ItemStack convertToItemStack(T var1);

    protected abstract void onPageClick(Player var1, T var2, ClickType var3);

    protected boolean updateButtonOnClick() {
        return true;
    }

    protected boolean addPageNumbers() {
        return true;
    }

    protected boolean isEmpty() {
        return this.pages.isEmpty() || ((List)this.pages.get(0)).isEmpty();
    }

    public ItemStack getItemAt(int slot) {
        if (slot < this.getCurrentPageItems().size()) {
            T object = this.getCurrentPageItems().get(slot);
            if (object != null) {
                return this.convertToItemStack(object);
            }
        }

        if (slot == this.getSize() - 6) {
            return this.prevButton.getItem();
        } else {
            return slot == this.getSize() - 4 ? this.nextButton.getItem() : null;
        }
    }

    public final void onMenuClick(Player player, int slot, InventoryAction action, ClickType click, ItemStack cursor, ItemStack clicked, boolean cancelled) {
        if (slot < this.getCurrentPageItems().size()) {
            T obj = this.getCurrentPageItems().get(slot);
            if (obj != null) {
                InventoryType prevType = player.getOpenInventory().getType();
                this.onPageClick(player, obj, click);
                if (this.updateButtonOnClick() && prevType == player.getOpenInventory().getType()) {
                    player.getOpenInventory().getTopInventory().setItem(slot, this.getItemAt(slot));
                }
            }
        }

    }

    public final void onButtonClick(Player player, int slot, InventoryAction action, ClickType click, Button button) {
        super.onButtonClick(player, slot, action, click, button);
    }

    public final void onMenuClick(Player player, int slot, ItemStack clicked) {
        throw new FoException("Simplest click unsupported");
    }

    private List<T> getCurrentPageItems() {
        Valid.checkBoolean(this.pages.containsKey(this.currentPage - 1), "The menu has only " + this.pages.size() + " pages, not " + this.currentPage + "!", new Object[0]);
        return (List)this.pages.get(this.currentPage - 1);
    }

    public Map<Integer, List<T>> getPages() {
        return this.pages;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }
}