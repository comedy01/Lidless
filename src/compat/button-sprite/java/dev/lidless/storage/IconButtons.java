package dev.lidless.storage;

import dev.lidless.client.Ids;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;

final class IconButtons {
    private IconButtons() {
    }

    static Button create(String sprite, Component label, Button.OnPress onPress, int size, int icon) {
        return SpriteIconButton.builder(label, onPress, true)
                .size(size, size)
                .sprite(Ids.of(sprite), icon, icon)
                .build();
    }
}
