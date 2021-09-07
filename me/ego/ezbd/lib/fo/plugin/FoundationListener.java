package me.ego.ezbd.lib.fo.plugin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MathUtil;
import me.ego.ezbd.lib.fo.Messenger;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.constants.FoPermissions;
import me.ego.ezbd.lib.fo.model.ChatPaginator;
import me.ego.ezbd.lib.fo.model.HookManager;
import me.ego.ezbd.lib.fo.model.SimpleComponent;
import me.ego.ezbd.lib.fo.model.SimpleScoreboard;
import me.ego.ezbd.lib.fo.model.SpigotUpdater;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Pages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.metadata.MetadataValue;

final class FoundationListener implements Listener {
    FoundationListener() {
    }

    @EventHandler(
        priority = EventPriority.LOW
    )
    public void onJoin(PlayerJoinEvent event) {
        SpigotUpdater check = SimplePlugin.getInstance().getUpdateCheck();
        if (check != null && check.isNewVersionAvailable() && PlayerUtil.hasPerm(event.getPlayer(), FoPermissions.NOTIFY_UPDATE)) {
            Common.tellLater(80, event.getPlayer(), new String[]{check.getNotifyMessage()});
        }

    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onQuit(PlayerQuitEvent event) {
        SimpleScoreboard.clearBoardsFor(event.getPlayer());
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    public void onServiceRegister(ServiceRegisterEvent event) {
        HookManager.updateVaultIntegration();
    }

    @EventHandler(
        priority = EventPriority.LOWEST
    )
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String[] args = message.split(" ");
        if (message.startsWith("/#flp")) {
            if (args.length != 2) {
                Common.tell(player, new String[]{Pages.NO_PAGE_NUMBER});
                event.setCancelled(true);
            } else {
                String nbtPageTag = ChatPaginator.getPageNbtTag();
                if (!player.hasMetadata(nbtPageTag)) {
                    event.setCancelled(true);
                } else if (!player.hasMetadata("FoPages") || ((MetadataValue)player.getMetadata("FoPages").get(0)).asString().equals(SimplePlugin.getNamed())) {
                    String numberRaw = args[1];
                    boolean var7 = true;

                    int page;
                    try {
                        page = Integer.parseInt(numberRaw) - 1;
                    } catch (NumberFormatException var15) {
                        Common.tell(player, new String[]{Pages.INVALID_PAGE.replace("{input}", numberRaw)});
                        event.setCancelled(true);
                        return;
                    }

                    ChatPaginator chatPages = (ChatPaginator)((MetadataValue)player.getMetadata(nbtPageTag).get(0)).value();
                    Map<Integer, List<SimpleComponent>> pages = chatPages.getPages();
                    pages.entrySet().removeIf((entry) -> {
                        return ((List)entry.getValue()).isEmpty();
                    });
                    if (!pages.containsKey(page)) {
                        String playerMessage = Pages.NO_PAGE;
                        if (Messenger.ENABLED) {
                            Messenger.error(player, playerMessage);
                        } else {
                            Common.tell(player, new String[]{playerMessage});
                        }

                        event.setCancelled(true);
                    } else {
                        Iterator var10 = chatPages.getHeader().iterator();

                        while(var10.hasNext()) {
                            SimpleComponent component = (SimpleComponent)var10.next();
                            component.send(new Player[]{player});
                        }

                        List<SimpleComponent> messagesOnPage = (List)pages.get(page);
                        int multiply = 1;
                        Iterator var12 = messagesOnPage.iterator();

                        while(var12.hasNext()) {
                            SimpleComponent comp = (SimpleComponent)var12.next();
                            comp.replace("{count}", page + multiply++).send(new Player[]{player});
                        }

                        int whiteLines = chatPages.getLinesPerPage();
                        if (whiteLines == 15 && pages.size() == 1) {
                            if (messagesOnPage.size() < 17) {
                                whiteLines = 7;
                            } else {
                                whiteLines += 2;
                            }
                        }

                        for(int i = messagesOnPage.size(); i < whiteLines; ++i) {
                            SimpleComponent.of("&r").send(new Player[]{player});
                        }

                        Iterator var24 = chatPages.getFooter().iterator();

                        while(var24.hasNext()) {
                            SimpleComponent component = (SimpleComponent)var24.next();
                            component.send(new Player[]{player});
                        }

                        if (MinecraftVersion.atLeast(V.v1_7) && pages.size() > 1) {
                            Common.tellNoPrefix(player, new String[]{" "});
                            int pagesDigits = (int)(Math.log10((double)pages.size()) + 1.0D);
                            multiply = 23 - (int)MathUtil.ceiling((double)pagesDigits);
                            SimpleComponent pagination = SimpleComponent.of(chatPages.getThemeColor() + "&m" + Common.duplicate("-", multiply) + "&r");
                            if (page == 0) {
                                pagination.append(" &7« ");
                            } else {
                                pagination.append(" &6« ").onHover(new String[]{Pages.GO_TO_PAGE.replace("{page}", String.valueOf(page))}).onClickRunCmd("/#flp " + page);
                            }

                            pagination.append("&f" + (page + 1)).onHover(new String[]{Pages.GO_TO_FIRST_PAGE}).onClickRunCmd("/#flp 1");
                            pagination.append("/");
                            pagination.append(pages.size() + "").onHover(Pages.TOOLTIP);
                            if (page + 1 >= pages.size()) {
                                pagination.append(" &7» ");
                            } else {
                                pagination.append(" &6» ").onHover(new String[]{Pages.GO_TO_PAGE.replace("{page}", String.valueOf(page + 2))}).onClickRunCmd("/#flp " + (page + 2));
                            }

                            pagination.append(chatPages.getThemeColor() + "&m" + Common.duplicate("-", multiply));
                            pagination.send(new Player[]{player});
                        }

                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}