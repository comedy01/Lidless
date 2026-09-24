package dev.lidless.peek;

import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.BlockEntity;

final class BlockLoot {
    private BlockLoot() {
    }

    static boolean pending(BlockEntity blockEntity) {
        return blockEntity instanceof RandomizableContainer randomizable && randomizable.getLootTable() != null;
    }
}
