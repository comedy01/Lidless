package dev.lidless.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class LidlessConfig {
    public static final String FILE_NAME = "lidless.json";

    private static final Logger LOGGER = LoggerFactory.getLogger("lidless");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SerializedName("peek")
    private boolean peek = LidlessPolicy.DEFAULT_PEEK;

    @SerializedName("requireSneak")
    private boolean requireSneak = LidlessPolicy.DEFAULT_REQUIRE_SNEAK;

    @SerializedName("remember")
    private boolean remember = LidlessPolicy.DEFAULT_REMEMBER;

    @SerializedName("compact")
    private boolean compact = LidlessPolicy.DEFAULT_COMPACT;

    @SerializedName("tooltipPreview")
    private boolean tooltipPreview = LidlessPolicy.DEFAULT_TOOLTIP_PREVIEW;

    @SerializedName("searchBox")
    private boolean searchBox = LidlessPolicy.DEFAULT_SEARCH_BOX;

    @SerializedName("sortButtons")
    private boolean sortButtons = LidlessPolicy.DEFAULT_SORT_BUTTONS;

    @SerializedName("middleClickSort")
    private boolean middleClickSort = LidlessPolicy.DEFAULT_MIDDLE_CLICK_SORT;

    @SerializedName("sortOrder")
    private SortOrder sortOrder = LidlessPolicy.DEFAULT_SORT_ORDER;

    @SerializedName("rows")
    private int rows = LidlessPolicy.DEFAULT_ROWS;

    @SerializedName("xPosition")
    private int xPosition = LidlessPolicy.DEFAULT_X_POSITION;

    @SerializedName("yPosition")
    private int yPosition = LidlessPolicy.DEFAULT_Y_POSITION;

    public boolean peek() {
        return peek;
    }

    public void setPeek(boolean value) {
        peek = value;
    }

    public boolean requireSneak() {
        return requireSneak;
    }

    public void setRequireSneak(boolean value) {
        requireSneak = value;
    }

    public boolean remember() {
        return remember;
    }

    public void setRemember(boolean value) {
        remember = value;
    }

    public boolean compact() {
        return compact;
    }

    public void setCompact(boolean value) {
        compact = value;
    }

    public boolean tooltipPreview() {
        return tooltipPreview;
    }

    public void setTooltipPreview(boolean value) {
        tooltipPreview = value;
    }

    public boolean searchBox() {
        return searchBox;
    }

    public void setSearchBox(boolean value) {
        searchBox = value;
    }

    public boolean sortButtons() {
        return sortButtons;
    }

    public void setSortButtons(boolean value) {
        sortButtons = value;
    }

    public boolean middleClickSort() {
        return middleClickSort;
    }

    public void setMiddleClickSort(boolean value) {
        middleClickSort = value;
    }

    public SortOrder sortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder value) {
        sortOrder = value == null ? LidlessPolicy.DEFAULT_SORT_ORDER : value;
    }

    public int rows() {
        return rows;
    }

    public void setRows(int value) {
        rows = LidlessPolicy.clampRows(value);
    }

    public int xPosition() {
        return xPosition;
    }

    public void setXPosition(int value) {
        xPosition = LidlessPolicy.clampPosition(value);
    }

    public int yPosition() {
        return yPosition;
    }

    public void setYPosition(int value) {
        yPosition = LidlessPolicy.clampPosition(value);
    }

    public void resetToDefaults() {
        peek = LidlessPolicy.DEFAULT_PEEK;
        requireSneak = LidlessPolicy.DEFAULT_REQUIRE_SNEAK;
        remember = LidlessPolicy.DEFAULT_REMEMBER;
        compact = LidlessPolicy.DEFAULT_COMPACT;
        tooltipPreview = LidlessPolicy.DEFAULT_TOOLTIP_PREVIEW;
        searchBox = LidlessPolicy.DEFAULT_SEARCH_BOX;
        sortButtons = LidlessPolicy.DEFAULT_SORT_BUTTONS;
        middleClickSort = LidlessPolicy.DEFAULT_MIDDLE_CLICK_SORT;
        sortOrder = LidlessPolicy.DEFAULT_SORT_ORDER;
        rows = LidlessPolicy.DEFAULT_ROWS;
        xPosition = LidlessPolicy.DEFAULT_X_POSITION;
        yPosition = LidlessPolicy.DEFAULT_Y_POSITION;
    }

    private void sanitize() {
        setSortOrder(sortOrder);
        setRows(rows);
        setXPosition(xPosition);
        setYPosition(yPosition);
    }

    public static LidlessConfig load(Path file) {
        if (!Files.isRegularFile(file)) {
            LidlessConfig fresh = new LidlessConfig();
            fresh.saveQuietly(file);
            return fresh;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            LidlessConfig loaded = GSON.fromJson(reader, LidlessConfig.class);
            if (loaded == null) {
                throw new JsonParseException("config file is empty");
            }
            loaded.sanitize();
            return loaded;
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read {}; using defaults. {}", file, e.toString());
            moveAside(file);
            LidlessConfig fresh = new LidlessConfig();
            fresh.saveQuietly(file);
            return fresh;
        }
    }

    public void save(Path file) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(this) + System.lineSeparator(), StandardCharsets.UTF_8);
        try {
            Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void saveQuietly(Path file) {
        try {
            save(file);
        } catch (IOException e) {
            LOGGER.warn("Could not save {}: {}", file, e.toString());
        }
    }

    private static void moveAside(Path file) {
        try {
            Files.move(
                    file,
                    file.resolveSibling(file.getFileName() + ".broken"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Could not back up unreadable config {}: {}", file, e.toString());
        }
    }
}
