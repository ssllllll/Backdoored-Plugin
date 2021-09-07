package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.ego.ezbd.lib.fo.ChatUtil;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Commands;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public final class ChatPaginator {
    public static final int FOUNDATION_HEIGHT = 15;
    private final int linesPerPage;
    private final ChatColor themeColor;
    private final List<SimpleComponent> header;
    private final Map<Integer, List<SimpleComponent>> pages;
    private final List<SimpleComponent> footer;

    public ChatPaginator() {
        this(15, Commands.HEADER_COLOR);
    }

    public ChatPaginator(ChatColor themeColor) {
        this(15, themeColor);
    }

    public ChatPaginator(int linesPerPage) {
        this(linesPerPage, Commands.HEADER_COLOR);
    }

    public ChatPaginator setFoundationHeader(String title) {
        return this.setHeader("&r", this.themeColor + "&m" + ChatUtil.center("&r" + this.themeColor + " " + title + " &m", '-', 150), "&r");
    }

    public ChatPaginator setHeader(SimpleComponent... components) {
        SimpleComponent[] var2 = components;
        int var3 = components.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            SimpleComponent component = var2[var4];
            this.header.add(component);
        }

        return this;
    }

    public ChatPaginator setHeader(String... messages) {
        String[] var2 = messages;
        int var3 = messages.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String message = var2[var4];
            this.header.add(SimpleComponent.of(message));
        }

        return this;
    }

    public ChatPaginator setPages(SimpleComponent... components) {
        this.pages.clear();
        this.pages.putAll(Common.fillPages(this.linesPerPage, Arrays.asList(components)));
        return this;
    }

    public ChatPaginator setPages(String... messages) {
        List<SimpleComponent> pages = new ArrayList();
        String[] var3 = messages;
        int var4 = messages.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String message = var3[var5];
            pages.add(SimpleComponent.of(message));
        }

        return this.setPages((Collection)pages);
    }

    public ChatPaginator setPages(Collection<SimpleComponent> components) {
        this.pages.clear();
        this.pages.putAll(Common.fillPages(this.linesPerPage, components));
        return this;
    }

    public ChatPaginator setFooter(SimpleComponent... components) {
        SimpleComponent[] var2 = components;
        int var3 = components.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            SimpleComponent component = var2[var4];
            this.footer.add(component);
        }

        return this;
    }

    public ChatPaginator setFooter(String... messages) {
        String[] var2 = messages;
        int var3 = messages.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String message = var2[var4];
            this.footer.add(SimpleComponent.of(message));
        }

        return this;
    }

    public void send(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (player.hasMetadata("FoPages")) {
                Plugin owningPlugin = ((MetadataValue)player.getMetadata("FoPages").get(0)).getOwningPlugin();
                player.removeMetadata("FoPages", owningPlugin);
            }

            player.setMetadata("FoPages", new FixedMetadataValue(SimplePlugin.getInstance(), SimplePlugin.getNamed()));
            player.setMetadata(getPageNbtTag(), new FixedMetadataValue(SimplePlugin.getInstance(), this));
            player.chat("/#flp 1");
        } else {
            Iterator var7 = this.header.iterator();

            while(var7.hasNext()) {
                SimpleComponent component = (SimpleComponent)var7.next();
                component.send(new CommandSender[]{sender});
            }

            int amount = 1;
            Iterator var10 = this.pages.values().iterator();

            while(var10.hasNext()) {
                List<SimpleComponent> components = (List)var10.next();
                Iterator var5 = components.iterator();

                while(var5.hasNext()) {
                    SimpleComponent component = (SimpleComponent)var5.next();
                    component.replace("{count}", amount++).send(new CommandSender[]{sender});
                }
            }

            var10 = this.footer.iterator();

            while(var10.hasNext()) {
                SimpleComponent component = (SimpleComponent)var10.next();
                component.send(new CommandSender[]{sender});
            }
        }

    }

    public static String getPageNbtTag() {
        return "FoPages_" + SimplePlugin.getNamed();
    }

    public int getLinesPerPage() {
        return this.linesPerPage;
    }

    public ChatColor getThemeColor() {
        return this.themeColor;
    }

    public List<SimpleComponent> getHeader() {
        return this.header;
    }

    public Map<Integer, List<SimpleComponent>> getPages() {
        return this.pages;
    }

    public List<SimpleComponent> getFooter() {
        return this.footer;
    }

    public ChatPaginator(int linesPerPage, ChatColor themeColor) {
        this.header = new ArrayList();
        this.pages = new HashMap();
        this.footer = new ArrayList();
        this.linesPerPage = linesPerPage;
        this.themeColor = themeColor;
    }
}