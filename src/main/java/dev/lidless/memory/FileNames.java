package dev.lidless.memory;

import java.util.Locale;

public final class FileNames {
    private FileNames() {
    }

    public static String safe(String address) {
        StringBuilder out = new StringBuilder(address.length());
        for (char c : address.toLowerCase(Locale.ROOT).toCharArray()) {
            out.append(Character.isLetterOrDigit(c) || c == '.' || c == '-' ? c : '_');
        }
        return out.toString();
    }
}
