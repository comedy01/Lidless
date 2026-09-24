package dev.lidless.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class Canvas {
    private final GuiGraphics graphics;

    public Canvas(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    public int width() {
        return graphics.guiWidth();
    }

    public int height() {
        return graphics.guiHeight();
    }

    public void raise() {
        graphics.nextStratum();
    }

    public void fill(int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0, y0, x1, y1, color);
    }

    public void outline(int x, int y, int width, int height, int color) {
        fill(x, y, x + width, y + 1, color);
        fill(x, y + height - 1, x + width, y + height, color);
        fill(x, y + 1, x + 1, y + height - 1, color);
        fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public void item(ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
    }

    public void itemDecorations(Font font, ItemStack stack, int x, int y, String count) {
        graphics.renderItemDecorations(font, stack, x, y, count);
    }

    public void text(Font font, Component text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }
}
