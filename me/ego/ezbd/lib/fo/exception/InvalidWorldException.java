package me.ego.ezbd.lib.fo.exception;

public final class InvalidWorldException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String world;

    public InvalidWorldException(String message, String world) {
        super(message);
        this.world = world;
    }

    public String getWorld() {
        return this.world;
    }
}
