package me.ego.ezbd.lib.fo.exception;

public class CommandException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String[] messages;

    public CommandException(String... messages) {
        super("");
        this.messages = messages;
    }

    public String[] getMessages() {
        return this.messages;
    }
}
