package dev.lidless.gametest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class Screens {
    private Screens() {
    }

    static Screen current(Minecraft mc) {
        return mc.gui.screen();
    }
}
