package dev.lidless.fabric;

import dev.lidless.hud.Canvas;
import dev.lidless.hud.PeekHudRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

final class HudHook {
    private HudHook() {
    }

    static void register() {
        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> PeekHudRenderer.render(new Canvas(graphics)));
    }
}
