package dev.lidless.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.IdentityHashMap;
import java.util.Map;

abstract class OptionsPage extends Screen {
    private static final int GAP = 10;

    protected final Screen lastScreen;
    protected final Options options;
    private final Map<AbstractWidget, Component> tooltips = new IdentityHashMap<>();
    private int y;

    OptionsPage(Screen lastScreen, Options options, Component title) {
        super(title);
        this.lastScreen = lastScreen;
        this.options = options;
    }

    protected abstract void addOptions();

    void addRow(AbstractWidget... widgets) {
        int total = 0;
        for (AbstractWidget widget : widgets) {
            total += widget.getWidth();
        }
        int x = (width - total - GAP * (widgets.length - 1)) / 2;
        for (AbstractWidget widget : widgets) {
            widget.x = x;
            widget.y = y;
            addRenderableWidget(widget);
            x += widget.getWidth() + GAP;
        }
        y += 24;
    }

    // Widgets have no tooltip of their own before 1.19.3, so the page draws them.
    <T extends AbstractWidget> T tooltip(T widget, Component tooltip) {
        tooltips.put(widget, tooltip);
        return widget;
    }

    @Override
    protected void init() {
        tooltips.clear();
        y = height / 6 - 12;
        addOptions();
        y += 6;
        addRow(new Button(0, 0, 200, 20, CommonComponents.GUI_DONE, button -> onClose()));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        drawCenteredString(poseStack, font, title, width / 2, 15, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
        for (Map.Entry<AbstractWidget, Component> entry : tooltips.entrySet()) {
            AbstractWidget widget = entry.getKey();
            if (widget.visible
                    && mouseX >= widget.x && mouseX < widget.x + widget.getWidth()
                    && mouseY >= widget.y && mouseY < widget.y + widget.getHeight()) {
                renderTooltip(poseStack, font.split(entry.getValue(), 200), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
    }
}
