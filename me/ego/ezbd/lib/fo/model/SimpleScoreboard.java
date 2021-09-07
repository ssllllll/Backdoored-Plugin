package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.RandomUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.remain.CompRunnable;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class SimpleScoreboard {
    private static final List<SimpleScoreboard> registeredBoards = new ArrayList();
    private final List<String> rows = new ArrayList();
    private final StrictList<SimpleScoreboard.ViewedScoreboard> scoreboards = new StrictList();
    private final String[] theme = new String[2];
    private String title;
    private int updateDelayTicks;
    private BukkitTask updateTask;

    public static final void clearBoards() {
        registeredBoards.clear();
    }

    public static final void clearBoardsFor(Player player) {
        Iterator var1 = registeredBoards.iterator();

        while(var1.hasNext()) {
            SimpleScoreboard scoreboard = (SimpleScoreboard)var1.next();
            if (scoreboard.isViewing(player)) {
                scoreboard.hide(player);
            }
        }

    }

    public SimpleScoreboard() {
        registeredBoards.add(this);
    }

    public final void addRows(String... entries) {
        this.addRows(Arrays.asList(entries));
    }

    public final void addRows(List<String> entries) {
        this.rows.addAll(entries);
    }

    public final void clearRows() {
        this.rows.clear();
    }

    public final void removeRow(int index) {
        this.rows.remove(index);
    }

    public final void removeRow(String thatContains) {
        Iterator it = this.rows.iterator();

        while(it.hasNext()) {
            String row = (String)it.next();
            if (row.contains(thatContains)) {
                it.remove();
            }
        }

    }

    private final void start() {
        Valid.checkBoolean(this.updateTask == null, "Scoreboard " + this + " already running", new Object[0]);
        this.updateTask = (new CompRunnable() {
            public void run() {
                try {
                    SimpleScoreboard.this.update();
                } catch (Throwable var3) {
                    String lines = String.join(" ", SimpleScoreboard.this.rows);
                    Common.error(var3, new String[]{"Error displaying " + SimpleScoreboard.this, "Entries: " + lines, "%error", "Stopping rendering for safety."});
                    SimpleScoreboard.this.stop();
                }

            }
        }).runTaskTimer(SimplePlugin.getInstance(), 0L, (long)this.updateDelayTicks);
    }

    private final void update() {
        this.onUpdate();
        Iterator var1 = this.scoreboards.iterator();

        while(var1.hasNext()) {
            SimpleScoreboard.ViewedScoreboard viewedScoreboard = (SimpleScoreboard.ViewedScoreboard)var1.next();
            this.resetObjective(viewedScoreboard);
            this.reloadEntries(viewedScoreboard);
        }

    }

    private final void resetObjective(SimpleScoreboard.ViewedScoreboard viewedScoreboard) {
        Scoreboard scoreboard = viewedScoreboard.getScoreboard();
        Objective objective = viewedScoreboard.getObjective();
        if (objective != null) {
            objective.unregister();
        }

        objective = scoreboard.registerNewObjective(viewedScoreboard.getViewer().getName(), "dummy");
        objective.setDisplayName(Common.colorize(this.title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        viewedScoreboard.setObjective(objective);
    }

    private final void reloadEntries(SimpleScoreboard.ViewedScoreboard viewedScoreboard) {
        Objective objective = viewedScoreboard.getObjective();
        StrictList<String> duplicates = new StrictList();

        for(int i = this.rows.size(); i > 0; --i) {
            String sidebarEntry = (String)this.rows.get(this.rows.size() - i);
            String entry = this.replaceVariables(viewedScoreboard.getViewer(), this.replaceTheme(sidebarEntry));
            String line = this.fixDuplicates(duplicates, entry);
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }

            Remain.getScore(objective, line).setScore(i);
        }

    }

    private final String replaceTheme(String row) {
        if (this.theme != null && row.contains(":")) {
            if (this.theme.length == 1) {
                return this.theme[0] + row;
            }

            if (this.theme[0] != null) {
                String[] split = row.split("\\:");
                if (split.length > 1) {
                    return this.theme[0] + split[0] + ":" + this.theme[1] + split[1];
                }
            }
        }

        return row;
    }

    public final void setTheme(@NonNull ChatColor primary, @Nullable ChatColor secondary) {
        if (primary == null) {
            throw new NullPointerException("primary is marked non-null but is null");
        } else {
            if (secondary != null) {
                this.theme[0] = "&" + primary.getChar();
                this.theme[1] = "&" + secondary.getChar();
            } else {
                this.theme[0] = "&" + primary.getChar();
            }

        }
    }

    private final String fixDuplicates(StrictList<String> duplicates, String message) {
        message = StringUtils.substring(message, 0, 40);
        boolean cut = MinecraftVersion.olderThan(V.v1_8);
        if (cut && message.length() > 16) {
            message = message.substring(0, 16);
        }

        if (duplicates.contains(message)) {
            for(int i = 0; i < duplicates.size() && message.length() < 40; ++i) {
                message = message + RandomUtil.nextColorOrDecoration();
            }
        }

        if (cut && message.length() > 16) {
            message = message.substring(0, 16);
        }

        duplicates.add(message);
        return message;
    }

    protected String replaceVariables(Player player, String message) {
        return message;
    }

    protected void onUpdate() {
    }

    public final void stop() {
        Iterator iterator = this.scoreboards.iterator();

        while(iterator.hasNext()) {
            SimpleScoreboard.ViewedScoreboard score = (SimpleScoreboard.ViewedScoreboard)iterator.next();
            score.getViewer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            iterator.remove();
        }

        if (this.updateTask != null) {
            this.cancelUpdateTask();
        }

    }

    private final void cancelUpdateTask() {
        Valid.checkNotNull(this.updateTask, "Scoreboard " + this + " not running");
        this.updateTask.cancel();
        this.updateTask = null;
    }

    public final boolean isRunning() {
        return this.updateTask != null;
    }

    public final void show(Player player) {
        Valid.checkBoolean(!this.isViewing(player), "Player " + player.getName() + " is already viewing scoreboard: " + this.getTitle(), new Object[0]);
        if (this.updateTask == null) {
            this.start();
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.scoreboards.add(new SimpleScoreboard.ViewedScoreboard(scoreboard, (Objective)null, player));
        player.setScoreboard(scoreboard);
    }

    public final void hide(Player player) {
        Valid.checkBoolean(this.isViewing(player), "Player " + player.getName() + " is not viewing scoreboard: " + this.getTitle(), new Object[0]);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        Iterator var2 = this.scoreboards.iterator();

        while(var2.hasNext()) {
            SimpleScoreboard.ViewedScoreboard viewed = (SimpleScoreboard.ViewedScoreboard)var2.next();
            if (viewed.getViewer().equals(player)) {
                this.scoreboards.remove(viewed);
                break;
            }
        }

        if (this.scoreboards.isEmpty()) {
            this.cancelUpdateTask();
        }

    }

    public final boolean isViewing(Player player) {
        Iterator var2 = this.scoreboards.iterator();

        SimpleScoreboard.ViewedScoreboard viewed;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            viewed = (SimpleScoreboard.ViewedScoreboard)var2.next();
        } while(!viewed.getViewer().equals(player));

        return true;
    }

    public final String toString() {
        return "Scoreboard{title=" + this.getTitle() + "}";
    }

    public static List<SimpleScoreboard> getRegisteredBoards() {
        return registeredBoards;
    }

    public List<String> getRows() {
        return this.rows;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUpdateDelayTicks() {
        return this.updateDelayTicks;
    }

    public void setUpdateDelayTicks(int updateDelayTicks) {
        this.updateDelayTicks = updateDelayTicks;
    }

    private class ViewedScoreboard {
        private final Scoreboard scoreboard;
        private Objective objective;
        private final Player viewer;

        public boolean equals(Object obj) {
            return obj instanceof SimpleScoreboard.ViewedScoreboard && ((SimpleScoreboard.ViewedScoreboard)obj).getViewer().equals(this.viewer);
        }

        public Scoreboard getScoreboard() {
            return this.scoreboard;
        }

        public Objective getObjective() {
            return this.objective;
        }

        public Player getViewer() {
            return this.viewer;
        }

        public void setObjective(Objective objective) {
            this.objective = objective;
        }

        private ViewedScoreboard(Scoreboard scoreboard, Objective objective, Player viewer) {
            this.scoreboard = scoreboard;
            this.objective = objective;
            this.viewer = viewer;
        }
    }
}