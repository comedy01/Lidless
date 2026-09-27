package dev.lidless.forge;

import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.hud.Canvas;
import dev.lidless.hud.PeekHudRenderer;
import dev.lidless.tooltip.ContainerPreview;
import dev.lidless.tooltip.ContainerPreviewTooltip;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

final class ForgeClient {
    private ForgeClient() {
    }

    static void register(ModLoadingContext context, IEventBus modBus) {
        modBus.addListener(ForgeClient::setup);
        context.registerExtensionPoint(
                ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory(
                        (client, parent) -> new LidlessSettingsScreen(parent, client.options)));
    }

    private static void setup(FMLClientSetupEvent event) {
        OverlayRegistry.registerOverlayTop("Lidless", (gui, poseStack, partialTick, width, height) ->
                PeekHudRenderer.render(new Canvas(poseStack)));
        MinecraftForgeClient.registerTooltipComponentFactory(ContainerPreview.class, ContainerPreviewTooltip::new);
    }
}
