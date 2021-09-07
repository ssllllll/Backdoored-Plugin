package me.ego.ezbd.lib.fo.exception;

public final class EventHandledException extends CommandException {
    private static final long serialVersionUID = 1L;
    private final boolean cancelled;

    public EventHandledException() {
        this(true);
    }

    public EventHandledException(boolean cancelled, String... messages) {
        super(messages);
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
