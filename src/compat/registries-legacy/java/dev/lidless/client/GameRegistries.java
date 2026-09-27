package dev.lidless.client;

import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

public final class GameRegistries {
    private GameRegistries() {
    }

    public static String itemNamespace(Item item) {
        return Registry.ITEM.getKey(item).getNamespace();
    }

    public static String itemPath(Item item) {
        return Registry.ITEM.getKey(item).getPath();
    }

    public static int itemId(Item item) {
        return Registry.ITEM.getId(item);
    }

    public static boolean hasEntityType(String path) {
        return Registry.ENTITY_TYPE.keySet().stream().anyMatch(id -> id.getPath().equals(path));
    }
}
