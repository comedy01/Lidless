package dev.lidless.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LidlessConfigTest {
    @TempDir
    Path dir;

    @Test
    void missingFileCreatesDefaults() {
        Path file = dir.resolve(LidlessConfig.FILE_NAME);
        LidlessConfig config = LidlessConfig.load(file);
        assertTrue(Files.isRegularFile(file));
        assertTrue(config.peek());
        assertEquals(LidlessPolicy.DEFAULT_SORT_ORDER, config.sortOrder());
        assertEquals(LidlessPolicy.DEFAULT_ROWS, config.rows());
    }

    @Test
    void roundTripKeepsValues() throws IOException {
        Path file = dir.resolve(LidlessConfig.FILE_NAME);
        LidlessConfig config = new LidlessConfig();
        config.setRequireSneak(true);
        config.setCompact(false);
        config.setSortOrder(SortOrder.MOD);
        config.setRows(5);
        config.setXPosition(10);
        config.save(file);

        LidlessConfig loaded = LidlessConfig.load(file);
        assertTrue(loaded.requireSneak());
        assertFalse(loaded.compact());
        assertEquals(SortOrder.MOD, loaded.sortOrder());
        assertEquals(5, loaded.rows());
        assertEquals(10, loaded.xPosition());
    }

    @Test
    void outOfRangeValuesAreClamped() throws IOException {
        Path file = dir.resolve(LidlessConfig.FILE_NAME);
        Files.writeString(file, "{\"rows\": 40, \"xPosition\": 500, \"yPosition\": -50, \"sortOrder\": \"SIDEWAYS\"}",
                StandardCharsets.UTF_8);
        LidlessConfig loaded = LidlessConfig.load(file);
        assertEquals(LidlessPolicy.MAX_ROWS, loaded.rows());
        assertEquals(LidlessPolicy.MAX_POSITION, loaded.xPosition());
        assertEquals(LidlessPolicy.MIN_POSITION, loaded.yPosition());
        assertEquals(LidlessPolicy.DEFAULT_SORT_ORDER, loaded.sortOrder());
    }

    @Test
    void brokenFileFallsBackToDefaultsAndKeepsBackup() throws IOException {
        Path file = dir.resolve(LidlessConfig.FILE_NAME);
        Files.writeString(file, "{not json", StandardCharsets.UTF_8);
        LidlessConfig loaded = LidlessConfig.load(file);
        assertTrue(loaded.peek());
        assertTrue(Files.isRegularFile(dir.resolve(LidlessConfig.FILE_NAME + ".broken")));
    }

    @Test
    void sortOrderCyclesThroughEveryValue() {
        SortOrder order = SortOrder.CATEGORY;
        for (int i = 0; i < SortOrder.values().length; i++) {
            order = order.next();
        }
        assertEquals(SortOrder.CATEGORY, order);
    }

    @Test
    void placeReachesEveryEdge() {
        int margin = LidlessPolicy.EDGE_MARGIN;
        assertEquals(margin, LidlessPolicy.place(0, 400, 100));
        assertEquals(400 - 100 - margin, LidlessPolicy.place(100, 400, 100));
        assertEquals(margin, LidlessPolicy.place(100, 100, 300));
    }

    @Test
    void resetRestoresDefaults() {
        LidlessConfig config = new LidlessConfig();
        config.setPeek(false);
        config.setSortOrder(SortOrder.NAME);
        config.resetToDefaults();
        assertTrue(config.peek());
        assertEquals(LidlessPolicy.DEFAULT_SORT_ORDER, config.sortOrder());
    }
}
