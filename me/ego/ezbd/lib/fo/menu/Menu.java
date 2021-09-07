package me.ego.ezbd.lib.fo.menu;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.ItemUtil;
import me.ego.ezbd.lib.fo.Messenger;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.constants.FoConstants.NBT;
import me.ego.ezbd.lib.fo.event.MenuOpenEvent;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.menu.button.Button;
import me.ego.ezbd.lib.fo.menu.button.ButtonReturnBack;
import me.ego.ezbd.lib.fo.menu.button.Button.DummyButton;
import me.ego.ezbd.lib.fo.menu.model.InventoryDrawer;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.menu.model.MenuClickLocation;
import me.ego.ezbd.lib.fo.model.SimpleSound;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.CompSound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public abstract class Menu {
    private static SimpleSound sound;
    private static boolean titleAnimationEnabled;
    private static int titleAnimationDurationTicks;
    protected static final ItemStack NO_ITEM;
    private final List<Button> registeredButtons;
    private boolean buttonsRegistered;
    private final Menu parent;
    private final Button returnButton;
    private String title;
    private Integer size;
    private String[] info;
    private Player viewer;
    private boolean slotNumbersVisible;

    protected Menu() {
        this((Menu)null);
    }

    protected Menu(Menu parent) {
        this(parent, false);
    }

    protected Menu(Menu parent, boolean returnMakesNewInstance) {
        this.registeredButtons = new ArrayList();
        this.buttonsRegistered = false;
        this.title = "&0Menu";
        this.size = 27;
        this.info = null;
        this.parent = parent;
        this.returnButton = (Button)(parent != null ? new ButtonReturnBack(parent, returnMakesNewInstance) : Button.makeEmpty());
    }

    public static final Menu getMenu(Player player) {
        return getMenu0(player, NBT.TAG_MENU_CURRENT);
    }

    public static final Menu getPreviousMenu(Player player) {
        return getMenu0(player, NBT.TAG_MENU_PREVIOUS);
    }

    private static Menu getMenu0(Player player, String tag) {
        if (player.hasMetadata(tag)) {
            Menu menu = (Menu)((MetadataValue)player.getMetadata(tag).get(0)).value();
            Valid.checkNotNull(menu, "Menu missing from " + player.getName() + "'s metadata '" + tag + "' tag!");
            return menu;
        } else {
            return null;
        }
    }

    protected final void registerButtons() {
        this.registeredButtons.clear();
        List<Button> buttons = this.getButtonsToAutoRegister();
        if (buttons != null) {
            this.registeredButtons.addAll(buttons);
        }

        Class lookup = this.getClass();

        do {
            Field[] var2 = lookup.getDeclaredFields();
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Field f = var2[var4];
                this.registerButton0(f);
            }
        } while(Menu.class.isAssignableFrom(lookup = lookup.getSuperclass()));

    }

    private void registerButton0(Field field) {
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (Button.class.isAssignableFrom(type)) {
            Button button = (Button)ReflectionUtil.getFieldContent(field, this);
            Valid.checkNotNull(button, "Null button field named " + field.getName() + " in " + this);
            this.registeredButtons.add(button);
        } else if (Button[].class.isAssignableFrom(type)) {
            Valid.checkBoolean(Modifier.isFinal(field.getModifiers()), "Report / Button[] field must be final: " + field, new Object[0]);
            Button[] buttons = (Button[])((Button[])ReflectionUtil.getFieldContent(field, this));
            Valid.checkBoolean(buttons != null && buttons.length > 0, "Null " + field.getName() + "[] in " + this, new Object[0]);
            this.registeredButtons.addAll(Arrays.asList(buttons));
        }

    }

    private final void registerButtonsIfHasnt() {
        if (!this.buttonsRegistered) {
            this.registerButtons();
            this.buttonsRegistered = true;
        }

    }

    protected List<Button> getButtonsToAutoRegister() {
        return null;
    }

    protected final Button getButton(ItemStack fromItem) {
        this.registerButtonsIfHasnt();
        if (fromItem != null) {
            Iterator var2 = this.registeredButtons.iterator();

            while(var2.hasNext()) {
                Button button = (Button)var2.next();
                Valid.checkNotNull(button, "Menu button is null at " + this.getClass().getSimpleName());
                Valid.checkNotNull(button.getItem(), "Menu " + this.getTitle() + " contained button " + button + " with empty item!");
                if (ItemUtil.isSimilar(fromItem, button.getItem())) {
                    return button;
                }
            }
        }

        return null;
    }

    public Menu newInstance() {
        try {
            return (Menu)ReflectionUtil.instantiate(this.getClass());
        } catch (Throwable var4) {
            try {
                Object parent = this.getClass().getMethod("getParent").invoke(this.getClass());
                if (parent != null) {
                    return (Menu)ReflectionUtil.instantiate(this.getClass(), new Object[]{parent});
                }
            } catch (Throwable var3) {
            }

            var4.printStackTrace();
            throw new FoException("Could not instantiate menu of " + this.getClass() + ", override the method 'newInstance()' or ensure you have a public constructor which takes only one parameter ");
        }
    }

    public final void displayTo(Player player) {
        Valid.checkNotNull(this.size, "Size not set in " + this + " (call setSize in your constructor)");
        Valid.checkNotNull(this.title, "Title not set in " + this + " (call setTitle in your constructor)");
        this.viewer = player;
        this.registerButtonsIfHasnt();
        InventoryDrawer drawer = InventoryDrawer.of(this.size, this.title);
        this.compileBottomBar0().forEach((slot, itemx) -> {
            drawer.setItem(slot, itemx);
        });

        for(int i = 0; i < drawer.getSize(); ++i) {
            ItemStack item = this.getItemAt(i);
            if (item != null && !drawer.isSet(i)) {
                drawer.setItem(i, item);
            }
        }

        this.onDisplay(drawer);
        this.debugSlotNumbers(drawer);
        if (Common.callEvent(new MenuOpenEvent(this, drawer, player))) {
            if (player.isConversing()) {
                player.sendRawMessage(Common.colorize(me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.CANNOT_OPEN_DURING_CONVERSATION));
            } else {
                sound.play(player);
                Menu previous = getMenu(player);
                if (previous != null) {
                    player.setMetadata(NBT.TAG_MENU_PREVIOUS, new FixedMetadataValue(SimplePlugin.getInstance(), previous));
                }

                Common.runLater(1, () -> {
                    drawer.display(player);
                    player.setMetadata(NBT.TAG_MENU_CURRENT, new FixedMetadataValue(SimplePlugin.getInstance(), this));
                });
            }
        }
    }

    private void debugSlotNumbers(InventoryDrawer drawer) {
        if (this.slotNumbersVisible) {
            for(int slot = 0; slot < drawer.getSize(); ++slot) {
                ItemStack item = drawer.getItem(slot);
                if (item == null) {
                    drawer.setItem(slot, ItemCreator.of(CompMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, "Slot " + slot, new String[0]).build().make());
                }
            }
        }

    }

    protected void onDisplay(InventoryDrawer drawer) {
    }

    public final void restartMenu() {
        this.restartMenu((String)null);
    }

    public final void restartMenu(String animatedTitle) {
        this.registerButtons();
        this.redraw();
        if (animatedTitle != null) {
            this.animateTitle(animatedTitle);
        }

    }

    protected final void redraw() {
        Inventory inv = this.getViewer().getOpenInventory().getTopInventory();
        Valid.checkBoolean(inv.getType() == InventoryType.CHEST, this.getViewer().getName() + "'s inventory closed in the meanwhile (now == " + inv.getType() + ").", new Object[0]);

        for(int i = 0; i < this.size; ++i) {
            ItemStack item = this.getItemAt(i);
            Valid.checkBoolean(i < inv.getSize(), "Item (" + (item != null ? item.getType() : "null") + ") position (" + i + ") > inv size (" + inv.getSize() + ")", new Object[0]);
            inv.setItem(i, item);
        }

        this.compileBottomBar0().forEach((slot, itemx) -> {
            inv.setItem(slot, itemx);
        });
        this.getViewer().updateInventory();
    }

    private Map<Integer, ItemStack> compileBottomBar0() {
        Map<Integer, ItemStack> items = new HashMap();
        if (this.addInfoButton() && this.getInfo() != null) {
            items.put(this.getInfoButtonPosition(), Button.makeInfo(this.getInfo()).getItem());
        }

        if (this.addReturnButton() && !(this.returnButton instanceof DummyButton)) {
            items.put(this.getReturnButtonPosition(), this.returnButton.getItem());
        }

        return items;
    }

    public final void animateTitle(String title) {
        if (titleAnimationEnabled) {
            PlayerUtil.updateInventoryTitle(this, this.getViewer(), title, this.getTitle(), titleAnimationDurationTicks);
        }

    }

    public void tell(String... messages) {
        Common.tell(this.viewer, messages);
    }

    public void tellInfo(String message) {
        Messenger.info(this.viewer, message);
    }

    public void tellSuccess(String message) {
        Messenger.success(this.viewer, message);
    }

    public void tellWarn(String message) {
        Messenger.warn(this.viewer, message);
    }

    public void tellError(String message) {
        Messenger.error(this.viewer, message);
    }

    public void tellQuestion(String message) {
        Messenger.question(this.viewer, message);
    }

    public void tellAnnounce(String message) {
        Messenger.announce(this.viewer, message);
    }

    public ItemStack getItemAt(int slot) {
        return null;
    }

    protected int getInfoButtonPosition() {
        return this.size - 9;
    }

    protected boolean addReturnButton() {
        return true;
    }

    protected boolean addInfoButton() {
        return true;
    }

    protected int getReturnButtonPosition() {
        return this.size - 1;
    }

    protected final int getCenterSlot() {
        int pos = this.size / 2;
        return this.size % 2 == 1 ? pos : pos - 5;
    }

    /** @deprecated */
    @Deprecated
    protected boolean isActionAllowed(MenuClickLocation location, int slot, ItemStack clicked, ItemStack cursor) {
        return false;
    }

    public final String getTitle() {
        return this.title;
    }

    protected final void setTitle(String title) {
        this.title = title;
    }

    public final Menu getParent() {
        return this.parent;
    }

    public final Integer getSize() {
        return this.size;
    }

    protected final void setSize(Integer size) {
        this.size = size;
    }

    protected final void setInfo(String... info) {
        this.info = info;
    }

    protected final Player getViewer() {
        return this.viewer;
    }

    protected final void setViewer(Player viewer) {
        this.viewer = viewer;
    }

    protected final Inventory getInventory() {
        Valid.checkNotNull(this.viewer, "Cannot get inventory when there is no viewer!");
        Inventory topInventory = this.viewer.getOpenInventory().getTopInventory();
        Valid.checkNotNull(topInventory, "Top inventory is null!");
        return topInventory;
    }

    protected final ItemStack[] getContent(int from, int to) {
        ItemStack[] content = this.getInventory().getContents();
        ItemStack[] copy = new ItemStack[content.length];

        for(int i = from; i < copy.length; ++i) {
            ItemStack item = content[i];
            copy[i] = item != null ? item.clone() : null;
        }

        return (ItemStack[])Arrays.copyOfRange(copy, from, to);
    }

    protected final void setSlotNumbersVisible() {
        this.slotNumbersVisible = true;
    }

    protected void onMenuClick(Player player, int slot, InventoryAction action, ClickType click, ItemStack cursor, ItemStack clicked, boolean cancelled) {
        this.onMenuClick(player, slot, clicked);
    }

    protected void onMenuClick(Player player, int slot, ItemStack clicked) {
    }

    protected void onButtonClick(Player player, int slot, InventoryAction action, ClickType click, Button button) {
        button.onClickedInMenu(player, this, click);
    }

    protected void onMenuClose(Player player, Inventory inventory) {
    }

    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }

    public static SimpleSound getSound() {
        return sound;
    }

    public static void setSound(SimpleSound sound) {
        Menu.sound = sound;
    }

    public static boolean isTitleAnimationEnabled() {
        return titleAnimationEnabled;
    }

    public static void setTitleAnimationEnabled(boolean titleAnimationEnabled) {
        Menu.titleAnimationEnabled = titleAnimationEnabled;
    }

    public static void setTitleAnimationDurationTicks(int titleAnimationDurationTicks) {
        Menu.titleAnimationDurationTicks = titleAnimationDurationTicks;
    }

    protected String[] getInfo() {
        return this.info;
    }

    static {
        sound = new SimpleSound(CompSound.NOTE_STICKS.getSound(), 0.4F);
        titleAnimationEnabled = true;
        titleAnimationDurationTicks = 20;
        NO_ITEM = null;
    }
}