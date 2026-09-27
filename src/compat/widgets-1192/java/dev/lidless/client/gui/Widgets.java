package dev.lidless.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class Widgets {
    private Widgets() {
    }

    public static int x(AbstractWidget widget) {
        return widget.x;
    }

    public static int y(AbstractWidget widget) {
        return widget.y;
    }

    public static void move(AbstractWidget widget, int x, int y) {
        widget.x = x;
        widget.y = y;
    }

    // EditBox has no hint before 1.19.3, so draw it while the box is empty.
    public static EditBox searchBox(Font font, int width, int height, Component label, Component hint) {
        return new EditBox(font, 0, 0, width, height, label) {
            @Override
            public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                super.renderButton(poseStack, mouseX, mouseY, partialTick);
                if (isVisible() && getValue().isEmpty() && !isFocused()) {
                    font.drawShadow(poseStack, hint, x + 4, y + (this.height - 8) / 2, 0xFFFFFFFF);
                }
            }
        };
    }

    public static void focus(EditBox box, boolean focused) {
        box.setFocus(focused);
    }
}
