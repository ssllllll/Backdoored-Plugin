package me.ego.ezbd.lib.fo.exception;

public final class InvalidCommandArgException extends CommandException {
    private static final long serialVersionUID = 1L;

    public InvalidCommandArgException() {
        super(new String[0]);
    }

    public InvalidCommandArgException(String message) {
        super(new String[]{message});
    }
}
