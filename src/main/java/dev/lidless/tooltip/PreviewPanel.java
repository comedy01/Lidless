package dev.lidless.tooltip;

import dev.lidless.hud.Canvas;
import dev.lidless.peek.ItemGrid;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class PreviewPanel {
    private static final int COLUMNS = 9;
    private static final int MAX_ROWS = 6;
    private static final int MARGIN = 2;
    private static final int LINE_HEIGHT = 10;
    private static final int NOTE_COLOR = 0xFFA0A0A0;

    private final List<ItemGrid.Cell> cells;
    private final int tint;
    private final int shown;

    public PreviewPanel(ContainerPreview preview) {
        this.cells = ItemGrid.slots(preview.items());
        this.tint = preview.tint();
        this.shown = Math.min(cells.size(), COLUMNS * MAX_ROWS);
    }

    private int rows() {
        return ItemGrid.rows(shown, COLUMNS);
    }

    public int height() {
        int height = rows() * ItemGrid.CELL + MARGIN * 2;
        return shown < cells.size() ? height + LINE_HEIGHT : height;
    }

    public int width() {
        return COLUMNS * ItemGrid.CELL + 1;
    }

    public void draw(Canvas canvas, Font font, int x, int y) {
        int top = y + MARGIN;
        canvas.fill(x, top, x + width(), top + rows() * ItemGrid.CELL + 1, tint);
        ItemGrid.draw(canvas, font, cells, COLUMNS, shown, x + 1, top + 1, 0x40000000);
        if (shown < cells.size()) {
            canvas.text(font, Component.translatable("lidless.peek.more", cells.size() - shown),
                    x, top + rows() * ItemGrid.CELL + 2, NOTE_COLOR, false);
        }
    }
}
