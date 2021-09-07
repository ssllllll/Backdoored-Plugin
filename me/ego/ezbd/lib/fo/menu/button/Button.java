package me.ego.ezbd.lib.fo.menu.button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import me.ego.ezbd.lib.fo.conversation.SimpleDecimalPrompt;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator.ItemCreatorBuilder;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class Button {
    private static CompMaterial infoButtonMaterial;
    private static String infoButtonTitle;

    public Button() {
    }

    public abstract void onClickedInMenu(Player var1, Menu var2, ClickType var3);

    public abstract ItemStack getItem();

    public static final Button.DummyButton makeInfo(String... description) {
        List<String> lores = new ArrayList();
        lores.add(" ");
        String[] var2 = description;
        int var3 = description.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String line = var2[var4];
            lores.add("&7" + line);
        }

        return makeDummy(ItemCreator.of(infoButtonMaterial).name(infoButtonTitle).hideTags(true).lores(lores));
    }

    public static final Button.DummyButton makeEmpty() {
        return makeDummy(ItemCreator.of(CompMaterial.AIR));
    }

    public static final Button.DummyButton makeDummy(ItemCreatorBuilder builder) {
        return makeDummy(builder.build());
    }

    public static final Button.DummyButton makeDummy(ItemCreator creator) {
        return new Button.DummyButton(creator.makeMenuTool());
    }

    public static final Button makeSimple(final CompMaterial icon, final String title, final String label, final Consumer<Player> onClickFunction) {
        return new Button() {
            public ItemStack getItem() {
                return ItemCreator.of(icon, title, new String[]{"", label}).build().makeMenuTool();
            }

            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                onClickFunction.accept(player);
            }
        };
    }

    public static final Button makeSimple(final ItemCreatorBuilder builder, final Consumer<Player> onClickFunction) {
        return new Button() {
            public ItemStack getItem() {
                return builder.build().makeMenuTool();
            }

            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                onClickFunction.accept(player);
            }
        };
    }

    public static final Button makeSimple(final CompMaterial icon, final String title, final String label, final BiConsumer<Player, ClickType> onClickFunction) {
        return new Button() {
            public ItemStack getItem() {
                return ItemCreator.of(icon, title, new String[]{"", label}).build().makeMenuTool();
            }

            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                onClickFunction.accept(player, click);
            }
        };
    }

    public static Button makeDecimalPrompt(final ItemCreatorBuilder builder, final String question, final Consumer<Double> successAction) {
        return new Button() {
            public ItemStack getItem() {
                return builder.build().make();
            }

            public void onClickedInMenu(Player player, Menu menu, ClickType click) {
                SimpleDecimalPrompt.show(player, question, successAction);
            }
        };
    }

    public final String toString() {
        ItemStack item = this.getItem();
        return this.getClass().getSimpleName() + "{" + (item != null ? item.getType() : "null") + "}";
    }

    public static void setInfoButtonMaterial(CompMaterial infoButtonMaterial) {
        Button.infoButtonMaterial = infoButtonMaterial;
    }

    public static void setInfoButtonTitle(String infoButtonTitle) {
        Button.infoButtonTitle = infoButtonTitle;
    }

    static {
        infoButtonMaterial = CompMaterial.NETHER_STAR;
        infoButtonTitle = me.ego.ezbd.lib.fo.settings.SimpleLocalization.Menu.TOOLTIP_INFO;
    }

    public static final class DummyButton extends Button {
        private final ItemStack item;

        public void onClickedInMenu(Player player, Menu menu, ClickType click) {
        }

        private DummyButton(ItemStack item) {
            this.item = item;
        }

        public ItemStack getItem() {
            return this.item;
        }
    }
}
