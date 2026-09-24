package dev.lidless.peek;

import dev.lidless.hud.Canvas;
import dev.lidless.info.Counts;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ItemGrid {
    public static final int CELL = 18;

    public record Cell(ItemStack stack, String count) {
    }

    private ItemGrid() {
    }

    public static List<Cell> slots(List<ItemStack> items) {
        List<Cell> cells = new ArrayList<>(items.size());
        for (ItemStack stack : items) {
            cells.add(new Cell(stack, null));
        }
        return cells;
    }

    public static List<Cell> compact(List<ItemStack> items) {
        List<ItemStack> kinds = new ArrayList<>();
        List<int[]> totals = new ArrayList<>();
        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }
            int found = -1;
            for (int i = 0; i < kinds.size(); i++) {
                if (ItemStack.isSameItemSameComponents(kinds.get(i), stack)) {
                    found = i;
                    break;
                }
            }
            if (found < 0) {
                kinds.add(stack.copyWithCount(1));
                totals.add(new int[] {stack.getCount()});
            } else {
                totals.get(found)[0] += stack.getCount();
            }
        }
        List<Integer> order = new ArrayList<>(kinds.size());
        for (int i = 0; i < kinds.size(); i++) {
            order.add(i);
        }
        order.sort(Comparator.comparingInt((Integer i) -> totals.get(i)[0]).reversed());
        List<Cell> cells = new ArrayList<>(order.size());
        for (int i : order) {
            int total = totals.get(i)[0];
            cells.add(new Cell(kinds.get(i), total > 1 ? Counts.shortCount(total) : null));
        }
        return cells;
    }

    public static boolean isEmpty(List<ItemStack> items) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static int rows(int cells, int columns) {
        return (cells + columns - 1) / columns;
    }

    public static void draw(Canvas canvas, Font font, List<Cell> cells, int columns, int shown,
                            int x, int y, int slotColor) {
        for (int i = 0; i < shown && i < cells.size(); i++) {
            int cellX = x + (i % columns) * CELL;
            int cellY = y + (i / columns) * CELL;
            canvas.fill(cellX, cellY, cellX + CELL - 1, cellY + CELL - 1, slotColor);
            Cell cell = cells.get(i);
            if (!cell.stack().isEmpty()) {
                canvas.item(cell.stack(), cellX + 1, cellY + 1);
                canvas.itemDecorations(font, cell.stack(), cellX + 1, cellY + 1, cell.count());
            }
        }
    }
}
