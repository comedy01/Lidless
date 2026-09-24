package dev.lidless.client.gui;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

abstract class OptionsPage extends Screen {
    protected final Screen lastScreen;
    protected final Options options;
    private GridLayout.RowHelper rows;

    OptionsPage(Screen lastScreen, Options options, Component title) {
        super(title);
        this.lastScreen = lastScreen;
        this.options = options;
    }

    protected abstract void addOptions();

    void addRow(AbstractWidget... widgets) {
        for (AbstractWidget widget : widgets) {
            rows.addChild(widget, 2 / widgets.length);
        }
    }

    @Override
    protected void init() {
        GridLayout grid = new GridLayout();
        grid.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
        rows = grid.createRowHelper(2);
        addOptions();
        rows.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(200).build(),
                2, grid.newCellSettings().paddingTop(6));
        rows = null;
        grid.arrangeElements();
        FrameLayout.alignInRectangle(grid, 0, height / 6 - 12, width, height, 0.5F, 0.0F);
        grid.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
