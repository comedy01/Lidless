package dev.lidless.tooltip;

import dev.lidless.peek.ItemGrid;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ContainerPreviewTooltip implements ClientTooltipComponent {
    private static final int COLUMNS = 9;
    private static final int MAX_ROWS = 6;
    private static final int MARGIN = 2;
    private static final int LINE_HEIGHT = 10;
    private static final int NOTE_COLOR = 0xFFA0A0A0;

    private final List<ItemGrid.Cell> cells;
    private final int tint;
    private final int shown;

    public ContainerPreviewTooltip(ContainerPreview preview) {
        this.cells = ItemGrid.slots(preview.items());
        this.tint = preview.tint();
        this.shown = Math.min(cells.size(), COLUMNS * MAX_ROWS);
    }

    private int rows() {
        return ItemGrid.rows(shown, COLUMNS);
    }

    @Override
    public int getHeight(Font font) {
        int height = rows() * ItemGrid.CELL + MARGIN * 2;
        return shown < cells.size() ? height + LINE_HEIGHT : height;
    }

    @Override
    public int getWidth(Font font) {
        return COLUMNS * ItemGrid.CELL + 1;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        int top = y + MARGIN;
        int width = COLUMNS * ItemGrid.CELL + 1;
        graphics.fill(x, top, x + width, top + rows() * ItemGrid.CELL + 1, tint);
        ItemGrid.draw(graphics, font, cells, COLUMNS, shown, x + 1, top + 1, 0x40000000);
        if (shown < cells.size()) {
            graphics.text(font, Component.translatable("lidless.peek.more", cells.size() - shown),
                    x, top + rows() * ItemGrid.CELL + 2, NOTE_COLOR, false);
        }
    }
}
