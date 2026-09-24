package dev.lidless.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CountsTest {
    @Test
    void shortCountsStayReadable() {
        assertEquals("7", Counts.shortCount(7));
        assertEquals("999", Counts.shortCount(999));
        assertEquals("1k", Counts.shortCount(1000));
        assertEquals("1.7k", Counts.shortCount(1728));
        assertEquals("9.9k", Counts.shortCount(9999));
        assertEquals("12k", Counts.shortCount(12345));
    }

    @Test
    void agesUseTheLargestUnit() {
        assertEquals("0s", Counts.ago(-5L));
        assertEquals("42s", Counts.ago(42_000L));
        assertEquals("5m", Counts.ago(5 * 60_000L + 10_000L));
        assertEquals("3h", Counts.ago(3 * 3_600_000L));
        assertEquals("2d", Counts.ago(50 * 3_600_000L));
    }
}
