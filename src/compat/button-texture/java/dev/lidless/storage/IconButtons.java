package dev.lidless.storage;

import dev.lidless.client.Ids;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class IconButtons {
    private IconButtons() {
    }

    static Button create(String sprite, Component label, Button.OnPress onPress, int size, int icon) {
        return new IconButton(Ids.of("textures/gui/sprites/" + sprite + ".png"), label, onPress, size, icon);
    }

    private static final class IconButton extends Button {
        private final ResourceLocation texture;
        private final int icon;

        IconButton(ResourceLocation texture, Component label, OnPress onPress, int size, int icon) {
            super(0, 0, size, size, label, onPress, DEFAULT_NARRATION);
            this.texture = texture;
            this.icon = icon;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            int x = getX() + (width - icon) / 2;
            int y = getY() + (height - icon) / 2;
            graphics.blit(texture, x, y, 0, 0, icon, icon, icon, icon);
        }

        @Override
        public void renderString(GuiGraphics graphics, Font font, int color) {
        }
    }
}
