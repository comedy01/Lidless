package dev.lidless.hud;

import net.minecraft.client.Minecraft;

final class HudCompat {
    private HudCompat() {
    }

    static boolean isGuiHidden(Minecraft mc) {
        return mc.gui.hud.isHidden();
    }

    static boolean isScreenOpen(Minecraft mc) {
        return mc.gui.screen() != null;
    }
}
