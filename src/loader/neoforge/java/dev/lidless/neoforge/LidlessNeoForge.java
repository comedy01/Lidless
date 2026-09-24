package dev.lidless.neoforge;

import dev.lidless.client.Ids;
import dev.lidless.client.LidlessClient;
import dev.lidless.client.gui.LidlessSettingsScreen;
import dev.lidless.hud.Canvas;
import dev.lidless.hud.PeekHudRenderer;
import dev.lidless.tooltip.ContainerPreview;
import dev.lidless.tooltip.ContainerPreviewTooltip;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = LidlessNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class LidlessNeoForge {
    public static final String MOD_ID = "lidless";

    public LidlessNeoForge(IEventBus modBus, ModContainer container) {
        LidlessClient.init(FMLPaths.CONFIGDIR.get());
        modBus.addListener(LidlessNeoForge::registerGuiLayers);
        modBus.addListener(LidlessNeoForge::registerTooltips);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new LidlessSettingsScreen(parent, Minecraft.getInstance().options));
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Ids.of("peek"), (graphics, deltaTracker) -> PeekHudRenderer.render(new Canvas(graphics)));
    }

    private static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ContainerPreview.class, ContainerPreviewTooltip::new);
    }
}
