package me.ego.ezbd.lib.fo.bungee;

/** @deprecated */
public enum FoBungeeAction implements BungeeAction {
    BUNGEE_COMMAND(new Object[]{String.class}),
    TELL_PLAYER(new Object[]{String.class, String.class}),
    CHAT_MUTE(new Object[]{String.class}),
    CHAT_CLEAR(new Object[]{String.class}),
    CHANNEL(new Object[]{String.class, String.class, String.class, Boolean.class, Boolean.class}),
    ANNOUNCEMENT(new Object[]{String.class, String.class}),
    BROADCAST_JSON_WITH_PERMISSION_AS(new Object[]{String.class, String.class, String.class, Boolean.class}),
    SPY(new Object[]{String.class}),
    PM_LOOKUP(new Object[]{String.class, String.class, String.class, String.class, Boolean.class, Boolean.class, Boolean.class}),
    PM_PLAYER_NOT_FOUND(new Object[]{String.class, String.class}),
    PM_PLAYER_FOUND(new Object[]{String.class, String.class, String.class});

    private final Class<?>[] content;

    private FoBungeeAction(Object... validValues) {
        Class<?>[] classes = new Class[validValues.length];

        for(int i = 0; i < classes.length; ++i) {
            Object value = validValues[i];
            classes[i] = value.getClass();
        }

        this.content = classes;
    }

    public Class<?>[] getContent() {
        return this.content;
    }
}
