package dev.lidless.hud;

import net.minecraft.client.Minecraft;

final class HudCompat {
    private HudCompat() {
    }

    static boolean isGuiHidden(Minecraft mc) {
        return mc.options.hideGui;
    }

    static boolean isScreenOpen(Minecraft mc) {
        return mc.screen != null;
    }
}
