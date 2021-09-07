package me.ego.ezbd.lib.fo.plugin;

import java.util.Iterator;
import java.util.List;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.collection.StrictList;
import me.ego.ezbd.lib.fo.command.SimpleCommandGroup;
import me.ego.ezbd.lib.fo.event.SimpleListener;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

final class Reloadables {
    private final StrictList<Listener> listeners = new StrictList();
    private final StrictList<SimpleCommandGroup> commandGroups = new StrictList();

    Reloadables() {
    }

    void reload() {
        Iterator var1 = this.listeners.iterator();

        while(var1.hasNext()) {
            Listener listener = (Listener)var1.next();
            HandlerList.unregisterAll(listener);
        }

        this.listeners.clear();
        var1 = this.commandGroups.iterator();

        while(var1.hasNext()) {
            SimpleCommandGroup commandGroup = (SimpleCommandGroup)var1.next();
            commandGroup.unregister();
        }

        this.commandGroups.clear();
    }

    void registerEvents(Listener listener) {
        Common.registerEvents(listener);
        this.listeners.add(listener);
    }

    <T extends Event> void registerEvents(SimpleListener<T> listener) {
        listener.register();
        this.listeners.add(listener);
    }

    void registerCommands(String label, List<String> aliases, SimpleCommandGroup group) {
        group.register(label, aliases);
        this.commandGroups.add(group);
    }

    void registerCommands(StrictList<String> labelAndAliases, SimpleCommandGroup group) {
        group.register(labelAndAliases);
        this.commandGroups.add(group);
    }
}