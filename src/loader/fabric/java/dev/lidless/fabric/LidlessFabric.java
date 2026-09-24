package dev.lidless.fabric;

import dev.lidless.client.LidlessClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class LidlessFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LidlessClient.init(FabricLoader.getInstance().getConfigDir());
        HudHook.register();
        TooltipHook.register();
    }
}
