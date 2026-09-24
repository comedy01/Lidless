package dev.lidless.config;

public final class LidlessPolicy {
    public static final boolean DEFAULT_PEEK = true;
    public static final boolean DEFAULT_REQUIRE_SNEAK = false;
    public static final boolean DEFAULT_REMEMBER = true;
    public static final boolean DEFAULT_COMPACT = true;
    public static final boolean DEFAULT_TOOLTIP_PREVIEW = true;
    public static final boolean DEFAULT_SEARCH_BOX = true;
    public static final boolean DEFAULT_SORT_BUTTONS = true;
    public static final boolean DEFAULT_MIDDLE_CLICK_SORT = true;
    public static final SortOrder DEFAULT_SORT_ORDER = SortOrder.CATEGORY;

    public static final int MIN_ROWS = 1;
    public static final int MAX_ROWS = 6;
    public static final int DEFAULT_ROWS = 3;

    public static final int MIN_POSITION = 0;
    public static final int MAX_POSITION = 100;
    public static final int DEFAULT_X_POSITION = 100;
    public static final int DEFAULT_Y_POSITION = 50;

    public static final int EDGE_MARGIN = 4;

    private LidlessPolicy() {
    }

    public static int clampRows(int value) {
        return Math.max(MIN_ROWS, Math.min(MAX_ROWS, value));
    }

    public static int clampPosition(int value) {
        return Math.max(MIN_POSITION, Math.min(MAX_POSITION, value));
    }

    public static int place(int percent, int screen, int size) {
        int free = screen - size - EDGE_MARGIN * 2;
        if (free <= 0) {
            return EDGE_MARGIN;
        }
        return EDGE_MARGIN + Math.round(free * clampPosition(percent) / 100.0F);
    }
}
