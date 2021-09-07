package me.ego.ezbd.lib.fo;

import lombok.NonNull;
import me.ego.ezbd.lib.fo.settings.SimpleSettings;

final class TimedCharSequence implements CharSequence {
    private final CharSequence message;
    private final long futureTimestampLimit;

    private TimedCharSequence(@NonNull CharSequence message, long futureTimestampLimit) {
        if (message == null) {
            throw new NullPointerException("message is marked non-null but is null");
        } else {
            this.message = message;
            this.futureTimestampLimit = futureTimestampLimit;
        }
    }

    public char charAt(int index) {
        return this.message.charAt(index);
    }

    public int length() {
        return this.message.length();
    }

    public CharSequence subSequence(int start, int end) {
        return new TimedCharSequence(this.message.subSequence(start, end), this.futureTimestampLimit);
    }

    public String toString() {
        return this.message.toString();
    }

    static TimedCharSequence withSettingsLimit(CharSequence message) {
        return new TimedCharSequence(message, System.currentTimeMillis() + (long)SimpleSettings.REGEX_TIMEOUT);
    }
}