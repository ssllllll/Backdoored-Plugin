package me.ego.ezbd.lib.fo.event;

import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Messenger;
import me.ego.ezbd.lib.fo.PlayerUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.debug.LagCatcher;
import me.ego.ezbd.lib.fo.exception.EventHandledException;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.Variables;
import me.ego.ezbd.lib.fo.plugin.SimplePlugin;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;

public abstract class SimpleListener<T extends Event> implements Listener, EventExecutor {
    private final Class<T> eventClass;
    private final EventPriority priority;
    private final boolean ignoreCancelled;
    private T event;

    public SimpleListener(Class<T> event) {
        this(event, EventPriority.NORMAL);
    }

    public SimpleListener(Class<T> event, EventPriority priority) {
        this(event, priority, true);
    }

    public final void execute(Listener listener, Event event) throws EventException {
        if (event.getClass().equals(this.eventClass)) {
            String logName = listener.getClass().getSimpleName() + " listening to " + event.getEventName() + " at " + this.priority + " priority";
            LagCatcher.start(logName);

            try {
                this.event = (Event)this.eventClass.cast(event);
                this.execute(event);
            } catch (EventHandledException var16) {
                String[] messages = var16.getMessages();
                boolean cancelled = var16.isCancelled();
                Player player = this.findPlayer();
                if (messages != null && player != null) {
                    String[] var8 = messages;
                    int var9 = messages.length;

                    for(int var10 = 0; var10 < var9; ++var10) {
                        String message = var8[var10];
                        message = Variables.replace(message, player);
                        if (Messenger.ENABLED) {
                            Messenger.error(player, message);
                        } else {
                            Common.tell(player, new String[]{"&c" + message});
                        }
                    }
                }

                if (cancelled && event instanceof Cancellable) {
                    ((Cancellable)event).setCancelled(true);
                }
            } catch (Throwable var17) {
                Common.error(var17, new String[]{"Unhandled exception listening to " + this.eventClass.getSimpleName()});
            } finally {
                LagCatcher.end(logName);
            }

        }
    }

    protected abstract void execute(T var1);

    protected Player findPlayer() {
        Valid.checkNotNull(this.event, "Called findPlayer for null event!");
        if (this.event instanceof PlayerEvent) {
            return ((PlayerEvent)this.event).getPlayer();
        } else {
            throw new FoException("Called findPlayer but not method not implemented for event " + this.event);
        }
    }

    protected final void checkNotNull(Object toCheck, String... nullMessages) {
        this.checkBoolean(toCheck != null, nullMessages);
    }

    protected final void checkBoolean(boolean condition, String... falseMessages) {
        if (!condition) {
            throw new EventHandledException(true, falseMessages);
        }
    }

    protected final void checkPerm(String permission) {
        this.checkPerm(permission, SimpleLocalization.NO_PERMISSION);
    }

    protected final boolean hasPerm(String permission) {
        return PlayerUtil.hasPerm(this.findPlayer(), permission);
    }

    protected final void checkPerm(String permission, String falseMessage) {
        Player player = this.findPlayer();
        Valid.checkNotNull(player, "Player cannot be null for " + this.event + "!");
        if (!PlayerUtil.hasPerm(player, permission)) {
            throw new EventHandledException(true, new String[]{falseMessage.replace("{permission}", permission)});
        }
    }

    protected final void cancel(String... messages) {
        throw new EventHandledException(true, messages);
    }

    protected final void cancel() {
        throw new EventHandledException(true, new String[0]);
    }

    protected final void returnTell(String... messages) {
        throw new EventHandledException(false, messages);
    }

    public final void register() {
        Bukkit.getPluginManager().registerEvent(this.eventClass, this, this.priority, this, SimplePlugin.getInstance(), this.ignoreCancelled);
    }

    public SimpleListener(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled) {
        this.eventClass = eventClass;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
    }

    protected T getEvent() {
        return this.event;
    }
}
