package me.ego.ezbd.lib.fo.menu.button;

import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.conversation.SimpleConversation;
import me.ego.ezbd.lib.fo.conversation.SimplePrompt;
import me.ego.ezbd.lib.fo.menu.Menu;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator.ItemCreatorBuilder;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class ButtonConversation extends Button {
    private final SimpleConversation conversation;
    private final SimplePrompt prompt;
    private final ItemStack item;

    public ButtonConversation(SimpleConversation convo, CompMaterial material, String title, String... lore) {
        this(convo, ItemCreator.of(material, title, lore));
    }

    public ButtonConversation(SimpleConversation convo, ItemCreatorBuilder item) {
        this(convo, (SimplePrompt)null, item.hideTags(true).build().make());
    }

    public ButtonConversation(SimpleConversation convo, ItemCreator item) {
        this(convo, (SimplePrompt)null, item.make());
    }

    public ButtonConversation(SimplePrompt prompt, CompMaterial material, String title, String... lore) {
        this(prompt, ItemCreator.of(material, title, lore));
    }

    public ButtonConversation(SimplePrompt prompt, ItemCreator item) {
        this((SimpleConversation)null, prompt, item.make());
    }

    public ButtonConversation(SimplePrompt prompt, ItemCreatorBuilder item) {
        this((SimpleConversation)null, prompt, item.hideTags(true).build().make());
    }

    private ButtonConversation(SimpleConversation conversation, SimplePrompt prompt, ItemStack item) {
        this.conversation = conversation;
        this.prompt = prompt;
        this.item = item;
    }

    public void onClickedInMenu(Player player, Menu menu, ClickType click) {
        Valid.checkBoolean(this.conversation != null || this.prompt != null, "Conversation and prompt cannot be null!", new Object[0]);
        if (this.conversation != null) {
            this.conversation.setMenuToReturnTo(menu);
            this.conversation.start(player);
        } else {
            this.prompt.show(player);
        }

    }

    public ItemStack getItem() {
        return this.item;
    }
}
