package dev.lidless.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SearchQuery {
    private final List<String> words;
    private final List<String> mods;

    private SearchQuery(List<String> words, List<String> mods) {
        this.words = words;
        this.mods = mods;
    }

    public static SearchQuery parse(String text) {
        List<String> words = new ArrayList<>();
        List<String> mods = new ArrayList<>();
        for (String part : text.toLowerCase(Locale.ROOT).trim().split("\\s+")) {
            if (part.isEmpty()) {
                continue;
            }
            if (part.startsWith("@")) {
                if (part.length() > 1) {
                    mods.add(part.substring(1));
                }
            } else {
                words.add(part);
            }
        }
        return new SearchQuery(words, mods);
    }

    public boolean isEmpty() {
        return words.isEmpty() && mods.isEmpty();
    }

    public boolean matches(String namespace, List<String> texts) {
        for (String mod : mods) {
            if (!namespace.toLowerCase(Locale.ROOT).startsWith(mod)) {
                return false;
            }
        }
        for (String word : words) {
            boolean found = false;
            for (String text : texts) {
                if (text.toLowerCase(Locale.ROOT).contains(word)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
