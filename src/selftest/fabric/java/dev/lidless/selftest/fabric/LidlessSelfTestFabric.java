package dev.lidless.selftest.fabric;

import dev.lidless.selftest.LidlessSelfTest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class LidlessSelfTestFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LidlessSelfTest test = new LidlessSelfTest();
        ClientTickEvents.END_CLIENT_TICK.register(client -> test.tick());
    }
}
