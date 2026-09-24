package dev.lidless.peek;

import net.minecraft.world.entity.vehicle.ContainerEntity;

final class EntityLoot {
    private EntityLoot() {
    }

    static boolean pending(ContainerEntity container) {
        return container.getContainerLootTable() != null;
    }
}
