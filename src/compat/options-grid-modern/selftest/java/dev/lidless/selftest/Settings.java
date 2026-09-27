package dev.lidless.selftest;

import net.minecraft.client.gui.screens.Screen;

final class Settings {
    private Settings() {
    }

    static void scroll(Screen screen) {
        screen.mouseScrolled(screen.width / 2.0, screen.height / 2.0, 0.0, -20.0);
    }
}
