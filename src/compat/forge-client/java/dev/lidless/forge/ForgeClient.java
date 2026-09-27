package dev.lidless.forge;

import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.hud.Canvas;
import dev.lidless.hud.PeekHudRenderer;
import dev.lidless.tooltip.ContainerPreview;
import dev.lidless.tooltip.ContainerPreviewTooltip;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

final class ForgeClient {
    private ForgeClient() {
    }

    static void register(ModLoadingContext context, IEventBus modBus) {
        modBus.addListener(ForgeClient::registerOverlays);
        modBus.addListener(ForgeClient::registerTooltips);
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> new LidlessSettingsScreen(parent, client.options)));
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("peek", (gui, graphics, partialTick, width, height) -> PeekHudRenderer.render(new Canvas(graphics)));
    }

    private static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ContainerPreview.class, ContainerPreviewTooltip::new);
    }
}
