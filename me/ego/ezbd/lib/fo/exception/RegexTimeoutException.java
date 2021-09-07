package me.ego.ezbd.lib.fo.exception;

public final class RegexTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String checkedMessage;
    private final long executionLimit;

    public RegexTimeoutException(CharSequence checkedMessage, long timeoutLimit) {
        this.checkedMessage = checkedMessage.toString();
        this.executionLimit = timeoutLimit;
    }

    public String getCheckedMessage() {
        return this.checkedMessage;
    }

    public long getExecutionLimit() {
        return this.executionLimit;
    }
}