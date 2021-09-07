package me.ego.ezbd.lib.fo;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final DateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private static final DateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("dd.MM HH:mm");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);

    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    public static long currentTimeTicks() {
        return System.currentTimeMillis() / 50L;
    }

    public static String getFormattedDate() {
        return getFormattedDate(System.currentTimeMillis());
    }

    public static String getFormattedDate(long time) {
        return DATE_FORMAT.format(time);
    }

    public static String getFormattedDateShort() {
        return DATE_FORMAT_SHORT.format(System.currentTimeMillis());
    }

    public static String getFormattedDateShort(long time) {
        return DATE_FORMAT_SHORT.format(time);
    }

    public static String getFormattedDateMonth(long time) {
        return DATE_FORMAT_MONTH.format(time);
    }

    public static long toTicks(String humanReadableTime) {
        Valid.checkNotNull(humanReadableTime, "Time is null");
        long seconds = 0L;
        String[] split = humanReadableTime.split(" ");
        Valid.checkBoolean(split.length > 1, "Expected human readable time like '1 second', got '" + humanReadableTime + "' instead", new Object[0]);

        for(int i = 1; i < split.length; ++i) {
            String sub = split[i].toLowerCase();
            int multiplier = false;
            long unit = 0L;
            boolean isTicks = false;

            int multiplier;
            try {
                multiplier = Integer.parseInt(split[i - 1]);
            } catch (NumberFormatException var11) {
                continue;
            }

            if (sub.startsWith("tick")) {
                isTicks = true;
            } else if (sub.startsWith("second")) {
                unit = 1L;
            } else if (sub.startsWith("minute")) {
                unit = 60L;
            } else if (sub.startsWith("hour")) {
                unit = 3600L;
            } else if (sub.startsWith("day")) {
                unit = 86400L;
            } else if (sub.startsWith("week")) {
                unit = 604800L;
            } else if (sub.startsWith("month")) {
                unit = 2629743L;
            } else if (sub.startsWith("year")) {
                unit = 31556926L;
            } else {
                if (!sub.startsWith("potato")) {
                    throw new IllegalArgumentException("Must define date type! Example: '1 second' (Got '" + sub + "')");
                }

                unit = 1337L;
            }

            seconds += (long)multiplier * (isTicks ? 1L : unit * 20L);
        }

        return seconds;
    }

    public static String formatTimeGeneric(long seconds) {
        long second = seconds % 60L;
        long minute = seconds / 60L;
        String hourMsg = "";
        if (minute >= 60L) {
            long hour = seconds / 60L / 60L;
            minute %= 60L;
            hourMsg = hour + (hour == 1L ? " hour" : " hours") + " ";
        }

        return hourMsg + (minute != 0L ? minute : "") + (minute > 0L ? (minute == 1L ? " minute" : " minutes") + " " : "") + Long.parseLong(String.valueOf(second)) + (Long.parseLong(String.valueOf(second)) == 1L ? " second" : " seconds");
    }

    public static String formatTimeDays(long seconds) {
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        return days + " days " + hours % 24L + " hours " + minutes % 60L + " minutes " + seconds % 60L + " seconds";
    }

    public static String formatTimeShort(long seconds) {
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        hours %= 24L;
        minutes %= 60L;
        seconds %= 60L;
        return (days > 0L ? days + "d " : "") + (hours > 0L ? hours + "h " : "") + (minutes > 0L ? minutes + "m " : "") + seconds + "s";
    }

    public static long parseToken(String text) {
        Matcher matcher = TOKEN_PATTERN.matcher(text);
        long years = 0L;
        long months = 0L;
        long weeks = 0L;
        long days = 0L;
        long hours = 0L;
        long minutes = 0L;
        long seconds = 0L;
        boolean found = false;

        label74:
        while(matcher.find()) {
            if (matcher.group() != null && !matcher.group().isEmpty()) {
                int i;
                for(i = 0; i < matcher.groupCount(); ++i) {
                    if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    i = 1;

                    while(true) {
                        if (i >= 8) {
                            break label74;
                        }

                        if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
                            long output = Long.parseLong(matcher.group(i));
                            if (i == 1) {
                                checkLimit("years", output, 10);
                                years = output;
                            } else if (i == 2) {
                                checkLimit("months", output, 1200);
                                months = output;
                            } else if (i == 3) {
                                checkLimit("weeks", output, 400);
                                weeks = output;
                            } else if (i == 4) {
                                checkLimit("days", output, 3100);
                                days = output;
                            } else if (i == 5) {
                                checkLimit("hours", output, 2400);
                                hours = output;
                            } else if (i == 6) {
                                checkLimit("minutes", output, 6000);
                                minutes = output;
                            } else if (i == 7) {
                                checkLimit("seconds", output, 6000);
                                seconds = output;
                            }
                        }

                        ++i;
                    }
                }
            }
        }

        if (!found) {
            throw new NumberFormatException("Date not found from: " + text);
        } else {
            return (seconds + minutes * 60L + hours * 3600L + days * 86400L + weeks * 7L * 86400L + months * 30L * 86400L + years * 365L * 86400L) * 1000L;
        }
    }

    private static void checkLimit(String type, long value, int maxLimit) {
        if (value > (long)maxLimit) {
            throw new IllegalArgumentException("Value type " + type + " is out of bounds! Max limit: " + maxLimit + ", given: " + value);
        }
    }

    public static String toSQLTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        return (new Timestamp(date.getTime())).toString();
    }

    public static long fromSQLTimestamp(String timestamp) {
        return Timestamp.valueOf(timestamp).getTime();
    }

    private TimeUtil() {
    }
}