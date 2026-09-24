package dev.lidless.client;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class Ids {
    private Ids() {
    }

    public static Identifier of(String path) {
        return Identifier.fromNamespaceAndPath(LidlessClient.MOD_ID, path);
    }

    public static String name(ResourceKey<?> key) {
        return key.identifier().toString();
    }
}
