package dev.lidless.storage;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lidless.client.Ids;
import dev.lidless.client.Texts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class IconButtons {
    private static Component pendingTooltip;
    private static int pendingX;
    private static int pendingY;

    private IconButtons() {
    }

    public static void renderPendingTooltip(PoseStack poseStack) {
        if (pendingTooltip != null && Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.renderTooltip(poseStack, pendingTooltip, pendingX, pendingY);
        }
        pendingTooltip = null;
    }

    static Button create(String sprite, Component label, Button.OnPress onPress, int size, int icon) {
        return new IconButton(Ids.of("textures/gui/sprites/" + sprite + ".png"), label, onPress, size, icon);
    }

    private static final class IconButton extends Button {
        private final ResourceLocation texture;
        private final int icon;

        IconButton(ResourceLocation texture, Component label, OnPress onPress, int size, int icon) {
            super(0, 0, size, size, label, onPress,
                    (button, poseStack, mouseX, mouseY) -> {
                        pendingTooltip = label;
                        pendingX = mouseX;
                        pendingY = mouseY;
                    });
            this.texture = texture;
            this.icon = icon;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            Component message = getMessage();
            setMessage(Texts.empty());
            super.renderButton(poseStack, mouseX, mouseY, partialTick);
            setMessage(message);
            Minecraft.getInstance().getTextureManager().bind(texture);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int x = this.x + (width - icon) / 2;
            int y = this.y + (height - icon) / 2;
            GuiComponent.blit(poseStack, x, y, 0, 0, icon, icon, icon, icon);
        }
    }
}
