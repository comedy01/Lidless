package dev.lidless.config;

public enum SortOrder {
    CATEGORY("category"),
    NAME("name"),
    COUNT("count"),
    MOD("mod");

    private final String key;

    SortOrder(String key) {
        this.key = key;
    }

    public String translationKey() {
        return "lidless.sort." + key;
    }

    public SortOrder next() {
        SortOrder[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
