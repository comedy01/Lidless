package dev.lidless.peek;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;

// Before 1.19 only container minecarts hold items, and their loot table has no getter.
public final class EntityLoot {
    private EntityLoot() {
    }

    public static Container container(Entity entity) {
        return entity instanceof AbstractMinecartContainer container ? container : null;
    }

    static boolean pending(Entity entity) {
        return entity instanceof AbstractMinecartContainer && entity.saveWithoutId(new CompoundTag()).contains("LootTable");
    }

    public static ItemStack pickResult(Entity entity) {
        return entity.getPickResult();
    }
}
