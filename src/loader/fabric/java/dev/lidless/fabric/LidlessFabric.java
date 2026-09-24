package dev.lidless.fabric;

import dev.lidless.client.LidlessClient;
import dev.lidless.hud.PeekHudRenderer;
import dev.lidless.tooltip.ContainerPreview;
import dev.lidless.tooltip.ContainerPreviewTooltip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public final class LidlessFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LidlessClient.init(FabricLoader.getInstance().getConfigDir());
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(LidlessClient.MOD_ID, "peek"),
                PeekHudRenderer::render);
        ClientTooltipComponentCallback.EVENT.register(data ->
                data instanceof ContainerPreview preview ? new ContainerPreviewTooltip(preview) : null);
    }
}
