package dev.lidless.peek;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

final class BlockLoot {
    private BlockLoot() {
    }

    static boolean pending(BlockEntity blockEntity) {
        return blockEntity instanceof RandomizableContainerBlockEntity
                && blockEntity.save(new CompoundTag()).contains("LootTable");
    }
}
