package dev.lidless.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SortPlanner {
    public static final int EMPTY = -1;

    private final int[] kinds;
    private final int[] counts;
    private final int[] limits;
    private final List<Integer> clicks = new ArrayList<>();
    private int carriedKind = EMPTY;
    private int carriedCount;

    private SortPlanner(int[] kinds, int[] counts, int[] limits) {
        this.kinds = kinds.clone();
        this.counts = counts.clone();
        this.limits = limits.clone();
    }

    public static List<Integer> plan(int[] kinds, int[] counts, int[] limits, Comparator<Integer> order) {
        SortPlanner planner = new SortPlanner(kinds, counts, limits);
        planner.merge();
        planner.arrange(order);
        return planner.clicks;
    }

    public static int[][] simulate(int[] kinds, int[] counts, int[] limits, List<Integer> clicks) {
        SortPlanner planner = new SortPlanner(kinds, counts, limits);
        for (int slot : clicks) {
            planner.click(slot);
        }
        return new int[][] {planner.kinds, planner.counts, {planner.carriedKind, planner.carriedCount}};
    }

    private void merge() {
        for (int i = 0; i < kinds.length; i++) {
            for (int j = kinds.length - 1; j > i && kinds[i] != EMPTY && counts[i] < limits[kinds[i]]; j--) {
                if (kinds[j] == kinds[i]) {
                    click(j);
                    click(i);
                    if (carriedKind != EMPTY) {
                        click(j);
                    }
                }
            }
        }
    }

    private void arrange(Comparator<Integer> order) {
        for (int target = 0; target < kinds.length; target++) {
            int best = target;
            for (int i = target + 1; i < kinds.length; i++) {
                if (compare(order, i, best) < 0) {
                    best = i;
                }
            }
            if (best == target || compare(order, best, target) == 0) {
                continue;
            }
            click(best);
            click(target);
            if (carriedKind != EMPTY) {
                click(best);
            }
        }
    }

    private int compare(Comparator<Integer> order, int a, int b) {
        if (kinds[a] == EMPTY || kinds[b] == EMPTY) {
            return Boolean.compare(kinds[a] == EMPTY, kinds[b] == EMPTY);
        }
        if (kinds[a] != kinds[b]) {
            int result = order.compare(kinds[a], kinds[b]);
            if (result != 0) {
                return result;
            }
            return Integer.compare(kinds[a], kinds[b]);
        }
        return Integer.compare(counts[b], counts[a]);
    }

    private void click(int slot) {
        clicks.add(slot);
        if (carriedKind == EMPTY) {
            carriedKind = kinds[slot];
            carriedCount = counts[slot];
            kinds[slot] = EMPTY;
            counts[slot] = 0;
        } else if (kinds[slot] == EMPTY) {
            kinds[slot] = carriedKind;
            counts[slot] = carriedCount;
            carriedKind = EMPTY;
            carriedCount = 0;
        } else if (kinds[slot] == carriedKind) {
            int moved = Math.min(carriedCount, limits[kinds[slot]] - counts[slot]);
            counts[slot] += moved;
            carriedCount -= moved;
            if (carriedCount == 0) {
                carriedKind = EMPTY;
            }
        } else {
            int kind = kinds[slot];
            int count = counts[slot];
            kinds[slot] = carriedKind;
            counts[slot] = carriedCount;
            carriedKind = kind;
            carriedCount = count;
        }
    }
}
