package dev.lidless.fabric;

import dev.lidless.client.Ids;
import dev.lidless.hud.Canvas;
import dev.lidless.hud.PeekHudRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

final class HudHook {
    private HudHook() {
    }

    static void register() {
        HudElementRegistry.addLast(Ids.of("peek"), (graphics, deltaTracker) -> PeekHudRenderer.render(new Canvas(graphics)));
    }
}
