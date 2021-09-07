package me.ego.ezbd.lib.fo.collection;

public abstract class StrictCollection {
    private final String cannotRemoveMessage;
    private final String cannotAddMessage;

    public abstract Object serialize();

    protected String getCannotRemoveMessage() {
        return this.cannotRemoveMessage;
    }

    protected String getCannotAddMessage() {
        return this.cannotAddMessage;
    }

    public StrictCollection(String cannotRemoveMessage, String cannotAddMessage) {
        this.cannotRemoveMessage = cannotRemoveMessage;
        this.cannotAddMessage = cannotAddMessage;
    }
}