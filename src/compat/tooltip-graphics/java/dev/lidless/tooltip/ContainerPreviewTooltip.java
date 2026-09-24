package dev.lidless.tooltip;

import dev.lidless.hud.Canvas;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public final class ContainerPreviewTooltip implements ClientTooltipComponent {
    private final PreviewPanel panel;

    public ContainerPreviewTooltip(ContainerPreview preview) {
        this.panel = new PreviewPanel(preview);
    }

    @Override
    public int getHeight(Font font) {
        return panel.height();
    }

    @Override
    public int getWidth(Font font) {
        return panel.width();
    }

    @Override
    public void renderImage(Font font, int x, int y, int w, int h, GuiGraphics graphics) {
        panel.draw(new Canvas(graphics), font, x, y);
    }
}
