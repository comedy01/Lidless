package dev.lidless.client;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class Ids {
    private Ids() {
    }

    public static ResourceLocation of(String path) {
        return new ResourceLocation(LidlessClient.MOD_ID, path);
    }

    public static String name(ResourceKey<?> key) {
        return key.location().toString();
    }
}
