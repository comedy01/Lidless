package dev.lidless.showcase;

import net.fabricmc.api.ClientModInitializer;

public final class LidlessShowcaseFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Showcase.start();
    }
}
