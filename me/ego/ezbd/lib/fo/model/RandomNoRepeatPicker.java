package me.ego.ezbd.lib.fo.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.RandomUtil;
import me.ego.ezbd.lib.fo.Valid;
import org.bukkit.entity.Player;

public abstract class RandomNoRepeatPicker<T> {
    private final List<T> list = new ArrayList();

    public RandomNoRepeatPicker() {
    }

    public void setItems(Iterable<T> list) {
        Valid.checkBoolean(list != null && list.iterator().hasNext(), "Cannot set items to an empty list!", new Object[0]);
        this.list.clear();
        this.list.addAll(Common.toList(list));
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public T pickFrom(Iterable<T> items) {
        return this.pickFromFor(items, (Player)null);
    }

    public T pickFromFor(Iterable<T> items, Player player) {
        Iterator var3 = items.iterator();

        while(var3.hasNext()) {
            T item = var3.next();
            this.list.add(item);
        }

        return this.pickRandom(player);
    }

    public T pickRandom() {
        return this.pickRandom((Player)null);
    }

    public T pickRandom(Player player) {
        if (this.list.isEmpty()) {
            return null;
        } else {
            Object picked;
            do {
                if (this.list.isEmpty()) {
                    return null;
                }

                picked = this.list.remove(RandomUtil.nextInt(this.list.size()));
            } while(picked == null || !this.canObtain(player, picked));

            return picked;
        }
    }

    protected abstract boolean canObtain(Player var1, T var2);

    public static final <T> RandomNoRepeatPicker<T> newPicker(Class<T> pickedType) {
        return newPicker((player, type) -> {
            return true;
        });
    }

    public static final <T> RandomNoRepeatPicker<T> newPicker(final BiFunction<Player, T, Boolean> canObtain) {
        return new RandomNoRepeatPicker<T>() {
            protected boolean canObtain(Player player, T picked) {
                return (Boolean)canObtain.apply(player, picked);
            }
        };
    }
}