package dev.lidless.forge;

import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.hud.Canvas;
import dev.lidless.hud.PeekHudRenderer;
import dev.lidless.tooltip.TooltipEvents;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

final class ForgeClient {
    private ForgeClient() {
    }

    static void register(ModLoadingContext context, IEventBus modBus) {
        context.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY,
                () -> (client, parent) -> new LidlessSettingsScreen(parent, client.options));
        MinecraftForge.EVENT_BUS.addListener(ForgeClient::onOverlay);
        TooltipEvents.register();
    }

    private static void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            PeekHudRenderer.render(new Canvas(event.getMatrixStack()));
        }
    }
}
