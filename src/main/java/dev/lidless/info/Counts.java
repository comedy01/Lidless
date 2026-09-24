package dev.lidless.info;

import java.util.Locale;

public final class Counts {
    private Counts() {
    }

    public static String shortCount(int count) {
        if (count < 1000) {
            return Integer.toString(count);
        }
        if (count < 10000) {
            double thousands = count / 1000.0D;
            String text = String.format(Locale.ROOT, "%.1f", Math.floor(thousands * 10.0D) / 10.0D);
            return (text.endsWith(".0") ? text.substring(0, text.length() - 2) : text) + "k";
        }
        return count / 1000 + "k";
    }

    public static String ago(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        if (seconds < 60L) {
            return seconds + "s";
        }
        long minutes = seconds / 60L;
        if (minutes < 60L) {
            return minutes + "m";
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + "h";
        }
        return hours / 24L + "d";
    }
}
