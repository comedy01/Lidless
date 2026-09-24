package dev.lidless.peek;

import dev.lidless.mixin.HorseInventoryAccessor;
import dev.lidless.mixin.MountMenuAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class Mounts {
    public static final int FIRST_CHEST_SLOT = 0;

    private Mounts() {
    }

    public static boolean isChested(Entity entity) {
        return entity instanceof AbstractChestedHorse;
    }

    public static boolean hasChest(Entity entity) {
        return entity instanceof AbstractChestedHorse horse && horse.hasChest();
    }

    public static List<ItemStack> items(Entity entity) {
        Container inventory = ((HorseInventoryAccessor) entity).lidless$inventory();
        List<ItemStack> items = new ArrayList<>(inventory.getContainerSize());
        for (int i = FIRST_CHEST_SLOT; i < inventory.getContainerSize(); i++) {
            items.add(inventory.getItem(i).copy());
        }
        return items;
    }

    public static boolean isMountMenu(AbstractContainerMenu menu) {
        return menu instanceof HorseInventoryMenu;
    }

    public static Entity mount(AbstractContainerMenu menu) {
        return menu instanceof HorseInventoryMenu mount ? ((MountMenuAccessor) mount).lidless$mount() : null;
    }
}
