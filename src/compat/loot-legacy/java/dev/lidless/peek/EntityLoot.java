package dev.lidless.peek;

import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;

public final class EntityLoot {
    private EntityLoot() {
    }

    public static Container container(Entity entity) {
        return entity instanceof ContainerEntity container ? container : null;
    }

    static boolean pending(Entity entity) {
        return entity instanceof ContainerEntity container && container.getLootTable() != null;
    }

    public static ItemStack pickResult(Entity entity) {
        return entity.getPickResult();
    }
}
