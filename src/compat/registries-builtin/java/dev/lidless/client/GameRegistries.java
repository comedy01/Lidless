package dev.lidless.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public final class GameRegistries {
    private GameRegistries() {
    }

    public static String itemNamespace(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace();
    }

    public static String itemPath(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    public static int itemId(Item item) {
        return BuiltInRegistries.ITEM.getId(item);
    }

    public static boolean hasEntityType(String path) {
        return BuiltInRegistries.ENTITY_TYPE.keySet().stream().anyMatch(id -> id.getPath().equals(path));
    }
}
