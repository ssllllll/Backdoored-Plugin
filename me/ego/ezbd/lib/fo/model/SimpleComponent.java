package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.ChatUtil;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.SerializeUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.Remain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SimpleComponent implements ConfigSerializable {
    public static boolean STRIP_OVERSIZED_COMPONENTS = true;
    private static final Pattern URL_PATTERN = Pattern.compile("^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$");
    private final List<SimpleComponent.Part> pastComponents = new ArrayList();
    @Nullable
    private SimpleComponent.Part currentComponent;

    private SimpleComponent(String text) {
        if (Common.stripColors(text).startsWith("<center>")) {
            text = ChatUtil.center(text.replace("<center>", "").trim());
        }

        this.currentComponent = new SimpleComponent.Part(text);
    }

    private SimpleComponent() {
    }

    public SimpleComponent onHover(Collection<String> texts) {
        return this.onHover(Common.toArray(texts));
    }

    public SimpleComponent onHover(String... lines) {
        String joined = Common.colorize(String.join("\n", lines));
        this.currentComponent.hoverEvent = new HoverEvent(Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(TextComponent.fromLegacyText(joined))});
        return this;
    }

    public SimpleComponent onHover(ItemStack item) {
        if (CompMaterial.isAir(item.getType())) {
            return this.onHover("Air");
        } else {
            this.currentComponent.hoverEvent = new HoverEvent(Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(Remain.toJson(item))});
            return this;
        }
    }

    public SimpleComponent viewPermission(String viewPermission) {
        this.currentComponent.viewPermission = viewPermission;
        return this;
    }

    public SimpleComponent viewCondition(String viewCondition) {
        this.currentComponent.viewCondition = viewCondition;
        return this;
    }

    public SimpleComponent onClickRunCmd(String text) {
        return this.onClick(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, text);
    }

    public SimpleComponent onClickSuggestCmd(String text) {
        return this.onClick(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, text);
    }

    public SimpleComponent onClickOpenUrl(String url) {
        return this.onClick(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, url);
    }

    public SimpleComponent onClick(net.md_5.bungee.api.chat.ClickEvent.Action action, String text) {
        this.currentComponent.clickEvent = new ClickEvent(action, text);
        return this;
    }

    public SimpleComponent onClickInsert(String insertion) {
        this.currentComponent.insertion = insertion;
        return this;
    }

    public SimpleComponent appendFirst(SimpleComponent component) {
        this.pastComponents.add(0, component.currentComponent);
        this.pastComponents.addAll(0, component.pastComponents);
        return this;
    }

    public SimpleComponent append(String text) {
        return this.append(text, true);
    }

    public SimpleComponent append(String text, boolean colorize) {
        return this.append(text, (BaseComponent)null, colorize);
    }

    public SimpleComponent append(String text, BaseComponent inheritFormatting) {
        return this.append(text, inheritFormatting, true);
    }

    public SimpleComponent append(String text, BaseComponent inheritFormatting, boolean colorize) {
        BaseComponent inherit = inheritFormatting != null ? inheritFormatting : this.currentComponent.toTextComponent((CommandSender)null);
        if (inherit != null && ((BaseComponent)inherit).getExtra() != null && !((BaseComponent)inherit).getExtra().isEmpty()) {
            inherit = (BaseComponent)((BaseComponent)inherit).getExtra().get(((BaseComponent)inherit).getExtra().size() - 1);
        }

        if (colorize) {
            List<String> formatContents = Arrays.asList(text.split("\n"));

            for(int i = 0; i < formatContents.size(); ++i) {
                String line = (String)formatContents.get(i);
                if (Common.stripColors(line).startsWith("<center>")) {
                    formatContents.set(i, ChatUtil.center(line.replace("<center>", "")));
                }
            }

            text = String.join("\n", formatContents);
        }

        this.pastComponents.add(this.currentComponent);
        this.currentComponent = new SimpleComponent.Part(colorize ? Common.colorize(text) : text);
        this.currentComponent.inheritFormatting = (BaseComponent)inherit;
        return this;
    }

    public SimpleComponent append(SimpleComponent component) {
        this.pastComponents.add(this.currentComponent);
        this.pastComponents.addAll(component.pastComponents);
        BaseComponent inherit = (BaseComponent)Common.getOrDefault(component.currentComponent.inheritFormatting, this.currentComponent.toTextComponent((CommandSender)null));
        if (inherit != null && inherit.getExtra() != null && !inherit.getExtra().isEmpty()) {
            inherit = (BaseComponent)inherit.getExtra().get(inherit.getExtra().size() - 1);
        }

        this.currentComponent = component.currentComponent;
        this.currentComponent.inheritFormatting = inherit;
        return this;
    }

    public String getPlainMessage() {
        return this.build((CommandSender)null).toLegacyText();
    }

    public TextComponent getTextComponent() {
        return this.build((CommandSender)null);
    }

    public TextComponent build(CommandSender receiver) {
        TextComponent preparedComponent = null;
        Iterator var3 = this.pastComponents.iterator();

        while(var3.hasNext()) {
            SimpleComponent.Part part = (SimpleComponent.Part)var3.next();
            TextComponent component = part.toTextComponent(receiver);
            if (component != null) {
                if (preparedComponent == null) {
                    preparedComponent = component;
                } else {
                    preparedComponent.addExtra(component);
                }
            }
        }

        TextComponent currentComponent = this.currentComponent.toTextComponent(receiver);
        if (currentComponent != null) {
            if (preparedComponent == null) {
                preparedComponent = currentComponent;
            } else {
                preparedComponent.addExtra(currentComponent);
            }
        }

        return (TextComponent)Common.getOrDefault(preparedComponent, new TextComponent(""));
    }

    public SimpleComponent replace(String variable, Object value) {
        String serialized = SerializeUtil.serialize(value).toString();
        Iterator var4 = this.pastComponents.iterator();

        while(var4.hasNext()) {
            SimpleComponent.Part part = (SimpleComponent.Part)var4.next();
            Valid.checkNotNull(part.text);
            part.text = part.text.replace(variable, serialized);
        }

        Valid.checkNotNull(this.currentComponent.text);
        this.currentComponent.text = this.currentComponent.text.replace(variable, serialized);
        return this;
    }

    public <T extends CommandSender> void send(T... receivers) {
        this.send((Iterable)Arrays.asList(receivers));
    }

    public <T extends CommandSender> void send(Iterable<T> receivers) {
        this.sendAs((CommandSender)null, receivers);
    }

    public <T extends CommandSender> void sendAs(@Nullable CommandSender sender, Iterable<T> receivers) {
        Iterator var3 = receivers.iterator();

        while(true) {
            while(var3.hasNext()) {
                CommandSender receiver = (CommandSender)var3.next();
                TextComponent component = this.build(receiver);
                if (receiver instanceof Player && sender instanceof Player) {
                    this.setRelationPlaceholders(component, (Player)receiver, (Player)sender);
                }

                if (STRIP_OVERSIZED_COMPONENTS && Remain.toJson(new BaseComponent[]{component}).length() + 1 >= 32767) {
                    String legacy = Common.colorize(component.toLegacyText());
                    if (legacy.length() + 1 >= 32767) {
                        Common.log(new String[]{"Warning: JSON Message to " + receiver.getName() + " was too large and could not be sent: '" + legacy + "'"});
                    } else {
                        Common.log(new String[]{"Warning: JSON Message to " + receiver.getName() + " was too large, removing interactive elements to avoid kick. Sending plain: '" + legacy + "'"});
                        receiver.sendMessage(legacy);
                    }
                } else {
                    Remain.sendComponent(receiver, component);
                }
            }

            return;
        }
    }

    private void setRelationPlaceholders(TextComponent component, Player receiver, Player sender) {
        component.setText(HookManager.replaceRelationPlaceholders(sender, receiver, component.getText()));
        if (component.getExtra() != null) {
            Iterator var4 = component.getExtra().iterator();

            while(true) {
                BaseComponent extra;
                do {
                    if (!var4.hasNext()) {
                        return;
                    }

                    extra = (BaseComponent)var4.next();
                } while(!(extra instanceof TextComponent));

                TextComponent text = (TextComponent)extra;
                ClickEvent clickEvent = text.getClickEvent();
                HoverEvent hoverEvent = text.getHoverEvent();
                if (clickEvent != null) {
                    text.setClickEvent(new ClickEvent(clickEvent.getAction(), HookManager.replaceRelationPlaceholders(sender, receiver, clickEvent.getValue())));
                }

                if (hoverEvent != null) {
                    BaseComponent[] var9 = hoverEvent.getValue();
                    int var10 = var9.length;

                    for(int var11 = 0; var11 < var10; ++var11) {
                        BaseComponent hoverBaseComponent = var9[var11];
                        if (hoverBaseComponent instanceof TextComponent) {
                            TextComponent hoverTextComponent = (TextComponent)hoverBaseComponent;
                            hoverTextComponent.setText(HookManager.replaceRelationPlaceholders(sender, receiver, hoverTextComponent.getText()));
                        }
                    }
                }

                this.setRelationPlaceholders(text, receiver, sender);
            }
        }
    }

    public String toString() {
        return this.serialize().toStringFormatted();
    }

    public SerializedMap serialize() {
        SerializedMap map = new SerializedMap();
        map.putIf("Current_Component", this.currentComponent);
        map.put("Past_Components", this.pastComponents);
        return map;
    }

    public static SimpleComponent deserialize(SerializedMap map) {
        SimpleComponent component = new SimpleComponent();
        component.currentComponent = (SimpleComponent.Part)map.get("Current_Component", SimpleComponent.Part.class);
        component.pastComponents.addAll(map.getList("Past_Components", SimpleComponent.Part.class));
        return component;
    }

    private static TextComponent[] toComponent(@NonNull String message, @Nullable BaseComponent inheritFormatting) {
        if (message == null) {
            throw new NullPointerException("message is marked non-null but is null");
        } else {
            List<TextComponent> components = new ArrayList();
            if (inheritFormatting != null) {
                if (inheritFormatting.isBold()) {
                    message = ChatColor.BOLD + message;
                }

                if (inheritFormatting.isItalic()) {
                    message = ChatColor.ITALIC + message;
                }

                if (inheritFormatting.isObfuscated()) {
                    message = ChatColor.MAGIC + message;
                }

                if (inheritFormatting.isStrikethrough()) {
                    message = ChatColor.STRIKETHROUGH + message;
                }

                if (inheritFormatting.isUnderlined()) {
                    message = ChatColor.UNDERLINE + message;
                }

                message = inheritFormatting.getColor() + message;
            }

            StringBuilder builder = new StringBuilder();
            TextComponent component = new TextComponent();

            for(int index = 0; index < message.length(); ++index) {
                char letter = message.charAt(index);
                TextComponent old;
                if (letter == 167) {
                    ++index;
                    if (index >= message.length()) {
                        break;
                    }

                    letter = message.charAt(index);
                    if (letter >= 'A' && letter <= 'Z') {
                        letter = (char)(letter + 32);
                    }

                    ChatColor format;
                    if (letter == 'x' && index + 12 < message.length()) {
                        StringBuilder hex = new StringBuilder("#");

                        for(int j = 0; j < 6; ++j) {
                            hex.append(message.charAt(index + 2 + j * 2));
                        }

                        try {
                            format = ChatColor.of(hex.toString());
                        } catch (IllegalArgumentException | NoSuchMethodError var10) {
                            format = null;
                        }

                        index += 12;
                    } else {
                        format = ChatColor.getByChar(letter);
                    }

                    if (format != null) {
                        if (builder.length() > 0) {
                            old = component;
                            component = new TextComponent(component);
                            old.setText(builder.toString());
                            builder = new StringBuilder();
                            components.add(old);
                        }

                        if (format == ChatColor.BOLD) {
                            component.setBold(true);
                        } else if (format == ChatColor.ITALIC) {
                            component.setItalic(true);
                        } else if (format == ChatColor.UNDERLINE) {
                            component.setUnderlined(true);
                        } else if (format == ChatColor.STRIKETHROUGH) {
                            component.setStrikethrough(true);
                        } else if (format == ChatColor.MAGIC) {
                            component.setObfuscated(true);
                        } else if (format == ChatColor.RESET) {
                            format = ChatColor.WHITE;
                            component = new TextComponent();
                            component.setColor(format);
                        } else {
                            component = new TextComponent();
                            component.setColor(format);
                        }
                    }
                } else {
                    int pos = message.indexOf(32, index);
                    if (pos == -1) {
                        pos = message.length();
                    }

                    if (URL_PATTERN.matcher(message).region(index, pos).find()) {
                        if (builder.length() > 0) {
                            old = component;
                            component = new TextComponent(component);
                            old.setText(builder.toString());
                            builder = new StringBuilder();
                            components.add(old);
                        }

                        old = component;
                        component = new TextComponent(component);
                        String urlString = message.substring(index, pos);
                        component.setText(urlString);
                        component.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, urlString.startsWith("http") ? urlString : "http://" + urlString));
                        components.add(component);
                        index += pos - index - 1;
                        component = old;
                    } else {
                        builder.append(letter);
                    }
                }
            }

            component.setText(builder.toString());
            components.add(component);
            return new TextComponent[]{new TextComponent((BaseComponent[])components.toArray(new TextComponent[components.size()]))};
        }
    }

    public static SimpleComponent empty() {
        return of(true, "");
    }

    public static SimpleComponent of(String text) {
        return of(true, text);
    }

    public static SimpleComponent of(boolean colorize, String text) {
        return new SimpleComponent(colorize ? Common.colorize(text) : text);
    }

    static final class Part implements ConfigSerializable {
        private String text;
        @Nullable
        private String viewPermission;
        @Nullable
        private String viewCondition;
        @Nullable
        private HoverEvent hoverEvent;
        @Nullable
        private ClickEvent clickEvent;
        @Nullable
        private String insertion;
        @Nullable
        private BaseComponent inheritFormatting;

        private Part(String text) {
            Valid.checkNotNull(text, "Part text cannot be null");
            this.text = text;
        }

        public SerializedMap serialize() {
            SerializedMap map = new SerializedMap();
            map.put("Text", this.text);
            map.putIf("View_Permission", this.viewPermission);
            map.putIf("View_Condition", this.viewCondition);
            map.putIf("Hover_Event", this.hoverEvent);
            map.putIf("Click_Event", this.clickEvent);
            map.putIf("Insertion", this.insertion);
            map.putIf("Inherit_Formatting", this.inheritFormatting);
            return map;
        }

        public static SimpleComponent.Part deserialize(SerializedMap map) {
            SimpleComponent.Part part = new SimpleComponent.Part(map.getString("Text"));
            part.viewPermission = map.getString("View_Permission");
            part.viewCondition = map.getString("View_Condition");
            part.hoverEvent = (HoverEvent)map.get("Hover_Event", HoverEvent.class);
            part.clickEvent = (ClickEvent)map.get("Click_Event", ClickEvent.class);
            part.insertion = map.getString("Insertion");
            part.inheritFormatting = (BaseComponent)map.get("Inherit_Formatting", BaseComponent.class);
            return part;
        }

        @Nullable
        private TextComponent toTextComponent(CommandSender receiver) {
            if (this.canSendTo(receiver) && !this.isEmpty()) {
                List<BaseComponent> base = SimpleComponent.toComponent(this.text, this.inheritFormatting)[0].getExtra();
                Iterator var3 = base.iterator();

                while(var3.hasNext()) {
                    BaseComponent part = (BaseComponent)var3.next();
                    if (this.hoverEvent != null) {
                        part.setHoverEvent(this.hoverEvent);
                    }

                    if (this.clickEvent != null) {
                        part.setClickEvent(this.clickEvent);
                    }

                    if (this.insertion != null) {
                        part.setInsertion(this.insertion);
                    }
                }

                return new TextComponent((BaseComponent[])base.toArray(new BaseComponent[base.size()]));
            } else {
                return null;
            }
        }

        private boolean isEmpty() {
            return this.text.isEmpty() && this.hoverEvent == null && this.clickEvent == null && this.insertion == null;
        }

        private boolean canSendTo(@Nullable CommandSender receiver) {
            if (this.viewPermission == null || this.viewPermission.isEmpty() || receiver != null && PlayerUtil.hasPerm(receiver, this.viewPermission)) {
                if (this.viewCondition != null && !this.viewCondition.isEmpty()) {
                    if (receiver == null) {
                        return false;
                    }

                    Object result = JavaScriptExecutor.run(Variables.replace(this.viewCondition, receiver), receiver);
                    if (result != null) {
                        Valid.checkBoolean(result instanceof Boolean, "View condition must return Boolean not " + (result == null ? "null" : result.getClass()) + " for component: " + this, new Object[0]);
                        if (!(Boolean)result) {
                            return false;
                        }
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        public String toString() {
            return this.serialize().toStringFormatted();
        }
    }
}