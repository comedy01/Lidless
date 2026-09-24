package dev.lidless.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class Canvas {
    private final GuiGraphicsExtractor graphics;

    public Canvas(GuiGraphicsExtractor graphics) {
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
        graphics.outline(x, y, width, height, color);
    }

    public void item(ItemStack stack, int x, int y) {
        graphics.item(stack, x, y);
    }

    public void itemDecorations(Font font, ItemStack stack, int x, int y, String count) {
        graphics.itemDecorations(font, stack, x, y, count);
    }

    public void text(Font font, Component text, int x, int y, int color, boolean shadow) {
        graphics.text(font, text, x, y, color, shadow);
    }
}
