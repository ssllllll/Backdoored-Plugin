package me.ego.ezbd.lib.fo.remain;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

class AdvancementAccessor {
    private final NamespacedKey key = new NamespacedKey(SimplePlugin.getInstance(), UUID.randomUUID().toString());
    private final String icon;
    private final String message;

    AdvancementAccessor(String message, String icon) {
        this.message = message;
        this.icon = icon;
    }

    public void show(Player player) {
        this.loadAdvancement();
        this.grantAdvancement(player);
        Common.runLater(10, () -> {
            this.revokeAdvancement(player);
            this.removeAdvancement();
        });
    }

    private void loadAdvancement() {
        Bukkit.getUnsafe().loadAdvancement(this.key, this.compileJson0());
    }

    private String compileJson0() {
        JsonObject json = new JsonObject();
        JsonObject icon = new JsonObject();
        icon.addProperty("item", this.icon);
        JsonObject display = new JsonObject();
        display.add("icon", icon);
        display.addProperty("title", this.message);
        display.addProperty("description", "");
        display.addProperty("background", "minecraft:textures/gui/advancements/backgrounds/adventure.png");
        display.addProperty("frame", "goal");
        display.addProperty("announce_to_chat", false);
        display.addProperty("show_toast", true);
        display.addProperty("hidden", true);
        JsonObject criteria = new JsonObject();
        JsonObject trigger = new JsonObject();
        trigger.addProperty("trigger", "minecraft:impossible");
        criteria.add("impossible", trigger);
        json.add("criteria", criteria);
        json.add("display", display);
        return (new Gson()).toJson(json);
    }

    private void grantAdvancement(Player plazer) {
        Advancement adv = this.getAdvancement();
        AdvancementProgress progress = plazer.getAdvancementProgress(adv);
        if (!progress.isDone()) {
            progress.getRemainingCriteria().forEach((crit) -> {
                progress.awardCriteria(crit);
            });
        }

    }

    private void revokeAdvancement(Player plazer) {
        Advancement adv = this.getAdvancement();
        AdvancementProgress prog = plazer.getAdvancementProgress(adv);
        if (prog.isDone()) {
            prog.getAwardedCriteria().forEach((crit) -> {
                prog.revokeCriteria(crit);
            });
        }

    }

    private void removeAdvancement() {
        Bukkit.getUnsafe().removeAdvancement(this.key);
    }

    private Advancement getAdvancement() {
        return Bukkit.getAdvancement(this.key);
    }
}