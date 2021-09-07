package me.ego.ezbd.lib.fo.exception;

import me.ego.ezbd.lib.fo.debug.Debugger;

public class FoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FoException(Throwable t) {
        super(t);
        Debugger.saveError(t, new String[0]);
    }

    public FoException(String message) {
        super(message);
        Debugger.saveError(this, new String[]{message});
    }

    public FoException(Throwable t, String message) {
        super(message, t);
        Debugger.saveError(t, new String[]{message});
    }

    public FoException() {
        Debugger.saveError(this, new String[0]);
    }

    public String getMessage() {
        return "Report: " + super.getMessage();
    }
}
