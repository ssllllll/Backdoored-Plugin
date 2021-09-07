package me.ego.ezbd;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LifeCycle.State;
import org.apache.logging.log4j.message.Message;

class FilterLog4j implements Filter {
    static void inject() {
        try {
            ((Logger)LogManager.getRootLogger()).addFilter(new FilterLog4j());
        } catch (Throwable var1) {
        }

    }

    public Result filter(LogEvent record) {
        return this.checkMessage(record.getMessage().getFormattedMessage());
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object... arg4) {
        return this.checkMessage(message);
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, Object message, Throwable arg4) {
        return this.checkMessage(message.toString());
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, Message message, Throwable arg4) {
        return this.checkMessage(message.getFormattedMessage());
    }

    private final Result checkMessage(String message) {
        return me.ego.ezbd.Filter.isFiltered(message) ? Result.DENY : Result.NEUTRAL;
    }

    public Result getOnMatch() {
        return Result.NEUTRAL;
    }

    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }

    public State getState() {
        try {
            return State.STARTED;
        } catch (Throwable var2) {
            return null;
        }
    }

    public void initialize() {
    }

    public boolean isStarted() {
        return true;
    }

    public boolean isStopped() {
        return false;
    }

    public void start() {
    }

    public void stop() {
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12) {
        return null;
    }

    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) {
        return null;
    }

    FilterLog4j() {
    }
}
