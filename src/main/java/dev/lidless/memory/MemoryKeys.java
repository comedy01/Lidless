package dev.lidless.memory;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class MemoryKeys {
    public static final String ENDER_CHEST = "ender";

    private MemoryKeys() {
    }

    public static String block(ResourceKey<Level> dimension, BlockPos pos) {
        return "block/" + dimension.identifier() + "/" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static String entity(Entity entity) {
        return "entity/" + entity.getUUID();
    }
}
