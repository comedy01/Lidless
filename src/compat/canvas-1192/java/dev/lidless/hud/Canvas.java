package dev.lidless.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class Canvas {
    private static final int RAISED_Z = 300;

    private final PoseStack pose;
    private final int base;
    private int z;

    public Canvas(PoseStack pose) {
        this(pose, 0);
    }

    // Tooltip images draw at the tooltip's blit offset instead of a translated pose.
    public Canvas(PoseStack pose, int base) {
        this.pose = pose;
        this.base = base;
    }

    public int width() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public int height() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public void raise() {
        z = RAISED_Z;
    }

    public void fill(int x0, int y0, int x1, int y1, int color) {
        pose.pushPose();
        pose.translate(0.0, 0.0, base + z);
        GuiComponent.fill(pose, x0, y0, x1, y1, color);
        pose.popPose();
    }

    public void outline(int x, int y, int width, int height, int color) {
        fill(x, y, x + width, y + 1, color);
        fill(x, y + height - 1, x + width, y + height, color);
        fill(x, y + 1, x + 1, y + height - 1, color);
        fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public void item(ItemStack stack, int x, int y) {
        ItemRenderer items = Minecraft.getInstance().getItemRenderer();
        float old = items.blitOffset;
        items.blitOffset = old + z;
        items.renderAndDecorateItem(stack, x, y);
        items.blitOffset = old;
    }

    public void itemDecorations(Font font, ItemStack stack, int x, int y, String count) {
        ItemRenderer items = Minecraft.getInstance().getItemRenderer();
        float old = items.blitOffset;
        items.blitOffset = old + z;
        items.renderGuiItemDecorations(font, stack, x, y, count);
        items.blitOffset = old;
    }

    public void text(Font font, Component text, int x, int y, int color, boolean shadow) {
        pose.pushPose();
        pose.translate(0.0, 0.0, base + z);
        if (shadow) {
            font.drawShadow(pose, text, x, y, color);
        } else {
            font.draw(pose, text, x, y, color);
        }
        pose.popPose();
    }
}
