package dev.lidless.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SortPlannerTest {
    private static final int E = SortPlanner.EMPTY;
    private static final Comparator<Integer> BY_KIND = Comparator.naturalOrder();

    @Test
    void mergesPartialStacksAndSorts() {
        int[] kinds = {2, E, 0, 2, 1, 0};
        int[] counts = {10, 0, 30, 60, 5, 50};
        int[] limits = {64, 64, 64};
        List<Integer> clicks = SortPlanner.plan(kinds, counts, limits, BY_KIND);
        int[][] result = SortPlanner.simulate(kinds, counts, limits, clicks);

        assertArrayEquals(new int[] {0, 0, 1, 2, 2, E}, result[0]);
        assertArrayEquals(new int[] {64, 16, 5, 64, 6, 0}, result[1]);
        assertEquals(E, result[2][0]);
    }

    @Test
    void sortedInputNeedsNoClicks() {
        int[] kinds = {0, 1, 2, E};
        int[] counts = {64, 1, 3, 0};
        int[] limits = {64, 1, 64};
        assertTrue(SortPlanner.plan(kinds, counts, limits, BY_KIND).isEmpty());
    }

    @Test
    void unstackableItemsStaySeparate() {
        int[] kinds = {1, 0, 1, 0};
        int[] counts = {1, 1, 1, 1};
        int[] limits = {1, 1};
        List<Integer> clicks = SortPlanner.plan(kinds, counts, limits, BY_KIND);
        int[][] result = SortPlanner.simulate(kinds, counts, limits, clicks);
        assertArrayEquals(new int[] {0, 0, 1, 1}, result[0]);
        assertArrayEquals(new int[] {1, 1, 1, 1}, result[1]);
    }

    @Test
    void customOrderIsRespected() {
        int[] kinds = {0, 1, 2};
        int[] counts = {1, 1, 1};
        int[] limits = {64, 64, 64};
        List<Integer> clicks = SortPlanner.plan(kinds, counts, limits, BY_KIND.reversed());
        assertArrayEquals(new int[] {2, 1, 0}, SortPlanner.simulate(kinds, counts, limits, clicks)[0]);
    }

    @Test
    void randomLayoutsKeepEveryItemAndEndSorted() {
        Random random = new Random(42L);
        for (int round = 0; round < 300; round++) {
            int size = 1 + random.nextInt(54);
            int kindCount = 1 + random.nextInt(6);
            int[] limits = new int[kindCount];
            for (int k = 0; k < kindCount; k++) {
                limits[k] = random.nextBoolean() ? 64 : random.nextBoolean() ? 16 : 1;
            }
            int[] kinds = new int[size];
            int[] counts = new int[size];
            for (int i = 0; i < size; i++) {
                if (random.nextInt(3) == 0) {
                    kinds[i] = E;
                } else {
                    kinds[i] = random.nextInt(kindCount);
                    counts[i] = 1 + random.nextInt(limits[kinds[i]]);
                }
            }

            List<Integer> clicks = SortPlanner.plan(kinds, counts, limits, BY_KIND);
            int[][] result = SortPlanner.simulate(kinds, counts, limits, clicks);
            assertEquals(E, result[2][0], "items left on the cursor");

            long[] before = totals(kinds, counts, kindCount);
            long[] after = totals(result[0], result[1], kindCount);
            assertArrayEquals(before, after, "item totals changed");

            int previous = Integer.MIN_VALUE;
            boolean emptySeen = false;
            for (int i = 0; i < size; i++) {
                int kind = result[0][i];
                if (kind == E) {
                    emptySeen = true;
                    continue;
                }
                assertTrue(!emptySeen, "item after an empty slot");
                assertTrue(kind >= previous, "kinds out of order");
                previous = kind;
            }
            for (int k = 0; k < kindCount; k++) {
                int partial = 0;
                for (int i = 0; i < size; i++) {
                    if (result[0][i] == k && result[1][i] < limits[k]) {
                        partial++;
                    }
                }
                assertTrue(partial <= 1, "more than one partial stack of a kind");
            }
        }
    }

    private static long[] totals(int[] kinds, int[] counts, int kindCount) {
        long[] totals = new long[kindCount];
        for (int i = 0; i < kinds.length; i++) {
            if (kinds[i] != E) {
                totals[kinds[i]] += counts[i];
            }
        }
        return totals;
    }
}
